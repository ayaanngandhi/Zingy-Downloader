# Zingy

Download videos from any platform - Android app + Web app

## Features
- Download from 1000+ sites (YouTube, Instagram, TikTok, Twitter, etc.)
- Simple, clean UI
- Customizable download path
- Works offline (after download)

## Android App
Package: `dev.ayaanngandhi.zingy`
- Download the APK from [Releases](../../releases)
- Requires Android 8.0+

## Web App
A Flask-based web interface for desktop use.

### Setup
```bash
cd webapp
pip install -r requirements.txt
python app.py
```
Then open http://localhost:4321

## Building from Source

### Android
```bash
./gradlew assembleRelease
```
APK will be at `app/build/outputs/apk/release/`

### Web
Just run `python webapp/app.py`

## License
MIT
