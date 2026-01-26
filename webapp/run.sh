#!/bin/bash
# Run the Video Downloader Web App on port 4321

cd "$(dirname "$0")"

# Install requirements if needed
if ! python3 -c "import flask" 2>/dev/null; then
    echo "Installing dependencies..."
    pip3 install -r requirements.txt
fi

echo "Starting Video Downloader Web App on http://localhost:4321"
python3 app.py
