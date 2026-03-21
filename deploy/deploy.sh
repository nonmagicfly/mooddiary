#!/bin/bash
# MoodDiary — деплой на VPS
# Использование: ./deploy.sh (загружает .env) или DOMAIN=... DB_PASSWORD=... ./deploy.sh

set -e

cd "$(dirname "$0")"

# Загрузка .env если есть
[[ -f .env ]] && set -a && source .env && set +a

# Проверка переменных
: "${DOMAIN:?Укажите DOMAIN (например: mooddiary.example.com)}"
: "${DB_PASSWORD:?Укажите DB_PASSWORD}"
: "${KEYCLOAK_ADMIN_PASSWORD:?Укажите KEYCLOAK_ADMIN_PASSWORD}"
: "${KEYCLOAK_DB_PASSWORD:?Укажите KEYCLOAK_DB_PASSWORD}"

# Генерация realm с подстановкой домена
export DOMAIN
export PROTOCOL=${PROTOCOL:-https}
envsubst '${DOMAIN} ${PROTOCOL}' < keycloak-realm.json.template > keycloak-realm.json

echo "Деплой MoodDiary на $DOMAIN..."
if docker compose version &>/dev/null; then
  docker compose -f docker-compose.prod.yml up -d --build
else
  docker-compose -f docker-compose.prod.yml up -d --build
fi

echo ""
echo "Готово! Приложение: ${PROTOCOL}://${DOMAIN}"
echo "Keycloak Admin: ${PROTOCOL}://${DOMAIN}/admin (логин: admin)"
echo ""
[[ "$PROTOCOL" == "http" ]] && echo "Для HTTPS настройте SSL (Let's Encrypt) и обновите nginx.conf"
