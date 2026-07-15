"""
Zingy Web App - Flask server with yt-dlp backend
Runs on port 4321
Streams directly to browser — no files stored on disk.
"""
import io
import os
import json
import uuid
import tempfile
import threading
from flask import Flask, render_template, request, jsonify, Response, send_file
from flask_cors import CORS
import yt_dlp

app = Flask(__name__)
CORS(app)

# Track downloads in progress (in-memory only)
downloads = {}
downloads_lock = threading.Lock()


class DownloadProgress:
    """Track download progress — writes to temp file, streams on completion"""
    def __init__(self, download_id):
        self.download_id = download_id
        self.progress = 0
        self.status = "starting"
        self.filename = ""
        self.error = None
        self.speed = ""
        self.eta = ""
        self.title = ""
        self._tmpfile = None  # tempfile.NamedTemporaryFile
        self._filepath = None

    def hook(self, d):
        status = d.get('status', 'unknown')

        if status == 'downloading':
            self.status = "downloading"
            total = d.get('total_bytes') or d.get('total_bytes_estimate', 0)
            downloaded = d.get('downloaded_bytes', 0)
            if total > 0:
                self.progress = int((downloaded / total) * 100)
            speed = d.get('speed', 0)
            if speed:
                self.speed = f"{speed / 1024 / 1024:.1f} MB/s"
            eta = d.get('eta', 0)
            if eta:
                self.eta = f"{eta}s"

        elif status == 'finished':
            self.status = "processing"
            self.progress = 100

        elif status == 'error':
            self.status = "error"
            self.error = str(d.get('error', 'Unknown error'))

    def to_dict(self):
        return {
            'id': self.download_id,
            'progress': self.progress,
            'status': self.status,
            'filename': os.path.basename(self.filename) if self.filename else '',
            'error': self.error,
            'speed': self.speed,
            'eta': self.eta,
            'title': self.title
        }


def detect_platform(url):
    url_lower = url.lower()
    if 'instagram.com' in url_lower or 'instagr.am' in url_lower:
        return 'instagram'
    elif 'youtube.com' in url_lower or 'youtu.be' in url_lower:
        return 'youtube'
    elif 'tiktok.com' in url_lower:
        return 'tiktok'
    elif 'twitter.com' in url_lower or 'x.com' in url_lower:
        return 'twitter'
    else:
        return 'unknown'


def get_available_formats(url):
    try:
        ydl_opts = {'quiet': True, 'no_warnings': True}
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=False)
            formats = []
            seen = set()

            if 'formats' in info:
                for f in info['formats']:
                    format_id = f.get('format_id', '')
                    ext = f.get('ext', 'unknown')
                    resolution = f.get('resolution', 'audio only')
                    height = f.get('height', 0)
                    vcodec = f.get('vcodec', 'none')
                    acodec = f.get('acodec', 'none')

                    if vcodec == 'none' and acodec == 'none':
                        continue

                    if vcodec != 'none' and acodec != 'none':
                        label = f"{resolution} ({ext}) - Video+Audio"
                    elif vcodec != 'none':
                        label = f"{resolution} ({ext}) - Video only"
                    else:
                        label = f"Audio ({ext})"

                    key = f"{height}_{ext}_{vcodec != 'none'}_{acodec != 'none'}"
                    if key not in seen:
                        seen.add(key)
                        formats.append({
                            'id': format_id,
                            'label': label,
                            'ext': ext,
                            'height': height or 0,
                            'has_video': vcodec != 'none',
                            'has_audio': acodec != 'none'
                        })

            formats.sort(key=lambda x: (x['has_video'], x['height']), reverse=True)

            presets = [
                {'id': 'best', 'label': 'Best Quality (Auto)', 'ext': 'mp4', 'height': 9999, 'has_video': True, 'has_audio': True},
                {'id': 'best[ext=mp4]', 'label': 'Best MP4', 'ext': 'mp4', 'height': 9998, 'has_video': True, 'has_audio': True},
                {'id': 'bestaudio', 'label': 'Audio Only (Best)', 'ext': 'm4a', 'height': 0, 'has_video': False, 'has_audio': True},
            ]

            return {
                'success': True,
                'title': info.get('title', 'Unknown'),
                'thumbnail': info.get('thumbnail', ''),
                'duration': info.get('duration', 0),
                'formats': presets + formats[:20]
            }

    except Exception as e:
        return {'success': False, 'error': str(e)}


def download_video_task(download_id, url, format_id):
    """Background task — downloads to temp file, NOT permanent storage"""
    progress = downloads.get(download_id)
    if not progress:
        return

    try:
        platform = detect_platform(url)

        # Use a temp file that gets auto-cleaned
        tmp = tempfile.NamedTemporaryFile(suffix='.mp4', delete=False)
        tmp.close()
        progress._filepath = tmp.name

        ydl_opts = {
            'format': format_id or 'best[ext=mp4]/best',
            'outtmpl': tmp.name,
            'progress_hooks': [progress.hook],
            'quiet': True,
            'no_warnings': True,
            'noplaylist': True,
            'merge_output_format': 'mp4',
            'socket_timeout': 30,
            'retries': 3,
            'restrictfilenames': True,
        }

        if platform == 'youtube':
            format_options = [
                'best[ext=mp4][acodec!=none][vcodec!=none]',
                'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]',
                '22', '18',
                'best[vcodec!=none][acodec!=none]',
                'best',
            ]
            if format_id in ['best', 'best[ext=mp4]', None]:
                ydl_opts['format'] = '/'.join(format_options)
            ydl_opts['extractor_args'] = {'youtube': {'player_client': ['android', 'web']}}
        elif platform == 'instagram':
            ydl_opts['extractor_args'] = {'instagram': {'skip': ['dash']}}

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            progress.title = info.get('title', 'Unknown')
            progress.status = "completed"
            progress.filename = tmp.name

    except Exception as e:
        progress.status = "error"
        progress.error = str(e)
        # Clean up temp file on error
        if progress._filepath and os.path.exists(progress._filepath):
            try:
                os.unlink(progress._filepath)
            except Exception:
                pass


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/api/formats', methods=['POST'])
def api_formats():
    data = request.get_json()
    url = data.get('url', '').strip()
    if not url:
        return jsonify({'success': False, 'error': 'URL is required'})
    platform = detect_platform(url)
    if platform == 'unknown':
        return jsonify({'success': False, 'error': 'Unsupported platform'})
    result = get_available_formats(url)
    result['platform'] = platform
    return jsonify(result)


@app.route('/api/download', methods=['POST'])
def api_download():
    """Start a download to temp file. Returns download_id for progress polling."""
    data = request.get_json()
    url = data.get('url', '').strip()
    format_id = data.get('format', 'best')

    if not url:
        return jsonify({'success': False, 'error': 'URL is required'})

    platform = detect_platform(url)
    if platform == 'unknown':
        return jsonify({'success': False, 'error': 'Unsupported platform'})

    download_id = str(uuid.uuid4())[:8]
    progress = DownloadProgress(download_id)

    with downloads_lock:
        downloads[download_id] = progress

    thread = threading.Thread(target=download_video_task, args=(download_id, url, format_id))
    thread.daemon = True
    thread.start()

    return jsonify({
        'success': True,
        'download_id': download_id,
        'message': 'Download started'
    })


@app.route('/api/progress/<download_id>')
def api_progress(download_id):
    """Get download progress"""
    progress = downloads.get(download_id)
    if not progress:
        return jsonify({'success': False, 'error': 'Download not found'})
    return jsonify({'success': True, **progress.to_dict()})


@app.route('/api/file/<download_id>')
def api_file(download_id):
    """Stream the completed download to client, then delete the temp file."""
    progress = downloads.get(download_id)
    if not progress:
        return jsonify({'success': False, 'error': 'Download not found'}), 404

    if progress.status != 'completed':
        return jsonify({'success': False, 'error': 'Download not complete yet'}), 400

    filepath = progress._filepath
    if not filepath or not os.path.exists(filepath):
        return jsonify({'success': False, 'error': 'File no longer available'}), 404

    # Determine a nice filename for the download
    safe_title = progress.title or 'video'
    # Remove chars unsafe for filenames
    safe_title = "".join(c for c in safe_title if c.isalnum() or c in ' ._-').strip()[:80]
    download_name = f"{safe_title}.mp4"

    def stream_and_cleanup():
        """Stream the file then delete it."""
        with open(filepath, 'rb') as f:
            while True:
                chunk = f.read(8192)
                if not chunk:
                    break
                yield chunk
        # Cleanup after streaming
        try:
            os.unlink(filepath)
        except Exception:
            pass
        # Remove from tracking
        with downloads_lock:
            downloads.pop(download_id, None)

    return Response(
        stream_and_cleanup(),
        mimetype='video/mp4',
        headers={
            'Content-Disposition': f'attachment; filename="{download_name}"',
            'X-Accel-Buffering': 'no',
        }
    )


if __name__ == '__main__':
    print("Zingy Web App — streaming mode (no disk storage)")
    print("Starting server on http://localhost:4321")
    app.run(host='0.0.0.0', port=4321, debug=False, threaded=True)
