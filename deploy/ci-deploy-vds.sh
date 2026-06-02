#!/usr/bin/env bash
set -euo pipefail

: "${VDS_HOST:?Set VDS_HOST in CI variables}"
: "${VDS_USER:?Set VDS_USER in CI variables}"
: "${VDS_APP_PATH:?Set VDS_APP_PATH in CI variables}"
: "${VDS_DOMAIN:?Set VDS_DOMAIN in CI variables}"
: "${CI_COMMIT_SHA:?CI_COMMIT_SHA is required}"

REMOTE="${VDS_USER}@${VDS_HOST}"

ssh "${REMOTE}" "set -euo pipefail
  cd '${VDS_APP_PATH}'
  git fetch --all --tags --prune
  git checkout --detach '${CI_COMMIT_SHA}'
  cd deploy
  test -f .env
  ./deploy.sh
  docker compose -f docker-compose.prod.yml ps
  curl -fsS -I 'https://${VDS_DOMAIN}/diary/login' >/dev/null
"

echo "Deployed ${CI_COMMIT_SHA} to https://${VDS_DOMAIN}"
