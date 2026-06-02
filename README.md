# MoodDiary MVP

## О чем проект
`MoodDiary` — это дневник эмоций, в котором пользователь ежедневно фиксирует настроение, энергию, продуктивность, уровень стресса, заметки, теги/симптомы и (опционально) фотографии. Данные используются для просмотра динамики и базовой аналитики.

## Актуальные продуктовые требования

- После авторизации пользователь попадает на страницу **Новая запись** (`/diary/entry/new`).
- По умолчанию используется **темная тема**; выбор темы хранится в `localStorage`.
- В пользовательском интерфейсе записи нет полей **качество сна** и **день завершен**. Legacy-поля API/БД сохраняются только для совместимости.
- Запись можно редактировать в течение **трех календарных дней** с даты записи; это правило также оставляет время на дозагрузку фото.
- Теги и симптомы выбираются прямо в карточке создания/редактирования записи, а управление справочниками находится в **Настройках**.
- В Истории нужны фильтры по периоду и симптомам; фильтр по тегам не используется.
- Аналитика строится по доступным записям за период и показывает настроение, энергию, продуктивность, стресс-корреляцию, динамику и частоты тегов.

## Архитектура
Используется гексагональная (ports & adapters) архитектура с обязательными слоями:

1. `domain` — доменные сущности и правила (валидации, инварианты).
2. `application` — use-cases и порты (интерфейсы) для внешних зависимостей.
3. `infrastructure` — реализация инфраструктуры: persistence, интеграции, outbox и т.д.
4. `adapter` — адаптеры к внешнему миру: REST контроллеры, маппинг HTTP/DTo, обработка запросов/ответов, security.

## Структура проекта
Backend:

- `src/main/java` — Java код
  - `.../domain` — доменная модель
  - `.../application` — use-cases и порты
  - `.../infrastructure` — persistence/интеграции
  - `.../adapter` — web/API адаптеры
- `src/main/resources/application.yml` — конфигурация (все параметры)
- `src/main/resources/db/changelog` — Liquibase changelog

Общие:

- `sql` — SQL-скрипты для создания БД и пользователей
- `devops` — devops скрипты (helm/k8s/gitlab-ci и т.д.)

Frontend (`src/main/web`):

- `package.json` / **`package-lock.json`** — зависимости; lockfile **коммитится** в репозиторий для воспроизводимых сборок и `npm ci` в CI.

## Правила разработки
- Код пишется production-ready: четкие границы слоев, без pet-подходов.
- Безопасность и аудит — обязательны (аудитируются все действия пользователя).
- Логирование:
  - логи на английском языке
  - `INFO` — только ключевые технические события
  - `DEBUG` — детали
  - не логировать чувствительные данные на `INFO`
- Все изменения схемы БД только через Liquibase.
- `Hibernate ddl-auto: validate`.
- Для каждого написанного кода — unit-тесты (JUnit5 + Mockito).
- Если используется сервис: сначала интерфейс, затем реализация в подпакете `impl`.
- Каркас и конфигурация должны соответствовать целевому стеку (Spring Boot, JPA, PostgreSQL, Liquibase, Keycloak).

## Запуск Backend
Требования:
- Java 17
- PostgreSQL

### PostgreSQL в Docker (локально)
Параметры совпадают с дефолтами в `application.yml` (`DB_NAME` / `DB_USER` / `DB_PASSWORD`).

**bash / Git Bash:**
```bash
docker run -d \
  --name mooddiary-db \
  -e POSTGRES_DB=mooddiary \
  -e POSTGRES_USER=mooddiary \
  -e POSTGRES_PASSWORD=mooddiary \
  -p 5432:5432 \
  postgres:15
```

**PowerShell (одна строка):**
```powershell
docker run -d --name mooddiary-db -e POSTGRES_DB=mooddiary -e POSTGRES_USER=mooddiary -e POSTGRES_PASSWORD=mooddiary -p 5432:5432 postgres:15
```

Если контейнер с таким именем уже есть: `docker start mooddiary-db` или удалите старый: `docker rm -f mooddiary-db`, затем создайте снова.

### Локальный запуск в Docker (PostgreSQL + Backend)
```bash
docker compose up -d --build
```

Сервисы:
- **PostgreSQL** — порт 5432
- **Backend** — http://localhost:8080

Frontend запускается отдельно (см. ниже). Для отладки локальный `docker-compose.yml` включает dev-auth без Keycloak: пользователь **`user` / `user`**.

Если нужен Keycloak локально:
```bash
docker compose --profile keycloak up -d --build
```

Keycloak: http://localhost:8180 (admin/admin), realm `mooddiary`, тестовый пользователь `test/test`.

Пример запуска (без Docker):
- `gradle bootRun`

Настройки:
- все параметры задаются в `application.yml` через env-переменные (см. файл).

## Запуск Frontend
Frontend размещен в `src/main/web` и собирается отдельно от backend.

План запуска (после клонирования / в CI):
- `cd src/main/web`
- **`npm ci`** — ставит зависимости строго по `package-lock.json` (рекомендуется).

Локально при **изменении зависимостей** в `package.json`:
- `npm install` — обновит `node_modules` и `package-lock.json`; закоммитьте обновлённый lockfile.

Далее:
- `npm run dev` — обычно **http://localhost:5173/**

В `vite.config.ts` настроен **proxy** `/api` → `http://localhost:8080`, поэтому для локальной разработки **`VITE_API_BASE_URL` не обязателен**, если backend слушает порт `8080`.

### Keycloak (SPA)
По умолчанию используется Keycloak из docker-compose: `http://host.docker.internal:8180/realms/mooddiary`, клиент `mooddiary-web`.

Клиент: **public**, flow **Standard** (Authorization Code) + **PKCE** (`keycloak-js`).

Переменные окружения (опционально, для переопределения):
- `VITE_KEYCLOAK_ISSUER_URI` — issuer realm (по умолчанию `http://host.docker.internal:8180/realms/mooddiary`).
- `VITE_KEYCLOAK_CLIENT_ID` — id клиента (по умолчанию `mooddiary-web`).

Realm `mooddiary` уже содержит настроенный клиент с redirect URIs для localhost:5173 и host.docker.internal:5173.

Файл `public/silent-check-sso.html` нужен для silent `check-sso`.

## Запуск тестов
- Backend: `gradle test`
- Frontend: `cd src/main/web && npm ci && npm test` (или `npm test`, если `node_modules` уже установлены)

## Деплой на VPS

**Упрощённый MVP** (один пользователь, без Keycloak, прямой IP/домен, без Cloudflare): [deploy/MVP.md](deploy/MVP.md), скрипт `deploy/deploy.mvp.sh`, `deploy/docker-compose.mvp.yml`.

Полный прод с Keycloak:

- Инструкция деплоя: [deploy/DEPLOY.md](deploy/DEPLOY.md)
- CI/CD процесс с ручным production approval: [docs/CI-CD.md](docs/CI-CD.md)

## Liquibase
Liquibase использует master changelog:
- `src/main/resources/db/changelog/db.changelog-master.yaml`

Все таблицы MVP создаются в `0001-init.yaml`.

## Audit (аудит)
Аудит фиксирует:
- кто выполнил действие (user)
- тип действия
- сущность и id сущности
- дата/время
- ip адрес (с учетом proxy headers)
- forwarded headers (цепочка/источники)
- user-agent

Для корректного определения client IP учитываются:
- `CF-Connecting-IP`
- `X-Forwarded-For`
- `X-Real-IP`
- fallback на `request.getRemoteAddr()`

Данные сохраняются в таблицу `audit_log`.

## Темы UI
UI поддерживает `light` и `dark`.
Текущая тема хранится в `localStorage`.

