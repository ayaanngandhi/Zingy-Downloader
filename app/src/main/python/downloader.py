"""
Video Downloader - yt-dlp wrapper for Android
"""

import os
import json
import traceback
import shutil
from datetime import datetime
import yt_dlp


class Logger:
    """Collects log messages with timestamps"""
    def __init__(self):
        self.logs = []

    def log(self, message, level="INFO"):
        timestamp = datetime.now().strftime("%H:%M:%S")
        entry = f"[{timestamp}] [{level}] {message}"
        self.logs.append(entry)
        print(entry)

    def get_logs(self):
        return self.logs

    def debug(self, msg):
        self.log(msg, "DEBUG")

    def warning(self, msg):
        self.log(msg, "WARN")

    def error(self, msg):
        self.log(msg, "ERROR")


class DownloadProgress:
    """Tracks download progress"""
    def __init__(self, logger, callback=None):
        self.callback = callback
        self.progress = 0
        self.status = "idle"
        self.filename = ""
        self.actual_filename = None
        self.error = None
        self.logger = logger
        self.hook_call_count = 0

    def hook(self, d):
        self.hook_call_count += 1
        status = d.get('status', 'unknown')

        self.logger.log(f"Hook #{self.hook_call_count}: status={status}")

        # Log all keys in the dict for debugging
        if self.hook_call_count == 1:
            self.logger.log(f"Hook dict keys: {list(d.keys())}")

        if status == 'downloading':
            self.status = "downloading"
            total = d.get('total_bytes') or d.get('total_bytes_estimate', 0)
            downloaded = d.get('downloaded_bytes', 0)
            speed = d.get('speed', 0)
            eta = d.get('eta', 0)

            self.logger.log(f"Downloading: {downloaded}/{total} bytes, speed={speed}, eta={eta}")

            if total > 0:
                self.progress = int((downloaded / total) * 100)
                self.logger.log(f"Progress: {self.progress}%")

            # Log filename being downloaded
            if 'filename' in d:
                self.logger.log(f"Downloading to: {d['filename']}")
            if 'tmpfilename' in d:
                self.logger.log(f"Temp file: {d['tmpfilename']}")

            if self.callback:
                self.callback(self.progress, self.status, "")

        elif status == 'finished':
            self.status = "finished"
            self.filename = d.get('filename', '')
            self.actual_filename = d.get('filename', '')
            self.progress = 100

            self.logger.log(f"=== DOWNLOAD FINISHED ===")
            self.logger.log(f"Filename from hook: {self.actual_filename}")

            # Log all info from finished hook
            for key in ['filename', 'tmpfilename', 'total_bytes', 'elapsed']:
                if key in d:
                    self.logger.log(f"  {key}: {d[key]}")

            # Check if file exists
            if self.actual_filename:
                exists = os.path.exists(self.actual_filename)
                self.logger.log(f"File exists check: {exists}")
                if exists:
                    size = os.path.getsize(self.actual_filename)
                    self.logger.log(f"File size: {size} bytes")

            if self.callback:
                self.callback(100, self.status, self.filename)

        elif status == 'error':
            self.logger.error(f"Hook error: {d.get('error', 'Unknown')}")

        else:
            self.logger.log(f"Unknown status: {status}, keys: {list(d.keys())}")


def detect_platform(url):
    """Detect if URL is Instagram or YouTube"""
    url_lower = url.lower()
    if 'instagram.com' in url_lower or 'instagr.am' in url_lower:
        return 'instagram'
    elif 'youtube.com' in url_lower or 'youtu.be' in url_lower:
        return 'youtube'
    else:
        return 'unknown'


def download_video(url, output_dir, progress_callback=None):
    """
    Download video from URL to specified directory
    """
    logger = Logger()

    try:
        logger.log(f"========== DOWNLOAD START ==========")
        logger.log(f"URL: {url}")
        logger.log(f"Target dir: {output_dir}")
        logger.log(f"CWD: {os.getcwd()}")
        logger.log(f"Script dir: {os.path.dirname(os.path.abspath(__file__))}")

        # Create output directory
        try:
            os.makedirs(output_dir, exist_ok=True)
            logger.log(f"Directory created/exists: {output_dir}")
        except Exception as dir_err:
            logger.error(f"Cannot create dir: {dir_err}")
            return json.dumps({
                'success': False,
                'error': f"Cannot create directory: {dir_err}",
                'logs': logger.get_logs()
            })

        # Test write
        test_file = os.path.join(output_dir, '.test')
        try:
            with open(test_file, 'w') as f:
                f.write('x')
            os.remove(test_file)
            logger.log("Write test: PASSED")
        except Exception as e:
            logger.error(f"Write test FAILED: {e}")
            return json.dumps({
                'success': False,
                'error': f"Cannot write to directory: {e}",
                'logs': logger.get_logs()
            })

        platform = detect_platform(url)
        logger.log(f"Platform: {platform}")

        progress = DownloadProgress(logger, progress_callback)

        # Use absolute path
        abs_output_dir = os.path.abspath(output_dir)
        outtmpl = os.path.join(abs_output_dir, '%(title).80s.%(ext)s')
        logger.log(f"Output template: {outtmpl}")

        # yt-dlp options with verbose logging
        ydl_opts = {
            'format': 'best[ext=mp4]/best',
            'outtmpl': outtmpl,
            'progress_hooks': [progress.hook],
            'quiet': False,
            'verbose': True,  # Enable verbose mode
            'no_warnings': False,
            'noplaylist': True,
            'merge_output_format': 'mp4',
            'socket_timeout': 30,
            'retries': 3,
            'restrictfilenames': True,
            'windowsfilenames': True,
            'noprogress': False,  # Show progress
            'consoletitle': False,
            # Force download even if file exists
            'overwrites': True,
        }

        if platform == 'instagram':
            logger.log("Using Instagram config")
            ydl_opts['extractor_args'] = {'instagram': {'skip': ['dash']}}
        elif platform == 'youtube':
            logger.log("Using YouTube config (no ffmpeg)")
            # Use pre-merged formats only (no ffmpeg needed)
            # 18 = 360p mp4, 22 = 720p mp4, 37 = 1080p mp4 (pre-merged with audio)
            ydl_opts['format'] = 'best[ext=mp4][acodec!=none][vcodec!=none]/18/22/best[ext=mp4]/best'
            # Don't try to merge formats
            ydl_opts['merge_output_format'] = None
            # Prefer formats that have both video and audio
            ydl_opts['prefer_free_formats'] = False

        logger.log("Creating YoutubeDL instance...")
        logger.log(f"yt-dlp version: {yt_dlp.version.__version__}")

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            logger.log("Calling extract_info with download=True...")

            try:
                info = ydl.extract_info(url, download=True)
            except Exception as extract_err:
                logger.error(f"extract_info failed: {extract_err}")
                logger.error(f"Traceback: {traceback.format_exc()}")
                return json.dumps({
                    'success': False,
                    'error': f"Extract failed: {extract_err}",
                    'logs': logger.get_logs()
                })

            if info is None:
                logger.error("info is None!")
                return json.dumps({
                    'success': False,
                    'error': "No video info returned",
                    'logs': logger.get_logs()
                })

            # Log all info keys
            logger.log(f"Info keys: {list(info.keys())[:20]}...")  # First 20 keys

            title = info.get('title', 'Unknown')
            logger.log(f"Title: {title}")

            # Log important info fields
            for key in ['id', 'ext', 'format', 'format_id', 'requested_downloads', '_filename', 'filepath']:
                if key in info:
                    val = info[key]
                    if isinstance(val, str) and len(val) > 100:
                        val = val[:100] + "..."
                    logger.log(f"info[{key}]: {val}")

            # Check requested_downloads for actual file path
            if 'requested_downloads' in info:
                logger.log(f"requested_downloads count: {len(info['requested_downloads'])}")
                for i, dl in enumerate(info['requested_downloads']):
                    logger.log(f"Download #{i}: {dl.get('filepath', dl.get('filename', 'N/A'))}")
                    if 'filepath' in dl:
                        filepath = dl['filepath']
                        logger.log(f"  Filepath: {filepath}")
                        logger.log(f"  Exists: {os.path.exists(filepath)}")
                        if os.path.exists(filepath):
                            logger.log(f"  Size: {os.path.getsize(filepath)} bytes")

            prepared_filename = ydl.prepare_filename(info)
            logger.log(f"Prepared filename: {prepared_filename}")
            logger.log(f"Prepared exists: {os.path.exists(prepared_filename)}")

            # Also check hook captured filename
            if progress.actual_filename:
                logger.log(f"Hook filename: {progress.actual_filename}")
                logger.log(f"Hook file exists: {os.path.exists(progress.actual_filename)}")

            logger.log(f"Total hook calls: {progress.hook_call_count}")

            # Search for the file
            actual_file = None

            # Check prepared filename and .mp4 variant
            candidates = [prepared_filename]
            base, ext = os.path.splitext(prepared_filename)
            if ext != '.mp4':
                candidates.append(base + '.mp4')

            # Add hook filename if different
            if progress.actual_filename and progress.actual_filename not in candidates:
                candidates.append(progress.actual_filename)

            for candidate in candidates:
                logger.log(f"Checking candidate: {candidate}")
                if os.path.exists(candidate):
                    actual_file = candidate
                    logger.log(f"FOUND: {candidate}")
                    break

            # Search output directory
            if not actual_file:
                logger.log(f"Listing {abs_output_dir}:")
                try:
                    contents = os.listdir(abs_output_dir)
                    logger.log(f"Contents ({len(contents)} items): {contents}")
                    for f in contents:
                        if f.endswith(('.mp4', '.mkv', '.webm', '.m4a')):
                            full = os.path.join(abs_output_dir, f)
                            logger.log(f"Video file found: {full}")
                            actual_file = full
                            break
                except Exception as e:
                    logger.error(f"List dir error: {e}")

            # Check CWD
            if not actual_file:
                cwd = os.getcwd()
                logger.log(f"Checking CWD: {cwd}")
                try:
                    contents = os.listdir(cwd)
                    videos = [f for f in contents if f.endswith(('.mp4', '.mkv', '.webm'))]
                    logger.log(f"Videos in CWD: {videos}")
                    for f in videos:
                        full = os.path.join(cwd, f)
                        dest = os.path.join(abs_output_dir, f)
                        logger.log(f"Moving {full} -> {dest}")
                        shutil.move(full, dest)
                        actual_file = dest
                        break
                except Exception as e:
                    logger.error(f"CWD check error: {e}")

            # Final result
            if actual_file and os.path.exists(actual_file):
                size = os.path.getsize(actual_file)
                logger.log(f"========== SUCCESS ==========")
                logger.log(f"File: {actual_file}")
                logger.log(f"Size: {size / 1024 / 1024:.2f} MB")

                return json.dumps({
                    'success': True,
                    'filename': actual_file,
                    'title': title,
                    'platform': platform,
                    'file_size': size,
                    'logs': logger.get_logs()
                })
            else:
                logger.error("========== FILE NOT FOUND ==========")
                return json.dumps({
                    'success': False,
                    'error': "Download completed but file not found",
                    'logs': logger.get_logs()
                })

    except Exception as e:
        logger.error(f"Exception: {e}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        return json.dumps({
            'success': False,
            'error': str(e),
            'logs': logger.get_logs()
        })


def validate_url(url):
    """Check if URL is a valid Instagram or YouTube URL"""
    platform = detect_platform(url)
    if platform == 'unknown':
        return json.dumps({
            'valid': False,
            'error': 'URL must be from Instagram or YouTube'
        })
    return json.dumps({
        'valid': True,
        'platform': platform
    })
