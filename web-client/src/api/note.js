import { request } from './http'

export function createNote(payload) {
  return request('/api/notes', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function listAllNotes() {
  return request('/api/notes')
}

export function listNoteSquare() {
  return request('/api/notes/square')
}
