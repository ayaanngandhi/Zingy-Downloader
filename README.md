# Video Downloader App

An Android app that downloads videos from Instagram Reels and YouTube using yt-dlp.

## Features

- Download Instagram Reels by sharing/pasting link
- Download YouTube videos by sharing/pasting link
- Automatic organization:
  - Instagram videos saved to: `storage/emulated/0/Movies/Instagram/`
  - YouTube videos saved to: `storage/emulated/0/Movies/YouTube/`
- Simple, clean UI
- Download progress indicator

## Requirements

- Android 7.0 (API 24) or higher
- Internet connection
- Storage permission

## Tech Stack

- Kotlin
- Jetpack Compose for UI
- yt-dlp (via chaquopy Python integration)
- Coroutines for async operations

## Usage

1. Copy a video link from Instagram or YouTube
2. Open the app
3. Paste the link or use the share menu
4. Tap Download
5. Video will be saved to the appropriate Movies subfolder

## Building

1. Open project in Android Studio
2. Sync Gradle
3. Build and run on device/emulator

## License

MIT License
