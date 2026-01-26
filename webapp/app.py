"""
Video Downloader Web App - Flask server with yt-dlp backend
Runs on port 4321
"""

import os
import json
import uuid
import threading
from datetime import datetime
from flask import Flask, render_template, request, jsonify, send_from_directory
import yt_dlp

app = Flask(__name__)

# Configuration
DOWNLOAD_DIR = os.path.expanduser("~/Downloads/VideoDownloader")
os.makedirs(DOWNLOAD_DIR, exist_ok=True)

# Track downloads in progress
downloads = {}
downloads_lock = threading.Lock()


class DownloadProgress:
    """Track download progress"""
    def __init__(self, download_id):
        self.download_id = download_id
        self.progress = 0
        self.status = "starting"
        self.filename = ""
        self.error = None
        self.speed = ""
        self.eta = ""
        self.title = ""

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
            self.filename = d.get('filename', '')

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
    """Detect video platform from URL"""
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
    """Get available formats for a video"""
    try:
        ydl_opts = {
            'quiet': True,
            'no_warnings': True,
        }

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

                    # Skip if no video and no audio
                    if vcodec == 'none' and acodec == 'none':
                        continue

                    # Create display label
                    if vcodec != 'none' and acodec != 'none':
                        label = f"{resolution} ({ext}) - Video+Audio"
                    elif vcodec != 'none':
                        label = f"{resolution} ({ext}) - Video only"
                    else:
                        label = f"Audio ({ext})"

                    # Deduplicate
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

            # Sort by height (resolution) descending
            formats.sort(key=lambda x: (x['has_video'], x['height']), reverse=True)

            # Add common presets at top
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
                'formats': presets + formats[:20]  # Limit to 20 formats
            }

    except Exception as e:
        return {
            'success': False,
            'error': str(e)
        }


def download_video_task(download_id, url, format_id):
    """Background task to download video"""
    progress = downloads.get(download_id)
    if not progress:
        return

    try:
        platform = detect_platform(url)

        outtmpl = os.path.join(DOWNLOAD_DIR, '%(title).80s.%(ext)s')

        ydl_opts = {
            'format': format_id or 'best[ext=mp4]/best',
            'outtmpl': outtmpl,
            'progress_hooks': [progress.hook],
            'quiet': True,
            'no_warnings': True,
            'noplaylist': True,
            'merge_output_format': 'mp4',
            'socket_timeout': 30,
            'retries': 3,
            'restrictfilenames': True,
        }

        # Platform-specific options
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
            progress.filename = ydl.prepare_filename(info)

    except Exception as e:
        progress.status = "error"
        progress.error = str(e)


@app.route('/')
def index():
    """Serve the main page"""
    return render_template('index.html')


@app.route('/api/formats', methods=['POST'])
def api_formats():
    """Get available formats for a URL"""
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
    """Start a download"""
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

    # Start download in background thread
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

    return jsonify({
        'success': True,
        **progress.to_dict()
    })


@app.route('/api/files')
def api_files():
    """List downloaded files"""
    files = []

    if os.path.exists(DOWNLOAD_DIR):
        for filename in os.listdir(DOWNLOAD_DIR):
            filepath = os.path.join(DOWNLOAD_DIR, filename)
            if os.path.isfile(filepath):
                stat = os.stat(filepath)
                files.append({
                    'name': filename,
                    'size': stat.st_size,
                    'size_formatted': f"{stat.st_size / 1024 / 1024:.2f} MB",
                    'modified': datetime.fromtimestamp(stat.st_mtime).isoformat(),
                })

    # Sort by modified date, newest first
    files.sort(key=lambda x: x['modified'], reverse=True)

    return jsonify({
        'success': True,
        'files': files,
        'download_dir': DOWNLOAD_DIR
    })


@app.route('/api/delete', methods=['POST'])
def api_delete():
    """Delete a downloaded file"""
    data = request.get_json()
    filename = data.get('filename', '')

    if not filename:
        return jsonify({'success': False, 'error': 'Filename required'})

    # Security: prevent path traversal
    filename = os.path.basename(filename)
    filepath = os.path.join(DOWNLOAD_DIR, filename)

    if os.path.exists(filepath):
        os.remove(filepath)
        return jsonify({'success': True, 'message': 'File deleted'})
    else:
        return jsonify({'success': False, 'error': 'File not found'})


@app.route('/downloads/<filename>')
def serve_download(filename):
    """Serve downloaded files"""
    return send_from_directory(DOWNLOAD_DIR, filename, as_attachment=True)


if __name__ == '__main__':
    print(f"Download directory: {DOWNLOAD_DIR}")
    print(f"Starting server on http://localhost:4321")
    app.run(host='0.0.0.0', port=4321, debug=False, threaded=True)
