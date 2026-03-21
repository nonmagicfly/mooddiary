import '@testing-library/jest-dom/vitest'
import { vi } from 'vitest'

const { mockKeycloak } = vi.hoisted(() => ({
  mockKeycloak: {
    authenticated: false,
    login: vi.fn(),
    logout: vi.fn(),
    updateToken: vi.fn(() => Promise.resolve(false))
  }
}))

vi.mock('../auth/keycloakBootstrap', () => ({
  getKeycloakInstance: () => mockKeycloak,
  initKeycloakOnce: () => Promise.resolve(false)
}))

