import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useKeycloak } from '../auth/KeycloakContext'
import { clearStoredKeycloakTokens } from '../auth/keycloakTokens'
import { getTelegramChatId, updateTelegramChatId } from '../api/api'
import { UUID } from '../api/types'

function getAccessToken(): string | null {
  return localStorage.getItem('access_token')
}

function safeDecodeJwtSubject(token: string): string | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
    // Common fields: `sub` or `subject`
    return (payload?.sub ?? payload?.subject ?? null) as string | null
  } catch {
    return null
  }
}

export default function SettingsPage() {
  const navigate = useNavigate()
  const { ready, authenticated, keycloak } = useKeycloak()

  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const stored = localStorage.getItem('theme')
    return stored === 'light' ? 'light' : 'dark'
  })

  const [tokenSubject, setTokenSubject] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [telegramChatId, setTelegramChatId] = useState('')
  const [telegramSaving, setTelegramSaving] = useState(false)
  const [telegramError, setTelegramError] = useState<string | null>(null)

  const hasToken = !!getAccessToken()

  useEffect(() => {
    const root = document.documentElement
    root.classList.toggle('dark', theme === 'dark')
    localStorage.setItem('theme', theme)
    window.dispatchEvent(new CustomEvent('mooddiary-theme', { detail: theme }))
  }, [theme])

  useEffect(() => {
    const token = getAccessToken()
    if (!token) {
      setTokenSubject(null)
      setError(null)
      return
    }
    const sub = safeDecodeJwtSubject(token)
    setTokenSubject(sub)
    setError(sub ? null : 'Не удалось декодировать JWT (проверьте формат токена).')
  }, [])

  useEffect(() => {
    if (!hasToken) return
    getTelegramChatId()
      .then((r) => setTelegramChatId(r.telegramChatId ?? ''))
      .catch(() => setTelegramChatId(''))
  }, [hasToken])

  const saveTelegramChatId = async () => {
    setTelegramError(null)
    setTelegramSaving(true)
    try {
      await updateTelegramChatId(telegramChatId.trim())
    } catch (e) {
      setTelegramError(e instanceof Error ? e.message : 'Ошибка сохранения')
    } finally {
      setTelegramSaving(false)
    }
  }

  const logout = () => {
    if (ready && authenticated) {
      void keycloak.logout({
        redirectUri: `${window.location.origin}/diary/login`
      })
      return
    }
    clearStoredKeycloakTokens()
    // Keep theme preference as-is.
    navigate('/diary/entry/new')
  }

  const subjectId = tokenSubject ? (tokenSubject as UUID) : null

  return (
    <div className="mx-auto max-w-3xl">
      <div className="mb-6">
        <h1 className="font-heading text-2xl font-semibold text-journal-ink dark:text-journalDark-ink">Настройки</h1>
        <div className="mt-1 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">Профиль и предпочтения</div>
      </div>

      <div className="space-y-4">
        <div className="journal-card p-4">
          <div className="font-heading text-sm font-medium">Справочники</div>
          <div className="mt-2 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
            Управление списками тегов и симптомов для записей.
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            <button type="button" className="journal-btn-secondary py-2" onClick={() => navigate('/diary/tags')}>
              Теги
            </button>
            <button type="button" className="journal-btn-secondary py-2" onClick={() => navigate('/diary/symptoms')}>
              Симптомы
            </button>
          </div>
        </div>

        <div className="journal-card p-4">
          <div className="font-heading text-sm font-medium">Telegram</div>
          <div className="mt-2 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
            Укажите Chat ID для отправки саммари дня в бот. Напишите /start боту @userinfobot в Telegram, чтобы получить свой ID.
          </div>
          <div className="mt-3 flex flex-wrap items-end gap-2">
            <label className="flex-1 min-w-[200px] space-y-1">
              <span className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">Chat ID</span>
              <input
                className="journal-input w-full"
                type="text"
                value={telegramChatId}
                onChange={(e) => setTelegramChatId(e.target.value)}
                placeholder="123456789"
              />
            </label>
            <button
              type="button"
              className="journal-btn-primary disabled:opacity-50"
              onClick={saveTelegramChatId}
              disabled={telegramSaving}
            >
              {telegramSaving ? 'Сохранение…' : 'Сохранить'}
            </button>
          </div>
          {telegramError ? <div className="mt-2 text-sm text-amber-700 dark:text-amber-200">{telegramError}</div> : null}
        </div>

        <div className="journal-card p-4">
          <div className="font-heading text-sm font-medium">Тема</div>
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <button
              type="button"
              className={`rounded-sm border px-3 py-1 text-sm ${theme === 'light' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}
              onClick={() => setTheme('light')}
            >
              Светлая
            </button>
            <button
              type="button"
              className={`rounded-sm border px-3 py-1 text-sm ${theme === 'dark' ? 'border-journal-accent bg-journal-accent text-white dark:border-journalDark-accent dark:bg-journalDark-accent' : 'journal-btn-secondary'}`}
              onClick={() => setTheme('dark')}
            >
              Тёмная
            </button>
          </div>
        </div>

        <div className="journal-card p-4">
          <div className="font-heading text-sm font-medium">Профиль</div>
          <div className="mt-2 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
            {hasToken ? (
              <>
                <div>
                  ID пользователя: <span className="font-mono">{subjectId ?? '—'}</span>
                </div>
                {error ? <div className="mt-2 text-amber-700 dark:text-amber-200">{error}</div> : null}
              </>
            ) : (
              <div>Токен доступа не найден в localStorage.</div>
            )}
          </div>

          <div className="mt-4 flex flex-wrap items-center gap-2">
            <button
              type="button"
              className="rounded-sm border border-amber-400 bg-amber-100 px-3 py-1 text-sm text-amber-900 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-200 disabled:opacity-50"
              onClick={logout}
              disabled={!hasToken}
            >
              Выйти
            </button>
            <button type="button" className="journal-btn-secondary text-sm" onClick={() => navigate('/diary/history')}>
              К истории
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

