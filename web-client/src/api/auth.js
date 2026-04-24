import { authState, clearAuth, getAuth, request, setAuth } from './http'

export { authState }

export function getStoredAuth() {
  return getAuth()
}

export async function login(credentials) {
  const data = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials)
  })

  setAuth(data)
  return data
}

export async function register(credentials) {
  const data = await request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(credentials)
  })

  setAuth(data)
  return data
}

export function logout() {
  clearAuth()
}
