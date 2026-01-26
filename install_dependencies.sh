#!/bin/bash
# Video Downloader App - Dependency Installation Script
# Run this script with: sudo bash install_dependencies.sh

set -e

echo "=== Installing Video Downloader Dependencies ==="

# Update package list
echo "[1/5] Updating package list..."
apt-get update

# Install Java JDK 17
echo "[2/5] Installing Java JDK 17..."
apt-get install -y openjdk-17-jdk unzip wget curl

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> /home/mac/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /home/mac/.bashrc

# Create Android SDK directory
echo "[3/5] Setting up Android SDK..."
ANDROID_HOME=/home/mac/Android/Sdk
mkdir -p $ANDROID_HOME/cmdline-tools

# Download Android command line tools
cd /tmp
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q -o cmdline-tools.zip
mv cmdline-tools $ANDROID_HOME/cmdline-tools/latest

# Set Android environment variables
echo "export ANDROID_HOME=$ANDROID_HOME" >> /home/mac/.bashrc
echo "export PATH=\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH" >> /home/mac/.bashrc

# Fix ownership
chown -R mac:mac /home/mac/Android
chown mac:mac /home/mac/.bashrc

# Accept licenses and install SDK packages
echo "[4/5] Installing Android SDK packages..."
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
yes | sdkmanager --licenses > /dev/null 2>&1 || true
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

# Install pip and yt-dlp for testing
echo "[5/5] Installing yt-dlp..."
apt-get install -y python3-pip
pip3 install yt-dlp --break-system-packages || pip3 install yt-dlp

echo ""
echo "=== Installation Complete ==="
echo "Please run: source ~/.bashrc"
echo "Then verify with: java -version && sdkmanager --version"
