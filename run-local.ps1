# PowerShell script to load .env and run the Spring Boot app
# Usage (PowerShell):
#   ./run-local.ps1

Set-StrictMode -Version Latest

$envFile = Join-Path -Path (Get-Location) -ChildPath '.env'
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if (-not [string]::IsNullOrWhiteSpace($line) -and -not $line.StartsWith('#')) {
            $parts = $line -split '=', 2
            if ($parts.Length -eq 2) {
                $key = $parts[0].Trim()
                $val = $parts[1].Trim().Trim("'\"")
                # set in current process environment
                $env:$key = $val
            }
        }
    }
}

# Validate required vars
if (-not $env:POSTGRES_USER) { Write-Error 'POSTGRES_USER not set'; exit 1 }
if (-not $env:POSTGRES_PASSWORD) { Write-Error 'POSTGRES_PASSWORD not set'; exit 1 }
if (-not $env:POSTGRES_DB) { Write-Error 'POSTGRES_DB not set'; exit 1 }
if (-not $env:JWT_SECRET_KEY) { Write-Error 'JWT_SECRET_KEY not set'; exit 1 }

Write-Host "Starting application with Postgres DB='$($env:POSTGRES_DB)' user='$($env:POSTGRES_USER)'"

if ($IsWindows) {
    & .\mvnw.cmd spring-boot:run
} else {
    & ./mvnw spring-boot:run
}

