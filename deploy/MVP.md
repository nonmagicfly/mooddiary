# MoodDiary MVP (упрощённый деплой)

Версия для быстрого запуска на **одном VPS** без Keycloak и без Cloudflare.

## Отличия от полного prod

| | MVP | Полный `docker-compose.prod.yml` |
|--|-----|----------------------------------|
| Вход | Один пользователь, логин/пароль из `.env` (по умолчанию `mvp`/`mvp`) | Keycloak |
| Cloudflare | Не нужен | По желанию |
| Домен | Один (`DOMAIN`): можно FQDN или IP | До двух хостов (`DOMAIN` + `AUTH_DOMAIN`) |

Бэкенд принимает API только с **Bearer-токеном**, который появляется после успешного входа в форме (тот же механизм, что локальный dev-auth).

## Шаги

1. **VPS** с Docker и Docker Compose v2.
2. **DNS** (если есть домен): A-запись `DOMAIN` → IP сервера. Можно без домена: укажите в `.env` сам IP.
3. **Файрвол**: откройте порт **80** (или `MVP_HTTP_PORT`, если меняете).
4. На сервере:
   ```bash
   cd /path/to/mooddiary/deploy
   cp env.mvp.example .env
   nano .env   # DOMAIN, DB_PASSWORD; при желании смените MVP_AUTH_*
   chmod +x deploy.mvp.sh
   ./deploy.mvp.sh
   ```
5. Откройте `http://ВАШ_DOMAIN/` и войдите с учётными из `.env`.

## HTTPS

В этой compose-сборке nginx слушает только **80**. Для HTTPS поставьте certbot на хосте или перед VPS свой reverse-proxy — это отдельная настройка.

## Полный прод

См. [DEPLOY.md](./DEPLOY.md) и `docker-compose.prod.yml`.
