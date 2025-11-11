@echo off
REM Simple runner for Windows (cmd.exe). This script does NOT persist env vars.
REM It attempts to load simple KEY=VALUE lines from .env into the process environment.
if exist .env (
  for /f "usebackq tokens=1* delims==" %%A in (".env") do (
    rem skip comments and empty keys
    if not "%%A"=="" (
      set "line=%%A=%%B"
      set "key=%%A"
      set "val=%%B"
      rem remove surrounding double quotes if present
      if "%val:~0,1%"=="\"" (
        set "val=%val:~1%"
        if "%val:~-1%"=="\"" set "val=%val:~0,-1%"
      )
      set "%%A=%val%"
    )
  )
)

echo Starting application with environment variables from .env (if any).
mvnw.cmd spring-boot:run
pause

