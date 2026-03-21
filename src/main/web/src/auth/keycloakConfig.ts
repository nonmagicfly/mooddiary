import type { KeycloakServerConfig } from 'keycloak-js'

/** Keycloak в Docker (docker compose): localhost:8180, realm mooddiary */
const DEFAULT_ISSUER = 'http://localhost:8180/realms/mooddiary'

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
  const issuer = (import.meta.env.VITE_KEYCLOAK_ISSUER_URI?.toString().trim() || DEFAULT_ISSUER).replace(/\/+$/, '')
  const { url, realm } = parseIssuerToServerConfig(issuer)
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID?.toString().trim() || 'mooddiary-web'
  return { url, realm, clientId }
}
