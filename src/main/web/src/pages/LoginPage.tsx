import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useKeycloak } from '../auth/KeycloakContext'

export default function LoginPage() {
  const navigate = useNavigate()
  const { ready, authenticated, keycloak, devAuthEnabled, loginWithDevCredentials } = useKeycloak()
  const [username, setUsername] = useState('user')
  const [password, setPassword] = useState('user')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!ready || !authenticated) return
    navigate('/diary/entry/new', { replace: true })
  }, [ready, authenticated, navigate])

  const loginRedirectUri = `${window.location.origin}/diary/login`

  const submitDevLogin = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (loginWithDevCredentials(username.trim(), password)) {
      navigate('/diary/entry/new', { replace: true })
      return
    }
    setError('Неверный логин или пароль')
  }

  return (
    <div className="mx-auto max-w-md space-y-6">
      <div>
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Вход</h1>
        <p className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
          {devAuthEnabled ? 'Отладочный вход без Keycloak' : 'Keycloak (Authorization Code + PKCE)'}
        </p>
      </div>

      {!ready ? (
        <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Проверка сессии…</div>
      ) : authenticated ? (
        <div className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Перенаправление…</div>
      ) : devAuthEnabled ? (
        <form className="journal-card space-y-4 p-4" onSubmit={submitDevLogin}>
          <label className="block space-y-1">
            <span className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Логин</span>
            <input className="journal-input w-full" value={username} onChange={(e) => setUsername(e.target.value)} />
          </label>
          <label className="block space-y-1">
            <span className="text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Пароль</span>
            <input
              className="journal-input w-full"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </label>
          {error ? <div className="text-sm text-amber-700 dark:text-amber-200">{error}</div> : null}
          <button type="submit" className="journal-btn-primary w-full min-h-[48px] px-4 py-3 text-base">
            Войти
          </button>
          <div className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">
            Для отладки используйте user / user.
          </div>
        </form>
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
