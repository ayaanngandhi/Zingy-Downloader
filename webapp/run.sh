#!/bin/bash
# Run Zingy Web App
# Frontend: http://localhost:4321
# Backend API: http://localhost:4322

cd "$(dirname "$0")"

# Create venv if it doesn't exist
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate venv
source venv/bin/activate

# Install requirements if needed
if ! python3 -c "import flask" 2>/dev/null || ! python3 -c "import flask_cors" 2>/dev/null; then
    echo "Installing dependencies..."
    pip install -r requirements.txt
fi

echo "Starting Zingy..."
echo "  Frontend: http://localhost:4321"
echo "  Backend:  http://localhost:4322"
echo ""

# Start backend in background
python3 app.py &
BACKEND_PID=$!

# Wait for backend to start
sleep 2

# Start frontend server (serving templates/index.html)
cd templates
python3 -m http.server 4321 &
FRONTEND_PID=$!

echo "Press Ctrl+C to stop"

# Handle cleanup
trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT TERM

# Wait for either to exit
wait
