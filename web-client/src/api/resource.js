import { request } from './http'

export function searchResources(keyword = '') {
  const params = new URLSearchParams()
  if (keyword.trim()) {
    params.set('keyword', keyword.trim())
  }

  const query = params.toString()
  return request(query ? `/api/resources?${query}` : '/api/resources')
}
