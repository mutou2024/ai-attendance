#!/usr/bin/env bash
set -euo pipefail

# Start the application locally (no Docker)
# Usage: ./scripts/start_local.sh
# If SKIP_FRONTEND=1 is set in env, frontend build/serve will be skipped.

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Building backend..."
if [ -f "backend/pom.xml" ] && command -v mvn >/dev/null 2>&1; then
  mvn -f backend/pom.xml -DskipTests package
else
  echo "mvn not found or backend/pom.xml missing. Please install Maven or build backend manually." >&2
fi

# Find built jar
JAR_FILE=$(ls backend/target/*-SNAPSHOT.jar 2>/dev/null || true)
if [ -z "$JAR_FILE" ]; then
  JAR_FILE=$(ls backend/target/*.jar 2>/dev/null | head -n1 || true)
fi

if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
  echo "Starting backend jar: $JAR_FILE"
  nohup java -jar "$JAR_FILE" --spring.config.location=classpath:/application.properties,./backend/src/main/resources/application.properties --server.port=8080 > backend.log 2>&1 &
  echo $! > backend.pid
  echo "Backend started (pid $(cat backend.pid)), logs -> backend.log"
else
  echo "No backend jar found; make sure mvn built the project or run it via your IDE." >&2
fi

if [ "${SKIP_FRONTEND:-0}" != "1" ]; then
  echo "Building frontend..."
  if [ -f "frontend/package.json" ] && command -v npm >/dev/null 2>&1; then
    (cd frontend && npm install)
    (cd frontend && npm run build)
  else
    echo "npm not found or frontend/package.json missing. Please install Node/npm or build frontend manually." >&2
  fi

  echo "Serving frontend (port 80)"
  # Try npx serve (preferred)
  if command -v npx >/dev/null 2>&1; then
    nohup npx serve -s frontend/dist -l 80 > frontend.log 2>&1 &
    echo $! > frontend.pid
    echo "Frontend served by 'serve' (pid $(cat frontend.pid)), logs -> frontend.log"
  else
    # Fallback to python http.server (requires root to bind port 80)
    nohup python3 -m http.server 80 --directory frontend/dist > frontend.log 2>&1 &
    echo $! > frontend.pid
    echo "Frontend served by python http.server (pid $(cat frontend.pid)), logs -> frontend.log"
  fi
else
  echo "SKIP_FRONTEND=1 set; skipping frontend build and serve."
fi

echo "Start script finished."