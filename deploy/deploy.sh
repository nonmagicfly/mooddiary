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
docker compose -f docker-compose.prod.yml up -d --build

echo ""
echo "Готово! Приложение: http://${DOMAIN}"
echo "Keycloak Admin: https://${DOMAIN}/admin (логин: admin)"
echo ""
echo "Для HTTPS настройте SSL (Let's Encrypt) и обновите nginx.conf"
