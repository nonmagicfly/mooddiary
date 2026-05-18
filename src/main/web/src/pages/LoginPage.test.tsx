import React from 'react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { KeycloakProvider } from '../auth/KeycloakContext'
import { routerFutureFlags } from '../test/routerFuture'
import LoginPage from './LoginPage'

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    vi.unstubAllEnvs()
  })

  it('should offer Keycloak sign-in when not authenticated', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter future={routerFutureFlags} initialEntries={['/diary/login']}>
        <KeycloakProvider>
          <Routes>
            <Route path="/diary/login" element={<LoginPage />} />
          </Routes>
        </KeycloakProvider>
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.queryByText('Проверка сессии…')).not.toBeInTheDocument()
    })

    const btn = screen.getByRole('button', { name: /войти через keycloak/i })
    await user.click(btn)

    const { getKeycloakInstance } = await import('../auth/keycloakBootstrap')
    const kc = getKeycloakInstance() as unknown as { login: ReturnType<typeof vi.fn> }
    expect(kc.login).toHaveBeenCalled()
  })
})
