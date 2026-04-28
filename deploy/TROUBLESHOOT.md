# Диагностика: сайт не открывается по IP

Выполните на сервере по порядку:

## 1. Контейнеры запущены?

```bash
cd /root/mooddiary/deploy
docker-compose -f docker-compose.prod.yml ps
```

Все 4 сервиса (postgres, keycloak, backend, nginx) должны быть в статусе `running`.

## 2. Nginx слушает порт 80?

```bash
ss -tlnp | grep 80
# или
netstat -tlnp | grep 80
```

Должна быть строка `*:80` или `0.0.0.0:80`.

## 3. Firewall (UFW)

```bash
ufw status
```

Порт 80 должен быть `ALLOW`.

## 4. Локальный тест на сервере

```bash
curl -I http://127.0.0.1
curl -I http://localhost
```

Если отвечает — nginx работает, проблема в сети или firewall.

## 5. Firewall провайдера

У многих хостингов (Timeweb, Selectel, REG.RU и т.д.) есть **дополнительный firewall в панели управления**. Проверьте, что порт 80 открыт в панели VPS.

## 6. Логи контейнеров

```bash
docker-compose -f docker-compose.prod.yml logs nginx
docker-compose -f docker-compose.prod.yml logs backend
docker-compose -f docker-compose.prod.yml logs keycloak
```

## 7. Перезапуск

```bash
cd /root/mooddiary/deploy
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

---

# Диагностика: Keycloak не работает

## 1. Проверка доступности Keycloak

```bash
# На сервере — Keycloak должен отвечать
curl -I http://127.0.0.1/realms/mooddiary/.well-known/openid-configuration
curl -I http://127.0.0.1/admin/
```

Ожидается `HTTP/1.1 200` или `302`.

## 2. Проверка .env

```bash
cd /root/mooddiary/deploy
cat .env | grep -E 'DOMAIN|PROTOCOL|KEYCLOAK'
```

Для работы по IP: `DOMAIN=89.124.83.7`, `PROTOCOL=http`.

## 3. Проверка keycloak-realm.json

```bash
cat keycloak-realm.json | grep -A2 redirectUris
```

Должны быть `http://89.124.83.7/*` и `http://89.124.83.7/diary/login` (если DOMAIN=89.124.83.7).

## 4. Пересоздание Keycloak (полная переустановка)

```bash
cd /root/mooddiary/deploy
docker stop mooddiary-keycloak
docker rm mooddiary-keycloak
# Перегенерировать realm
source .env
envsubst '${DOMAIN} ${PROTOCOL}' < keycloak-realm.json.template > keycloak-realm.json
docker-compose -f docker-compose.prod.yml up -d keycloak
```

## 5. Логи Keycloak

```bash
docker-compose -f docker-compose.prod.yml logs --tail 100 keycloak
```

## 6. Браузер

- Откройте http://89.124.83.7/admin в **режиме инкогнито** (без кэша и cookies).
- Логин: `admin`, пароль — из `KEYCLOAK_ADMIN_PASSWORD` в `.env`.
