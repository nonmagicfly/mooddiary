# MoodDiary — модель C4

Архитектурные представления по [C4 model](https://c4model.com/): **Context** → **Container** → **Component**. Уровень **Code** — см. `README.md` и пакеты `com.mooddiary.diary`.

## Как вставлять диаграммы в Mermaid Live / редактор

**Ошибка `Unexpected token '#'` / «не валидный JSON»** возникает, если в поле кода вставлен **весь этот `.md` файл** (он начинается с `#`) или в поле **JSON** вставлен текст не в формате JSON.

Что делать:

1. Копируйте **только** содержимое блока ` ```mermaid ... ``` ` — без строк с тремя обратными кавычками.
2. Либо откройте готовый файл **без** `#` в начале: [`docs/diagrams/c4-01-context.mmd`](./diagrams/c4-01-context.mmd) и следующие по номеру.
3. В [Mermaid Live Editor](https://mermaid.live) вставляйте код в левую панель как **текст Mermaid**, не как JSON.

Ниже — **универсальные `flowchart`** (работают в любом рендерере Mermaid). Расширенный синтаксис **C4** (строки `C4Context`, `C4Container`…) есть только в файлах `docs/diagrams/*.mmd` и требует поддержки C4 в Mermaid.

---

## Уровень 1 — System Context

```mermaid
flowchart LR
  user(["Пользователь"])
  idp_admin(["Администратор IdP"])
  mood["MoodDiary"]
  kc[["Keycloak"]]
  tg[["Telegram Bot API"]]

  user -->|"HTTPS, браузер"| mood
  idp_admin -->|"HTTPS /admin"| kc
  mood -->|"OIDC, PKCE, JWT"| kc
  mood -.->|"опционально"| tg
```

**Назначение:** граница продукта и внешние системы (IdP, опционально Telegram).

---

## Уровень 2 — Containers (production)

```mermaid
flowchart TB
  user(["Пользователь"])
  subgraph boundary["MoodDiary"]
    nginx["Nginx\nreverse proxy"]
    spa["Web SPA\nReact / Vite"]
    api["Backend API\nSpring Boot"]
    kc["Keycloak"]
    pg[("PostgreSQL")]
    fs[("Том фото\nPHOTO_STORAGE_DIR")]
  end

  user -->|"HTTPS"| nginx
  nginx -->|"статика"| spa
  nginx -->|"/api"| api
  nginx -->|"/realms, /admin"| kc
  api --> pg
  api --> fs
  kc --> pg
  spa -->|"OIDC"| kc
  spa -->|"Bearer /api"| nginx
```

**Локально:** SPA часто на порту 5173, backend на 8080, Keycloak на 8180 — логика та же, edge может быть без Nginx.

---

## Уровень 3 — Components (Backend API)

```mermaid
flowchart TB
  subgraph api["Backend API"]
    ctrl["HTTP adapters\nControllers"]
    sec["Security\nOAuth2 Resource Server"]
    app["Application\nuse cases, порты"]
    dom["Domain"]
    inf["Infrastructure\nJPA, файлы, Telegram"]
  end

  ctrl --> sec
  ctrl --> app
  app --> dom
  app --> inf
  inf --> dom
```

| Блок | Пакеты |
|------|--------|
| HTTP adapters | `adapter.http.*Controller` |
| Security | `adapter.security.SecurityConfiguration` |
| Application | `application.*`, `application.port.out` |
| Domain | `domain.*` |
| Infrastructure | `infrastructure.persistence.impl`, `infrastructure.storage` |

---

## Уровень 3 — Components (Web SPA)

```mermaid
flowchart LR
  subgraph spa["Web SPA"]
    pages["Страницы"]
    auth["keycloak-js"]
    client["API client"]
    router["react-router"]
  end

  pages --> auth
  pages --> client
  pages --> router
  client --> auth
```

---

## Уровень 4 — Code (указатель)

- `domain` — доменная модель  
- `application` — сценарии и порты  
- `adapter` — вход HTTP и интеграции  
- `infrastructure` — БД и файлы  

---

## Файлы с диалектом C4 (Mermaid C4)

| Файл | Содержание |
|------|------------|
| [diagrams/c4-01-context.mmd](./diagrams/c4-01-context.mmd) | Контекст |
| [diagrams/c4-02-container.mmd](./diagrams/c4-02-container.mmd) | Контейнеры |
| [diagrams/c4-03-component-api.mmd](./diagrams/c4-03-component-api.mmd) | Компоненты API |
| [diagrams/c4-04-component-spa.mmd](./diagrams/c4-04-component-spa.mmd) | Компоненты SPA |

Вставляйте **целиком содержимое `.mmd`** в редактор Mermaid с поддержкой C4 (часто нужен Mermaid ≥ 10 и включённый тип **C4**).

---

## Связанные документы

- [TOGAF-MoodDiary.md](./TOGAF-MoodDiary.md)  
- [README.md](../README.md)  

**Инструменты:** [Structurizr](https://structurizr.com/), [PlantUML C4](https://github.com/plantuml-stdlib/C4-PlantUML).
