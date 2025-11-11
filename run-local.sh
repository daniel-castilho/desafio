#!/usr/bin/env bash
# Run the application locally using variables from .env (WSL / Linux / macOS)
# Usage: ./run-local.sh

set -euo pipefail

# Load .env if present
if [ -f ".env" ]; then
  # export variables defined in .env
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

# Ensure required env vars are set (fail fast with helpful message)
: "${POSTGRES_USER:?Need to set POSTGRES_USER (in .env or env)}"
: "${POSTGRES_PASSWORD:?Need to set POSTGRES_PASSWORD (in .env or env)}"
: "${POSTGRES_DB:?Need to set POSTGRES_DB (in .env or env)}"
: "${JWT_SECRET_KEY:?Need to set JWT_SECRET_KEY (in .env or env)}"

echo "Starting application with Postgres host='${POSTGRES_HOST:-localhost}' db='${POSTGRES_DB}' user='${POSTGRES_USER}'"

# Run the app
./mvnw spring-boot:run

