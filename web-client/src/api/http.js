import { ref } from 'vue'

const tokenKey = 'smartstudy.auth'
export const authState = ref(readStoredAuth())

function readStoredAuth() {
  const raw = window.localStorage.getItem(tokenKey)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    window.localStorage.removeItem(tokenKey)
    return null
  }
}

function parseResponseBody(text) {
  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

function parsePayload(responseBody, fallbackMessage) {
  if (responseBody && responseBody.success === false) {
    throw new Error(responseBody.message || fallbackMessage)
  }

  return responseBody?.data ?? responseBody
}

export function getAuth() {
  return authState.value
}

export function setAuth(auth) {
  authState.value = auth
  window.localStorage.setItem(tokenKey, JSON.stringify(auth))
}

export function clearAuth() {
  authState.value = null
  window.localStorage.removeItem(tokenKey)
}

export async function request(path, options = {}) {
  const auth = getAuth()
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(auth?.token ? { Authorization: `Bearer ${auth.token}` } : {}),
      ...(options.headers || {})
    },
    ...options
  })

  const text = await response.text()
  const body = parseResponseBody(text)

  if (!response.ok) {
    if (body && typeof body === 'object') {
      throw new Error(body.message || `Request failed with status ${response.status}`)
    }

    throw new Error(body || `Request failed with status ${response.status}`)
  }

  return parsePayload(body, 'Request failed')
}
