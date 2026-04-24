import { cleanup, fireEvent, render, screen } from '@testing-library/vue'
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import NoteView from '../views/NoteView.vue'

const { createNoteSpy, listAllNotesSpy, listNoteSquareSpy } = vi.hoisted(() => ({
  createNoteSpy: vi.fn(),
  listAllNotesSpy: vi.fn(),
  listNoteSquareSpy: vi.fn()
}))

vi.mock('../api/note', () => ({
  createNote: createNoteSpy,
  listAllNotes: listAllNotesSpy,
  listNoteSquare: listNoteSquareSpy
}))

describe('NoteView', () => {
  beforeEach(() => {
    createNoteSpy.mockReset()
    listAllNotesSpy.mockReset()
    listNoteSquareSpy.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  test('shows empty state when no public notes are available', async () => {
    listAllNotesSpy.mockResolvedValue([])
    listNoteSquareSpy.mockResolvedValue([])

    render(NoteView)

    await screen.findByText('还没有公开笔记，发布第一篇来开启分享吧。')
  })

  test('renders note cards with collapsed preview and expandable full content', async () => {
    listAllNotesSpy.mockResolvedValue([])
    listNoteSquareSpy.mockResolvedValue([
      {
        id: 1,
        title: 'Spring Cloud 学习心得',
        content: '这是一个很长的学习笔记内容。'.repeat(12),
        publicVisible: true,
        createdAt: '2026-04-21T08:30:00Z'
      }
    ])

    render(NoteView)

    expect(await screen.findByText('公开笔记')).toBeInTheDocument()
    expect(screen.getByText(/发布时间/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '展开全文' })).toBeInTheDocument()
    expect(screen.getByText(/\.\.\.$/)).toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '展开全文' }))

    expect(screen.getByRole('button', { name: '收起' })).toBeInTheDocument()
    expect(screen.queryByText(/\.\.\.$/)).not.toBeInTheDocument()
  })

  test('shows unknown publish time when note createdAt is missing', async () => {
    listAllNotesSpy.mockResolvedValue([])
    listNoteSquareSpy.mockResolvedValue([
      {
        id: 2,
        title: '旧公开笔记',
        content: '这是没有 createdAt 的旧数据',
        publicVisible: true
      }
    ])

    render(NoteView)

    expect(await screen.findByText('发布时间未知')).toBeInTheDocument()
  })

  test('renders my notes with private and public visibility labels', async () => {
    listAllNotesSpy.mockResolvedValue([
      {
        id: 10,
        title: '我的私人草稿',
        content: '这是未发布到广场的内容',
        publicVisible: false,
        createdAt: '2026-04-21T09:00:00Z'
      },
      {
        id: 11,
        title: '我的公开笔记',
        content: '这篇已经发布',
        publicVisible: true,
        createdAt: '2026-04-21T10:00:00Z'
      }
    ])
    listNoteSquareSpy.mockResolvedValue([])

    render(NoteView)

    expect(await screen.findByText('我的笔记（2）')).toBeInTheDocument()
    expect(await screen.findByText('我的私人草稿')).toBeInTheDocument()
    expect(screen.getByText('仅自己可见')).toBeInTheDocument()
    expect(screen.getByText('我的公开笔记')).toBeInTheDocument()
    expect(screen.getByText('已发布到广场')).toBeInTheDocument()
  })

  test('shows only the latest five notes by default and expands on demand', async () => {
    listAllNotesSpy.mockResolvedValue([
      { id: 6, title: '笔记 6', content: '内容 6', publicVisible: true, createdAt: '2026-04-21T10:06:00Z' },
      { id: 5, title: '笔记 5', content: '内容 5', publicVisible: false, createdAt: '2026-04-21T10:05:00Z' },
      { id: 4, title: '笔记 4', content: '内容 4', publicVisible: true, createdAt: '2026-04-21T10:04:00Z' },
      { id: 3, title: '笔记 3', content: '内容 3', publicVisible: false, createdAt: '2026-04-21T10:03:00Z' },
      { id: 2, title: '笔记 2', content: '内容 2', publicVisible: true, createdAt: '2026-04-21T10:02:00Z' },
      { id: 1, title: '笔记 1', content: '内容 1', publicVisible: false, createdAt: '2026-04-21T10:01:00Z' }
    ])
    listNoteSquareSpy.mockResolvedValue([])

    render(NoteView)

    expect(await screen.findByText('我的笔记（6）')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '展开全部' })).toBeInTheDocument()
    expect(screen.getByText('笔记 6')).toBeInTheDocument()
    expect(screen.getByText('笔记 2')).toBeInTheDocument()
    expect(screen.queryByText('笔记 1')).not.toBeInTheDocument()

    await fireEvent.click(screen.getByRole('button', { name: '展开全部' }))

    expect(screen.getByRole('button', { name: '收起' })).toBeInTheDocument()
    expect(screen.getByText('笔记 1')).toBeInTheDocument()
  })
})
