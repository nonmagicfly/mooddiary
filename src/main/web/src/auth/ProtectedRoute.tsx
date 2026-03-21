import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useKeycloak } from './KeycloakContext'

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { ready, authenticated } = useKeycloak()
  const location = useLocation()

  if (!ready) {
    return (
      <div className="flex min-h-[200px] items-center justify-center text-journal-inkMuted dark:text-journalDark-inkMuted">
        Проверка авторизации…
      </div>
    )
  }

  if (!authenticated) {
    return <Navigate to="/diary/login" state={{ from: location }} replace />
  }

  return <>{children}</>
}

export function DefaultRedirect() {
  const { ready, authenticated } = useKeycloak()
  if (!ready) {
    return (
      <div className="flex min-h-[200px] items-center justify-center text-journal-inkMuted dark:text-journalDark-inkMuted">
        Загрузка…
      </div>
    )
  }
  return <Navigate to={authenticated ? '/diary/dashboard' : '/diary/login'} replace />
}
