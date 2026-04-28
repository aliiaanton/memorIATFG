$env:APP_STORE = "supabase"
$env:APP_SECURITY_ENABLED = "false"
$env:APP_ERROR_DETAILS = "true"

# Use Supabase Dashboard > Connect > Session pooler.
# Replace <project-id>, <region> and <password>.
# Example format:
# postgresql://postgres.<project-id>:<password>@aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
$env:SUPABASE_DB_URL = "postgresql://postgres.<project-id>:<password>@aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require"

$env:AI_SERVICE_BASE_URL = "http://localhost:8000"

Set-Location "$PSScriptRoot\..\backend"
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
