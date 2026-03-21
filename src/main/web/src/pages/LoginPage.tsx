import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useKeycloak } from '../auth/KeycloakContext'

export default function LoginPage() {
  const navigate = useNavigate()
  const { ready, authenticated, keycloak } = useKeycloak()

  useEffect(() => {
    if (!ready || !authenticated) return
    navigate('/diary/dashboard', { replace: true })
  }, [ready, authenticated, navigate])

  const loginRedirectUri = `${window.location.origin}/diary/login`

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div>
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Вход</h1>
        <p className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Keycloak (Authorization Code + PKCE)</p>
      </div>

      {!ready ? (
        <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Проверка сессии…</div>
      ) : authenticated ? (
        <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Перенаправление…</div>
      ) : (
        <button
          type="button"
          className="journal-btn-primary w-full min-h-[48px] px-4 py-3 text-base md:w-auto md:min-h-0 md:py-2 md:text-sm"
          onClick={() => keycloak.login({ redirectUri: loginRedirectUri })}
        >
          Войти через Keycloak
        </button>
      )}
    </div>
  )
}
