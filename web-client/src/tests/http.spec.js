import { afterEach, describe, expect, test, vi } from 'vitest'
import { request } from '../api/http'

describe('request', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  test('surfaces plain-text error responses without json parsing failures', async () => {
    vi.spyOn(window, 'fetch').mockResolvedValue({
      ok: false,
      status: 502,
      text: vi.fn().mockResolvedValue('gateway temporarily unavailable'),
      headers: new Headers({ 'content-type': 'text/plain' })
    })

    await expect(request('/api/plans')).rejects.toThrow('gateway temporarily unavailable')
  })
})
