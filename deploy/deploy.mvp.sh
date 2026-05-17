#!/bin/bash
# MoodDiary MVP — один фиксированный логин/пароль, без Keycloak, прямой VPS.
set -e
cd "$(dirname "$0")"

[[ -f .env ]] && set -a && source .env && set +a

: "${DOMAIN:?Укажите DOMAIN (например diary.example.com или IP)}"
: "${DB_PASSWORD:?Укажите DB_PASSWORD}"

export MVP_AUTH_USERNAME="${MVP_AUTH_USERNAME:-mvp}"
export MVP_AUTH_PASSWORD="${MVP_AUTH_PASSWORD:-mvp}"
export MVP_AUTH_SUBJECT="${MVP_AUTH_SUBJECT:-$MVP_AUTH_USERNAME}"

export DOMAIN
command -v envsubst >/dev/null || { echo "Нужен envsubst (apt install gettext-base)"; exit 1; }

envsubst '${DOMAIN}' < nginx.mvp.conf.template > nginx.mvp.runtime.conf

echo "Запуск MVP: домен $DOMAIN, логин $MVP_AUTH_USERNAME (задайте MVP_AUTH_PASSWORD в .env при необходимости)"
docker compose -f docker-compose.mvp.yml up -d --build

echo ""
echo "Откройте http://${DOMAIN}/ (или https после своего SSL). Вход: $MVP_AUTH_USERNAME / $MVP_AUTH_PASSWORD"
