import type Keycloak from 'keycloak-js'

export function persistKeycloakTokens(k: Keycloak) {
  if (k.token) localStorage.setItem('access_token', k.token)
  if (k.refreshToken) localStorage.setItem('refresh_token', k.refreshToken)
  if (k.idToken) localStorage.setItem('id_token', k.idToken)
}

export function clearStoredKeycloakTokens() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('id_token')
}
