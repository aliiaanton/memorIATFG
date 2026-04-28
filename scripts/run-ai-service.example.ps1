# Replace <gemini-api-key> with your local key.
$env:GEMINI_API_KEY = "<gemini-api-key>"

Set-Location "$PSScriptRoot\..\ai-service"

if (Test-Path ".\.venv\Scripts\Activate.ps1") {
    . .\.venv\Scripts\Activate.ps1
}

uvicorn app.main:app --reload --port 8000
