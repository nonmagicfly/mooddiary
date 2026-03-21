/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_KEYCLOAK_ISSUER_URI?: string
  readonly VITE_KEYCLOAK_CLIENT_ID?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

