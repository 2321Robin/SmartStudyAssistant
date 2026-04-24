import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/vue'
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import LoginView from '../views/LoginView.vue'

const { pushSpy, loginSpy, registerSpy } = vi.hoisted(() => ({
  pushSpy: vi.fn(),
  loginSpy: vi.fn(),
  registerSpy: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushSpy
  })
}))

vi.mock('../api/auth', () => ({
  login: loginSpy,
  register: registerSpy
}))

describe('LoginView', () => {
  beforeEach(() => {
    pushSpy.mockReset()
    loginSpy.mockReset()
    registerSpy.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  test('renders labeled username and password fields', () => {
    render(LoginView)

    expect(screen.getByLabelText('用户名')).toBeInTheDocument()
    expect(screen.getByLabelText('密码')).toBeInTheDocument()
  })

  test('submits login credentials and redirects on success', async () => {
    loginSpy.mockResolvedValue({ token: 'jwt-token', username: 'robin' })
    const { container } = render(LoginView)

    await fireEvent.update(screen.getByLabelText('用户名'), 'robin')
    await fireEvent.update(screen.getByLabelText('密码'), '12345678')
    await fireEvent.submit(container.querySelector('form'))

    await waitFor(() => {
      expect(loginSpy).toHaveBeenCalledWith({
        username: 'robin',
        password: '12345678'
      })
    })

    expect(pushSpy).toHaveBeenCalledWith('/plans/generate')
    expect(screen.getByText('登录成功，当前用户：robin')).toBeInTheDocument()
  })

  test('shows backend error message when login fails', async () => {
    loginSpy.mockRejectedValue(new Error('用户名或密码错误'))
    const { container } = render(LoginView)

    await fireEvent.update(screen.getByLabelText('用户名'), 'robin')
    await fireEvent.update(screen.getByLabelText('密码'), '12345678')
    await fireEvent.submit(container.querySelector('form'))

    expect(await screen.findByText('用户名或密码错误')).toBeInTheDocument()
    expect(pushSpy).not.toHaveBeenCalled()
  })
})
