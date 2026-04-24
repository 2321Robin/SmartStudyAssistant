import { request } from './http'

export function generatePlan(payload) {
  return request('/api/plans/generate', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function listPlans() {
  return request('/api/plans')
}

export function updateTaskStatus(planId, taskId, status) {
  return request(`/api/plans/${planId}/tasks/${taskId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
}

export function deletePlan(planId) {
  return request(`/api/plans/${planId}`, {
    method: 'DELETE'
  })
}
