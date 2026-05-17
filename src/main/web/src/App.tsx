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

const NAV_ITEMS = [
  { href: '/diary/entry/new', label: 'Запись', icon: '✦' },
  { href: '/diary/dashboard', label: 'Обзор', icon: '◌' },
  { href: '/diary/history', label: 'История', icon: '◇' },
  { href: '/diary/analytics', label: 'Аналитика', icon: '⌁' },
  { href: '/diary/tags', label: 'Теги', icon: '#' },
  { href: '/diary/symptoms', label: 'Симптомы', icon: '+' },
  { href: '/diary/settings', label: 'Настройки', icon: '•' }
]

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

function isActivePath(pathname: string, href: string) {
  if (href === '/diary/entry/new') return pathname === href || pathname.startsWith('/diary/entry/')
  if (href === '/diary/settings') return pathname === href || pathname === '/diary/tags' || pathname === '/diary/symptoms'
  return pathname === href
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
    <div className="min-h-screen text-journal-ink dark:text-journalDark-ink font-body">
      <div className="flex min-h-screen">
        <aside className="hidden w-72 border-r border-journal-line/70 bg-journal-paper/70 p-4 shadow-paper backdrop-blur-md dark:border-journalDark-line/70 dark:bg-journalDark-paper/65 md:block">
          <div className="journal-card overflow-hidden p-5">
            <div className="journal-eyebrow">MoodDiary</div>
            <div className="mt-3 font-heading text-2xl font-semibold leading-tight">Тёплый журнал</div>
            <div className="mt-2 text-sm text-journal-inkMuted dark:text-journalDark-inkMuted">
              Место для заметок, ритмов и тихих выводов о себе.
            </div>
          </div>
          <nav className="mt-5 space-y-2">
            {NAV_ITEMS.map((item) => {
              const active = isActivePath(location.pathname, item.href)
              return (
                <a
                  key={item.href}
                  className={`flex items-center gap-3 rounded-2xl px-4 py-3 font-heading text-sm transition-all ${
                    active
                      ? 'bg-journal-accent text-white shadow-paperFold dark:bg-journalDark-accent dark:text-journalDark-bg'
                      : 'text-journal-inkMuted hover:-translate-y-0.5 hover:bg-journal-paper/80 hover:text-journal-ink dark:text-journalDark-inkMuted dark:hover:bg-journalDark-paper/80 dark:hover:text-journalDark-ink'
                  }`}
                  href={item.href}
                >
                  <span className="flex h-7 w-7 items-center justify-center rounded-full border border-current/20 text-xs">
                    {item.icon}
                  </span>
                  {item.label}
                </a>
              )
            })}
          </nav>
        </aside>

        <div className="flex flex-1 flex-col pb-safe">
          <header
            className="sticky top-0 z-40 flex items-center justify-between border-b border-journal-line/70 bg-journal-cream/80 px-4 py-3 backdrop-blur-md dark:border-journalDark-line/70 dark:bg-journalDark-bg/80"
            style={{ paddingTop: 'max(12px, env(safe-area-inset-top))' }}
          >
            <div>
              <div className="font-heading text-lg font-semibold text-journal-ink dark:text-journalDark-ink">Дневник</div>
              <div className="hidden text-xs text-journal-inkMuted dark:text-journalDark-inkMuted sm:block">Записывайте главное без спешки</div>
            </div>
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

          <main className="flex-1 overflow-auto p-4 pb-24 md:p-8">
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
              className="mobile-nav fixed bottom-0 left-0 right-0 z-50 flex items-center justify-around border-t border-journal-line/70 bg-journal-paper/95 py-2 shadow-paper backdrop-blur supports-[backdrop-filter]:bg-journal-paper/90 dark:border-journalDark-line/70 dark:bg-journalDark-paper/95 supports-[backdrop-filter]:dark:bg-journalDark-paper/90 md:hidden"
              style={{ paddingBottom: 'max(8px, env(safe-area-inset-bottom))' }}
            >
              <a
                className={`flex flex-col items-center gap-0.5 rounded-2xl px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/entry/new' || location.pathname.startsWith('/diary/entry/') ? 'bg-journal-fold text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/entry/new"
              >
                <span className="text-lg">✏️</span>
                Запись
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-2xl px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/dashboard' ? 'bg-journal-fold text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/dashboard"
              >
                <span className="text-lg">📊</span>
                Обзор
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-2xl px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/history' ? 'bg-journal-fold text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/history"
              >
                <span className="text-lg">📅</span>
                История
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-2xl px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/analytics' ? 'bg-journal-fold text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
                href="/diary/analytics"
              >
                <span className="text-lg">📈</span>
                Аналитика
              </a>
              <a
                className={`flex flex-col items-center gap-0.5 rounded-2xl px-3 py-2 text-xs transition-colors ${location.pathname === '/diary/settings' || location.pathname === '/diary/tags' || location.pathname === '/diary/symptoms' ? 'bg-journal-fold text-journal-accent dark:bg-journalDark-fold dark:text-journalDark-accent' : 'text-journal-inkMuted dark:text-journalDark-inkMuted'}`}
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

