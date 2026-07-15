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
from datetime import datetime
from flask import Flask, render_template, request, jsonify, Response, send_file, g
from flask_cors import CORS
import yt_dlp

app = Flask(__name__)
CORS(app)

# Visit tracking
VISIT_LOG = os.path.join(os.path.dirname(__file__), "visit_logs.json")

# Track downloads in progress (in-memory only)
downloads = {}
downloads_lock = threading.Lock()


def log_visit():
    """Log every request to visit_logs.json with real IP."""
    ip = request.headers.get("X-Forwarded-For", request.remote_addr)
    if ip and "," in ip:
        ip = ip.split(",")[0].strip()

    visit = {
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "ip_address": ip or "unknown",
        "user_agent": request.headers.get("User-Agent", ""),
        "path": request.path,
        "referrer": request.headers.get("Referer", ""),
        "method": request.method,
        "host": request.headers.get("Host", ""),
    }

    visits = []
    if os.path.exists(VISIT_LOG):
        try:
            with open(VISIT_LOG, "r") as f:
                content = f.read().strip()
                if content:
                    visits = json.loads(content)
        except (json.JSONDecodeError, Exception):
            visits = []

    visits.append(visit)
    if len(visits) > 10000:
        visits = visits[-10000:]

    with open(VISIT_LOG, "w") as f:
        json.dump(visits, f, indent=2, ensure_ascii=False)


@app.before_request
def before_request():
    log_visit()


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
        self._tmpfile = None
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


# Supported platforms (displayed on frontend)
SUPPORTED_PLATFORMS = [
    {"name": "YouTube", "icon": "▶️", "domain": "youtube.com"},
    {"name": "Instagram", "icon": "📷", "domain": "instagram.com"},
    {"name": "TikTok", "icon": "🎵", "domain": "tiktok.com"},
    {"name": "Twitter/X", "icon": "🐦", "domain": "x.com"},
    {"name": "Facebook", "icon": "📘", "domain": "facebook.com"},
    {"name": "Reddit", "icon": "🤖", "domain": "reddit.com"},
    {"name": "Twitch", "icon": "🎮", "domain": "twitch.tv"},
    {"name": "Vimeo", "icon": "🎬", "domain": "vimeo.com"},
    {"name": "SoundCloud", "icon": "🎧", "domain": "soundcloud.com"},
    {"name": "Spotify", "icon": "🟢", "domain": "spotify.com"},
    {"name": "Bilibili", "icon": "📺", "domain": "bilibili.com"},
    {"name": "Dailymotion", "icon": "🎥", "domain": "dailymotion.com"},
    {"name": "Pinterest", "icon": "📌", "domain": "pinterest.com"},
    {"name": "Snapchat", "icon": "👻", "domain": "snapchat.com"},
    {"name": "Threads", "icon": "🧵", "domain": "threads.net"},
]


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
    elif 'facebook.com' in url_lower or 'fb.com' in url_lower:
        return 'facebook'
    elif 'reddit.com' in url_lower:
        return 'reddit'
    elif 'twitch.tv' in url_lower:
        return 'twitch'
    elif 'vimeo.com' in url_lower:
        return 'vimeo'
    elif 'soundcloud.com' in url_lower:
        return 'soundcloud'
    elif 'spotify.com' in url_lower:
        return 'spotify'
    elif 'bilibili.com' in url_lower:
        return 'bilibili'
    elif 'dailymotion.com' in url_lower:
        return 'dailymotion'
    elif 'pinterest.com' in url_lower or 'pin.it' in url_lower:
        return 'pinterest'
    elif 'snapchat.com' in url_lower:
        return 'snapchat'
    elif 'threads.net' in url_lower:
        return 'threads'
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
        return jsonify({'success': False, 'error': 'Unsupported platform. Try a YouTube, Instagram, TikTok, Twitter/X, Facebook, Reddit, Twitch, Vimeo, SoundCloud, or other major platform URL.'})
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

    safe_title = progress.title or 'video'
    safe_title = "".join(c for c in safe_title if c.isalnum() or c in ' ._-').strip()[:80]
    download_name = f"{safe_title}.mp4"

    def stream_and_cleanup():
        with open(filepath, 'rb') as f:
            while True:
                chunk = f.read(8192)
                if not chunk:
                    break
                yield chunk
        try:
            os.unlink(filepath)
        except Exception:
            pass
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


@app.route('/api/stats')
def api_stats():
    """Return visit stats."""
    visits = []
    if os.path.exists(VISIT_LOG):
        try:
            with open(VISIT_LOG, "r") as f:
                visits = json.load(f)
        except Exception:
            pass
    return jsonify({'total_visits': len(visits), 'recent': visits[-20:]})


if __name__ == '__main__':
    print("Zingy Web App — streaming mode (no disk storage)")
    print(f"Visit logs → {VISIT_LOG}")
    print("Starting server on http://localhost:4321")
    app.run(host='0.0.0.0', port=4321, debug=False, threaded=True)
