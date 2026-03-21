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
DOMAIN=mooddiary.yourdomain.com
PROTOCOL=https
DB_PASSWORD=secure_password_here
KEYCLOAK_ADMIN_PASSWORD=admin_secure_password
KEYCLOAK_DB_PASSWORD=keycloak_db_password
TELEGRAM_BOT_TOKEN=optional_for_telegram_summary
EOF
```

Для первого деплоя без SSL используйте `PROTOCOL=http`.

### 3. Запуск

```bash
chmod +x deploy.sh
source .env && ./deploy.sh
```

Или вручную:

```bash
export DOMAIN=mooddiary.yourdomain.com
export DB_PASSWORD=your_db_password
export KEYCLOAK_ADMIN_PASSWORD=your_keycloak_admin_password
export KEYCLOAK_DB_PASSWORD=your_keycloak_db_password

envsubst '${DOMAIN}' < keycloak-realm.json.template > keycloak-realm.json
docker-compose -f docker-compose.prod.yml up -d --build
```

### 4. Первый вход

1. Откройте `https://yourdomain.com` (или `http://` если SSL ещё не настроен)
2. Нажмите «Войти» — перенаправит на Keycloak
3. Создайте пользователя в Keycloak Admin: `https://yourdomain.com/admin` (логин: admin, пароль из `KEYCLOAK_ADMIN_PASSWORD`)
4. В Realm `mooddiary` → Users → Add user

## HTTPS (Let's Encrypt)

1. Установите certbot: `apt install certbot`
2. Получите сертификат: `certbot certonly --standalone -d mooddiary.yourdomain.com`
3. Создайте папку и скопируйте сертификаты:

```bash
mkdir -p deploy/ssl
cp /etc/letsencrypt/live/mooddiary.yourdomain.com/fullchain.pem deploy/ssl/
cp /etc/letsencrypt/live/mooddiary.yourdomain.com/privkey.pem deploy/ssl/
```

4. Раскомментируйте HTTPS-блок в `deploy/nginx.conf` и укажите пути к сертификатам
5. Перезапустите: `docker-compose -f deploy/docker-compose.prod.yml restart nginx`

## Переменные окружения

| Переменная | Обязательно | Описание |
|------------|-------------|----------|
| DOMAIN | Да | Домен приложения (например mooddiary.example.com) |
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
source .env && docker-compose -f docker-compose.prod.yml up -d --build
```

## Резервное копирование

```bash
# БД mooddiary
docker exec mooddiary-db pg_dump -U mooddiary mooddiary > backup_mooddiary.sql

# Фотографии
docker run --rm -v mooddiary_mooddiary_photos:/data -v $(pwd):/backup alpine tar czf /backup/photos_backup.tar.gz -C /data .
```
