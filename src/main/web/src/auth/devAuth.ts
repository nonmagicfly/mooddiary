const DEV_AUTH_KEY = 'mooddiary_dev_auth'

const DEV_USERNAME = import.meta.env.VITE_DEV_AUTH_USERNAME?.trim() || 'user'
const DEV_PASSWORD = import.meta.env.VITE_DEV_AUTH_PASSWORD?.trim() || 'user'
const DEV_SUBJECT = import.meta.env.VITE_DEV_AUTH_SUBJECT?.trim() || DEV_USERNAME

function base64Url(input: string): string {
  return btoa(input).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

function createDevJwt(): string {
  const header = base64Url(JSON.stringify({ alg: 'none', typ: 'JWT' }))
  const payload = base64Url(JSON.stringify({ sub: DEV_SUBJECT, preferred_username: DEV_USERNAME }))
  return `${header}.${payload}.`
}

export function isDevAuthEnabled(): boolean {
  return import.meta.env.VITE_DEV_AUTH_ENABLED === 'true'
}

export function isDevAuthenticated(): boolean {
  return localStorage.getItem(DEV_AUTH_KEY) === 'true'
}

export function loginWithDevCredentials(username: string, password: string): boolean {
  if (username !== DEV_USERNAME || password !== DEV_PASSWORD) return false
  localStorage.setItem(DEV_AUTH_KEY, 'true')
  localStorage.setItem('access_token', createDevJwt())
  return true
}

export function logoutDevAuth() {
  localStorage.removeItem(DEV_AUTH_KEY)
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('id_token')
}
