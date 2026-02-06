#!/bin/bash
# Run Zingy Web App on http://localhost:4321

cd "$(dirname "$0")"

# Create venv if it doesn't exist
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate venv
source venv/bin/activate

# Install requirements if needed
pip install -q -r requirements.txt

echo "Starting Zingy on http://localhost:4321"
python3 app.py
