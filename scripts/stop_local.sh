#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [ -f backend.pid ]; then
  echo "Stopping backend pid $(cat backend.pid)"
  kill "$(cat backend.pid)" || true
  rm -f backend.pid
fi

if [ -f frontend.pid ]; then
  echo "Stopping frontend pid $(cat frontend.pid)"
  kill "$(cat frontend.pid)" || true
  rm -f frontend.pid
fi

echo "Stopped local services."