import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type Keycloak from 'keycloak-js'
import { getKeycloakInstance, initKeycloakOnce } from './keycloakBootstrap'
import { persistKeycloakTokens } from './keycloakTokens'
import { isDevAuthEnabled, isDevAuthenticated, loginWithDevCredentials, logoutDevAuth } from './devAuth'

type AuthClient = Pick<Keycloak, 'login' | 'logout'>

export type KeycloakContextValue = {
  ready: boolean
  authenticated: boolean
  keycloak: AuthClient
  devAuthEnabled: boolean
  loginWithDevCredentials: (username: string, password: string) => boolean
}

const KeycloakContext = createContext<KeycloakContextValue | null>(null)

const TOKEN_REFRESH_INTERVAL_MS = 60_000
const TOKEN_MIN_VALIDITY_SEC = 70

export function KeycloakProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false)
  const [authenticated, setAuthenticated] = useState(false)
  const devAuthEnabled = isDevAuthEnabled()

  useEffect(() => {
    if (devAuthEnabled) {
      setAuthenticated(isDevAuthenticated())
      setReady(true)
      return
    }

    void initKeycloakOnce().then((auth) => {
      const k = getKeycloakInstance()
      setAuthenticated(!!auth && !!k.authenticated)
      setReady(true)
    })
  }, [devAuthEnabled])

  useEffect(() => {
    if (!ready || devAuthEnabled) return
    const k = getKeycloakInstance()

    k.onTokenExpired = () => {
      void k.updateToken(TOKEN_MIN_VALIDITY_SEC).then((refreshed) => {
        if (refreshed) persistKeycloakTokens(k)
      })
    }

    const id = window.setInterval(() => {
      if (!k.authenticated) return
      void k.updateToken(TOKEN_MIN_VALIDITY_SEC).then((refreshed) => {
        if (refreshed) persistKeycloakTokens(k)
      })
    }, TOKEN_REFRESH_INTERVAL_MS)

    return () => {
      window.clearInterval(id)
      k.onTokenExpired = undefined
    }
  }, [ready, devAuthEnabled])

  const keycloakClient = useMemo<AuthClient>(() => {
    if (!devAuthEnabled) return getKeycloakInstance()

    return {
      login: async () => undefined,
      logout: async (options?: { redirectUri?: string }) => {
        logoutDevAuth()
        setAuthenticated(false)
        if (options?.redirectUri) {
          window.location.href = options.redirectUri
        }
      }
    }
  }, [devAuthEnabled])

  const loginDev = (username: string, password: string) => {
    const ok = loginWithDevCredentials(username, password)
    if (ok) setAuthenticated(true)
    return ok
  }

  const value = useMemo<KeycloakContextValue>(
    () => ({
      ready,
      authenticated,
      keycloak: keycloakClient,
      devAuthEnabled,
      loginWithDevCredentials: loginDev
    }),
    [ready, authenticated, keycloakClient, devAuthEnabled]
  )

  return <KeycloakContext.Provider value={value}>{children}</KeycloakContext.Provider>
}

export function useKeycloak(): KeycloakContextValue {
  const ctx = useContext(KeycloakContext)
  if (!ctx) {
    throw new Error('useKeycloak must be used within KeycloakProvider')
  }
  return ctx
}
