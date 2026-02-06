# Run Zingy Web App on http://localhost:4321
# PowerShell script for Windows

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Create venv if it doesn't exist
if (-not (Test-Path "venv")) {
    Write-Host "Creating virtual environment..."
    python -m venv venv
}

# Activate venv
& ".\venv\Scripts\Activate.ps1"

# Install requirements
Write-Host "Installing dependencies..."
pip install -q -r requirements.txt

Write-Host ""
Write-Host "Starting Zingy on http://localhost:4321"
Write-Host "Press Ctrl+C to stop"
Write-Host ""

python app.py
