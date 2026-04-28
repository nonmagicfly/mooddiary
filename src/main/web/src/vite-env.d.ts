/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_KEYCLOAK_ISSUER_URI?: string
  readonly VITE_KEYCLOAK_CLIENT_ID?: string
  readonly VITE_DEV_AUTH_ENABLED?: string
  readonly VITE_DEV_AUTH_USERNAME?: string
  readonly VITE_DEV_AUTH_PASSWORD?: string
  readonly VITE_DEV_AUTH_SUBJECT?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

