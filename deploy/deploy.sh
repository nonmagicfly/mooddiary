#!/bin/bash
# MoodDiary — деплой на VPS
# Использование: ./deploy.sh (загружает .env) или DOMAIN=... DB_PASSWORD=... ./deploy.sh

set -e

cd "$(dirname "$0")"

if docker compose version >/dev/null 2>&1; then
  DOCKER_COMPOSE="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  DOCKER_COMPOSE="docker-compose"
else
  echo "Docker Compose is required (docker compose or docker-compose)"
  exit 1
fi

# Загрузка .env если есть
[[ -f .env ]] && set -a && source .env && set +a

# Проверка переменных
: "${DOMAIN:?Укажите DOMAIN (например: mooddiary.example.com)}"
: "${DB_PASSWORD:?Укажите DB_PASSWORD}"
: "${KEYCLOAK_ADMIN_PASSWORD:?Укажите KEYCLOAK_ADMIN_PASSWORD}"
: "${KEYCLOAK_DB_PASSWORD:?Укажите KEYCLOAK_DB_PASSWORD}"

# Публичный хост Keycloak (по умолчанию совпадает с DOMAIN — один сайт как раньше)
export AUTH_DOMAIN="${AUTH_DOMAIN:-$DOMAIN}"

# Генерация realm и nginx с подстановкой доменов
export DOMAIN
export PROTOCOL=${PROTOCOL:-https}
export SSL_CERT_PATH=${SSL_CERT_PATH:-/etc/nginx/ssl/fullchain.pem}
export SSL_CERT_KEY_PATH=${SSL_CERT_KEY_PATH:-/etc/nginx/ssl/privkey.pem}
export AUTH_DOMAIN
envsubst '${DOMAIN} ${PROTOCOL}' < keycloak-realm.json.template > keycloak-realm.json
envsubst '${DOMAIN} ${AUTH_DOMAIN} ${SSL_CERT_PATH} ${SSL_CERT_KEY_PATH}' < nginx.conf.template > nginx.runtime.conf

VITE_KEYCLOAK_ISSUER_URI="${PROTOCOL}://${AUTH_DOMAIN}/realms/mooddiary"
echo "Деплой MoodDiary: приложение ${PROTOCOL}://${DOMAIN}, Keycloak ${VITE_KEYCLOAK_ISSUER_URI}..."
${DOCKER_COMPOSE} -f docker-compose.prod.yml up -d --build

echo ""
echo "Готово! Приложение: ${PROTOCOL}://${DOMAIN}"
echo "Keycloak Admin: ${PROTOCOL}://${AUTH_DOMAIN}/admin (логин: admin)"
echo ""
[[ "$PROTOCOL" == "http" ]] && echo "Для HTTPS настройте SSL (Let's Encrypt) и допишите listen 443 в nginx.conf.template (оба server { }), затем перегенерируйте nginx.runtime.conf"
