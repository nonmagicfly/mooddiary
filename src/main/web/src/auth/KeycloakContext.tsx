import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type Keycloak from 'keycloak-js'
import { getKeycloakInstance, initKeycloakOnce } from './keycloakBootstrap'
import { persistKeycloakTokens } from './keycloakTokens'

export type KeycloakContextValue = {
  ready: boolean
  authenticated: boolean
  keycloak: Keycloak
}

const KeycloakContext = createContext<KeycloakContextValue | null>(null)

const TOKEN_REFRESH_INTERVAL_MS = 60_000
const TOKEN_MIN_VALIDITY_SEC = 70

export function KeycloakProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false)
  const [authenticated, setAuthenticated] = useState(false)

  useEffect(() => {
    void initKeycloakOnce().then((auth) => {
      const k = getKeycloakInstance()
      setAuthenticated(!!auth && !!k.authenticated)
      setReady(true)
    })
  }, [])

  useEffect(() => {
    if (!ready) return
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
  }, [ready])

  const value = useMemo<KeycloakContextValue>(
    () => ({
      ready,
      authenticated,
      keycloak: getKeycloakInstance()
    }),
    [ready, authenticated]
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
