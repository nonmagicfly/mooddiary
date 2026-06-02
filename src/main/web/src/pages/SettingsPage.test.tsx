import React from 'react'
import { MemoryRouter } from 'react-router-dom'
import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { routerFutureFlags } from '../test/routerFuture'
import SettingsPage from './SettingsPage'

vi.mock('../auth/KeycloakContext', () => ({
  useKeycloak: () => ({
    ready: true,
    authenticated: false,
    keycloak: { logout: vi.fn() }
  })
}))

vi.mock('../api/api', () => ({
  getTelegramChatId: vi.fn().mockResolvedValue({ telegramChatId: '' }),
  updateTelegramChatId: vi.fn().mockResolvedValue(undefined),
  getTags: vi.fn().mockResolvedValue([]),
  createTag: vi.fn().mockResolvedValue({}),
  updateTag: vi.fn().mockResolvedValue({}),
  deleteTag: vi.fn().mockResolvedValue(undefined),
  getSymptoms: vi.fn().mockResolvedValue([]),
  createSymptom: vi.fn().mockResolvedValue({}),
  updateSymptom: vi.fn().mockResolvedValue({}),
  deleteSymptom: vi.fn().mockResolvedValue(undefined)
}))

describe('SettingsPage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should render tag and symptom management inside settings', async () => {
    render(
      <MemoryRouter future={routerFutureFlags}>
        <SettingsPage />
      </MemoryRouter>
    )

    expect(screen.getByText('Справочники')).toBeInTheDocument()
    expect(await screen.findByText('Теги')).toBeInTheDocument()
    expect(await screen.findByText('Симптомы')).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText('Тегов пока нет.')).toBeInTheDocument()
      expect(screen.getByText('Симптомов пока нет.')).toBeInTheDocument()
    })
  })
})
