# Деплой MoodDiary на VPS

## Требования

- Docker и Docker Compose на VPS
- Домен, направленный на IP сервера (A-запись)
- Открытые порты 80 и 443 (firewall)

## Быстрый старт

### 1. Подготовка

Скопируйте проект на VPS и перейдите в папку `deploy`:

```bash
cd /path/to/mooddiary/deploy
```

### 2. Создайте `.env` с секретами

```bash
cat > .env << 'EOF'
DOMAIN=app.yourdomain.com
AUTH_DOMAIN=auth.yourdomain.com
PROTOCOL=https
DB_PASSWORD=secure_password_here
KEYCLOAK_ADMIN_PASSWORD=admin_secure_password
KEYCLOAK_DB_PASSWORD=keycloak_db_password
TELEGRAM_BOT_TOKEN=optional_for_telegram_summary
EOF
```

`AUTH_DOMAIN` можно не задавать — тогда Keycloak остаётся на том же хосте, что и приложение (как раньше). Для отдельного хоста Keycloak задайте, например: `AUTH_DOMAIN=auth.nonmagicfly.ru`, а `DOMAIN` — хост SPA/API (например `diary.nonmagicfly.ru`).

Для первого деплоя без SSL используйте `PROTOCOL=http`.

### 3. Запуск

```bash
chmod +x deploy.sh
source .env && ./deploy.sh
```

Или вручную:

```bash
export DOMAIN=mooddiary.yourdomain.com
export AUTH_DOMAIN="${AUTH_DOMAIN:-$DOMAIN}"
export PROTOCOL="${PROTOCOL:-https}"
export DB_PASSWORD=your_db_password
export KEYCLOAK_ADMIN_PASSWORD=your_keycloak_admin_password
export KEYCLOAK_DB_PASSWORD=your_keycloak_db_password

envsubst '${DOMAIN} ${PROTOCOL}' < keycloak-realm.json.template > keycloak-realm.json
envsubst '${DOMAIN} ${AUTH_DOMAIN}' < nginx.conf.template > nginx.runtime.conf
docker-compose -f docker-compose.prod.yml up -d --build
```

### 4. Первый вход

1. Откройте `https://yourdomain.com` (или `http://` если SSL ещё не настроен)
2. Нажмите «Войти» — перенаправит на Keycloak
3. Создайте пользователя в Keycloak Admin: `https://<AUTH_DOMAIN>/admin` (если задан `AUTH_DOMAIN`), иначе `https://<DOMAIN>/admin` (логин: admin, пароль из `KEYCLOAK_ADMIN_PASSWORD`)
4. В Realm `mooddiary` → Users → Add user

## HTTPS (GlobalSign / внешний сертификат)

1. Подготовьте сертификат и ключ для nginx.

По умолчанию compose монтирует `deploy/ssl` в контейнер как `/etc/nginx/ssl`.
Положите туда:

```
deploy/ssl/fullchain.pem
deploy/ssl/privkey.pem
```

`fullchain.pem` должен содержать серверный сертификат GlobalSign и промежуточные сертификаты в правильном порядке.

Если сертификаты лежат в другом каталоге на VPS, задайте в `.env`:

```bash
SSL_DIR=/path/to/cert-dir
SSL_CERT_PATH=/etc/nginx/ssl/fullchain.pem
SSL_CERT_KEY_PATH=/etc/nginx/ssl/privkey.pem
```

2. Перегенерируйте production-конфиги и перезапустите nginx:

```bash
source .env
./deploy.sh
docker compose -f docker-compose.prod.yml restart nginx
```

## Переменные окружения

| Переменная | Обязательно | Описание |
|------------|-------------|----------|
| DOMAIN | Да | Публичный домен SPA и API (например diary.example.com) |
| AUTH_DOMAIN | Нет | Публичный домен Keycloak (например auth.example.com); по умолчанию совпадает с DOMAIN |
| DB_PASSWORD | Да | Пароль PostgreSQL для БД mooddiary |
| KEYCLOAK_ADMIN_PASSWORD | Да | Пароль админа Keycloak |
| KEYCLOAK_DB_PASSWORD | Да | Пароль пользователя keycloak в PostgreSQL |
| TELEGRAM_BOT_TOKEN | Нет | Токен бота для отправки саммари в Telegram |

## Порты

- **80** — HTTP (Nginx)
- **443** — HTTPS (Nginx, после настройки SSL)

PostgreSQL, Keycloak и Backend работают во внутренней сети Docker.

## Обновление

```bash
cd deploy
git pull  # или скопируйте обновлённые файлы
source .env && ./deploy.sh
```
(или вручную снова выполните `envsubst` для `keycloak-realm.json` и `nginx.runtime.conf`, как в разделе «Или вручную».)

## Резервное копирование

```bash
# БД mooddiary
docker exec mooddiary-db pg_dump -U mooddiary mooddiary > backup_mooddiary.sql

# Фотографии
docker run --rm -v mooddiary_mooddiary_photos:/data -v $(pwd):/backup alpine tar czf /backup/photos_backup.tar.gz -C /data .
```
