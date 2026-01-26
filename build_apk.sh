#!/bin/bash
# Build script for Video Downloader APK
# Run this after install_dependencies.sh

set -e

cd /home/mac/android_downloader

echo "=== Building Video Downloader APK ==="

# Source environment variables
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/mac/Android/Sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

echo "[1/3] Cleaning previous build..."
./gradlew clean 2>/dev/null || true

echo "[2/3] Building debug APK..."
./gradlew assembleDebug --stacktrace

echo "[3/3] Build complete!"
echo ""
echo "=== APK Location ==="
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    ls -lh "$APK_PATH"
    echo ""
    echo "Copy to your device: adb install $APK_PATH"
else
    echo "APK not found. Check build output for errors."
fi

echo ""
echo "To build a release APK, run: ./gradlew assembleRelease"
