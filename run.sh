#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
  echo "Missing .env file in $(pwd)." >&2
  exit 1
fi

set -a
# shellcheck source=/dev/null
source .env
set +a

exec ./mvnw spring-boot:run  "$@"