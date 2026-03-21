import Keycloak from 'keycloak-js'
import { getKeycloakServerConfig } from './keycloakConfig'
import { persistKeycloakTokens } from './keycloakTokens'

let instance: Keycloak | null = null
let initPromise: Promise<boolean> | null = null

export function getKeycloakInstance(): Keycloak {
  if (!instance) {
    instance = new Keycloak(getKeycloakServerConfig())
  }
  return instance
}

/**
 * Single init per page load (safe with React StrictMode).
 * PKCE + silent check-sso; restores tokens from localStorage when present.
 */
export function initKeycloakOnce(): Promise<boolean> {
  if (initPromise) return initPromise

  const k = getKeycloakInstance()
  initPromise = k
    .init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      checkLoginIframe: false,
      responseMode: 'query',
      token: localStorage.getItem('access_token') || undefined,
      refreshToken: localStorage.getItem('refresh_token') || undefined,
      idToken: localStorage.getItem('id_token') || undefined
    })
    .then((auth) => {
      if (auth && k.token) {
        persistKeycloakTokens(k)
      }
      return auth
    })
    .catch((err) => {
      console.error('Keycloak init failed', err)
      return false
    })

  return initPromise
}
