import { beforeEach, describe, expect, test } from 'vitest'
import { authState, clearAuth, setAuth } from '../api/http'

describe('auth state', () => {
  beforeEach(() => {
    clearAuth()
  })

  test('keeps reactive auth state in sync with localStorage', () => {
    const auth = { token: 'jwt-token', username: 'robin' }

    setAuth(auth)

    expect(authState.value).toEqual(auth)
    expect(JSON.parse(window.localStorage.getItem('smartstudy.auth'))).toEqual(auth)

    clearAuth()

    expect(authState.value).toBeNull()
    expect(window.localStorage.getItem('smartstudy.auth')).toBeNull()
  })
})
