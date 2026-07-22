import type { KeycloakServerConfig } from 'keycloak-js'

/** Local dev default (docker compose): localhost:8180 */
const DEV_ISSUER = 'http://localhost:8180/realms/mooddiary'

/** When VITE_KEYCLOAK_ISSUER_URI is missing at build time, derive issuer from the current origin in production. */
function getDefaultIssuer(): string {
  if (typeof window !== 'undefined') {
    const host = window.location.hostname
    if (host !== 'localhost' && host !== '127.0.0.1') {
      return `${window.location.origin}/realms/mooddiary`
    }
  }
  return DEV_ISSUER
}

/**
 * Parses Spring-style issuer URI: {base}/realms/{realm}
 * Example: http://host.docker.internal:8180/realms/mooddiary → url + realm
 */
export function parseIssuerToServerConfig(issuer: string): Pick<KeycloakServerConfig, 'url' | 'realm'> {
  const trimmed = issuer.trim().replace(/\/+$/, '')
  const match = trimmed.match(/^(.*)\/realms\/([^/]+)$/)
  if (!match) {
    throw new Error(`Invalid Keycloak issuer URI (expected .../realms/{realm}): ${issuer}`)
  }
  return { url: match[1], realm: match[2] }
}

export function getKeycloakServerConfig(): KeycloakServerConfig {
  const issuer = (import.meta.env.VITE_KEYCLOAK_ISSUER_URI?.toString().trim() || getDefaultIssuer()).replace(/\/+$/, '')
  const { url, realm } = parseIssuerToServerConfig(issuer)
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID?.toString().trim() || 'mooddiary-web'
  return { url, realm, clientId }
}
