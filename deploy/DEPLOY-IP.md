# Деплой MoodDiary по IP 89.124.83.7

## 1. Подключение к серверу

```bash
ssh root@89.124.83.7
```

## 2. Установка Docker

```bash
apt update && apt install -y docker.io docker-compose-plugin git
systemctl enable docker && systemctl start docker
```

## 3. Клонирование проекта

```bash
cd /root
git clone https://github.com/nonmagicfly/mooddiary.git
cd mooddiary/deploy
```

## 4. Создание .env

```bash
cp env.ip.example .env
nano .env
```

Замените `замените_на_надёжный_пароль` на свои пароли. Сохраните: `Ctrl+O`, `Enter`, `Ctrl+X`.

## 5. Запуск

```bash
chmod +x deploy.sh
./deploy.sh
```

Сборка займёт 5–10 минут.

## 6. Настройка firewall (UFW)

Закрываем всё лишнее, оставляем только нужные порты:

```bash
# Сброс правил
ufw --force reset

# Политика по умолчанию
ufw default deny incoming
ufw default allow outgoing

# Разрешаем только нужные порты
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS (на будущее)

# Включаем firewall
ufw --force enable

# Проверка
ufw status verbose
```

**Важно:** не отключайте SSH (22) до проверки доступа, иначе можно потерять доступ к серверу.

## 7. Готово

- **Приложение:** http://89.124.83.7
- **Keycloak Admin:** http://89.124.83.7/admin (логин: admin)

## 8. Создание пользователя

1. Откройте http://89.124.83.7/admin
2. Войдите (admin + пароль из KEYCLOAK_ADMIN_PASSWORD)
3. Realm: `mooddiary` → Users → Add user
4. Укажите username → Credentials → Set password
