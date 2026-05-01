#!/usr/bin/env bash
# android-emulator-runner runs each "script" line as its own `sh -c`; keep logic in one file.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
chmod +x gradlew
for attempt in 1 2 3 4 5; do
  if ./gradlew --no-daemon connectedDebugAndroidTest --stacktrace; then
    exit 0
  fi
  echo "Gradle attempt ${attempt} failed (often transient). Retrying in 25s..."
  sleep 25
done
exit 1
