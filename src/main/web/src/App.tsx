import React, { useEffect, useMemo, useState } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { KeycloakProvider, useKeycloak } from './auth/KeycloakContext'
import { DefaultRedirect, ProtectedRoute } from './auth/ProtectedRoute'
import DiaryEntryPage from './pages/DiaryEntryPage'
import AnalyticsPage from './pages/AnalyticsPage'
import DiaryHistoryPage from './pages/DiaryHistoryPage'
import TagsPage from './pages/TagsPage'
import SymptomsPage from './pages/SymptomsPage'
import SettingsPage from './pages/SettingsPage'
import DashboardPage from './pages/DashboardPage'
import LoginPage from './pages/LoginPage'

const THEME_KEY = 'theme'

function applyTheme(theme: string) {
  const root = document.documentElement
  root.classList.toggle('dark', theme === 'dark')
}

function HeaderAuth() {
  const navigate = useNavigate()
  const { ready, authenticated, keycloak } = useKeycloak()

  if (!ready) {
    return <span className="text-xs text-journal-inkMuted dark:text-journalDark-inkMuted">Авторизация…</span>
  }

  if (!authenticated) {
    return (
      <button
        type="button"
        className="journal-btn-secondary text-sm"
        onClick={() => navigate('/diary/login')}
      >
        Войти
      </button>
    )
  }

  return (
    <button
      type="button"
      className="journal-btn-secondary text-sm"
      onClick={() =>
        keycloak.logout({
          redirectUri: `${window.location.origin}/diary/login`
        })
      }
    >
      Выйти
    </button>
  )
}

function AppShell() {
  const location = useLocation()
  const navigate = useNavigate()
  const initialTheme = useMemo(() => {
    const stored = localStorage.getItem(THEME_KEY)
    return stored === 'dark' ? 'dark' : 'light'
  }, [])

  const [theme, setTheme] = useState(initialTheme)

  useEffect(() => {
    localStorage.setItem(THEME_KEY, theme)
    applyTheme(theme)
  }, [theme])

  useEffect(() => {
    applyTheme(theme)
  }, [])

  useEffect(() => {
    applyTheme(theme)
  }, [location.pathname])

  useEffect(() => {
    const onThemeEvent = (e: Event) => {
      const detail = (e as CustomEvent).detail as string | undefined
      if (detail === 'light' || detail === 'dark') setTheme(detail)
    }

    window.addEventListener('mooddiary-theme', onThemeEvent)
    return () => window.removeEventListener('mooddiary-theme', onThemeEvent)
  }, [])

  const isLoginPage = location.pathname === '/diary/login'

  return (
    <div className="min-h-screen bg-journal-cream text-journal-ink dark:bg-journalDark-bg dark:text-journalDark-ink font-body">
      <div className="flex min-h-screen">
        <aside className="hidden w-64 border-r border-journal-line bg-journal-paper dark:border-journalDark-line dark:bg-journalDark-paper md:block">
          <div className="p-4 font-heading text-lg font-semibold text-journal-ink dark:text-journalDark-ink">MoodDiary</div>
          <nav className="space-y-1 p-4">
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/entry/new">
              Запись
            </a>
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/analytics">
              Аналитика
            </a>
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/history">
              История
            </a>
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/dashboard">
              Обзор
            </a>
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/tags">
              Теги
            </a>
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/symptoms">
              Симптомы
            </a>
            <a className="block rounded-sm px-2 py-1.5 text-journal-inkMuted transition-colors hover:bg-journal-fold hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-fold dark:hover:text-journalDark-ink" href="/diary/settings">
              Настройки
            </a>
          </nav>
        </aside>

        <div className="flex flex-1 flex-col pb-safe">
          <header
            className="flex items-center justify-between border-b border-journal-line bg-journal-paper px-4 py-3 dark:border-journalDark-line dark:bg-journalDark-paper"
            style={{ paddingTop: 'max(12px, env(safe-area-inset-top))' }}
          >
            <div className="font-heading font-medium text-journal-ink dark:text-journalDark-ink">Дневник</div>
            <div className="flex items-center gap-3">
              <HeaderAuth />
              <button
                type="button"
                className="journal-btn-secondary text-sm"
                onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
              >
                {theme === 'dark' ? 'Светлая' : 'Тёмная'}
              </button>
              <button
                type="button"
                className="text-sm text-journal-inkMuted transition-colors hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:text-journalDark-ink"
                onClick={() => navigate('/diary/settings')}
              >
                Профиль
              </button>
            </div>
          </header>

          <main className="flex-1 overflow-auto p-4 pb-20 md:pb-4">
            <Routes>
              <Route path="/diary/login" element={<LoginPage />} />
              <Route path="/diary/entry/new" element={<ProtectedRoute><DiaryEntryPage mode="create" /></ProtectedRoute>} />
              <Route path="/diary/entry/:id" element={<ProtectedRoute><DiaryEntryPage mode="edit" /></ProtectedRoute>} />
              <Route path="/diary/analytics" element={<ProtectedRoute><AnalyticsPage /></ProtectedRoute>} />
              <Route path="/diary/history" element={<ProtectedRoute><DiaryHistoryPage /></ProtectedRoute>} />
              <Route path="/diary/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
              <Route path="/diary/tags" element={<ProtectedRoute><TagsPage /></ProtectedRoute>} />
              <Route path="/diary/symptoms" element={<ProtectedRoute><SymptomsPage /></ProtectedRoute>} />
              <Route path="/diary/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
              <Route path="*" element={<DefaultRedirect />} />
            </Routes>
          </main>

          {!isLoginPage ? (
            <nav
              className="mobile-nav fixed bottom-0 left-0 right-0 z-50 flex items-center justify-around border-t border-journal-line bg-journal-paper/95 py-2 backdrop-blur supports-[backdrop-filter]:bg-journal-paper/90 dark:border-journalDark-line dark:bg-journalDark-paper/95 supports-[backdrop-filter]:dark:bg-journalDark-paper/90 md:hidden"
              style={{ paddingBottom: 'max(8px, env(safe-area-inset-bottom))' }}
            >
              <a
                className={`flex flex-col items-center gap-0.5 rounded-sm px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/entry/new' || location.pathname.startsWith('/diary/entry/') ? 'text-journal-accent dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/entry/new"
              >
                <span className="text-lg">✏️</span>
                Запись
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-sm px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/dashboard' ? 'text-journal-accent dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/dashboard"
              >
                <span className="text-lg">📊</span>
                Обзор
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-sm px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/history' ? 'text-journal-accent dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/history"
              >
                <span className="text-lg">📅</span>
                История
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-sm px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/analytics' ? 'text-journal-accent dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/analytics"
              >
                <span className="text-lg">📈</span>
                Аналитика
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-sm px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/settings' || location.pathname === '/diary/tags' || location.pathname === '/diary/symptoms' ? 'text-journal-accent dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/settings"
              >
                <span className="text-lg">⚙️</span>
                Ещё
              </a>
            </nav>
          ) : null}
        </div>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <KeycloakProvider>
      <AppShell />
    </KeycloakProvider>
  )
}

