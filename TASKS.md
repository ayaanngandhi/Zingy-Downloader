# Task List

## Project Setup
- [x] Create Android project with Kotlin and Jetpack Compose
- [x] Configure Gradle with required dependencies
- [x] Set up Chaquopy for Python/yt-dlp integration

## Core Functionality
- [x] Implement yt-dlp wrapper in Python
- [x] Create Kotlin bridge to call Python code
- [x] Implement URL detection (Instagram vs YouTube)
- [x] Implement video download logic
- [x] Handle download progress callbacks

## Storage
- [x] Request storage permissions
- [x] Create output directories if not exist
- [x] Save files to correct location based on source
- [x] Configurable save paths via settings

## UI
- [x] Create main screen with URL input field
- [x] Add paste from clipboard button
- [x] Add download button
- [x] Show download progress bar
- [x] Display success/error messages
- [x] Handle share intent from other apps
- [x] Settings panel for configurable paths

## Error Handling
- [x] Handle network errors
- [x] Handle invalid URLs
- [x] Handle storage permission denied
- [x] Handle download failures

## Build & Deploy
- [ ] Install dependencies (Java, Android SDK)
- [ ] Build debug APK
- [ ] Test on device

## How to Build

1. Install dependencies:
   ```bash
   sudo bash install_dependencies.sh
   source ~/.bashrc
   ```

2. Build APK:
   ```bash
   ./build_apk.sh
   ```

   Or manually:
   ```bash
   ./gradlew assembleDebug
   ```

3. APK location: `app/build/outputs/apk/debug/app-debug.apk`
