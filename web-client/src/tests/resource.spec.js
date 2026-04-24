import { cleanup, render, screen } from '@testing-library/vue'
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import ResourceView from '../views/ResourceView.vue'

const { searchResourcesSpy } = vi.hoisted(() => ({
  searchResourcesSpy: vi.fn()
}))

vi.mock('../api/resource', () => ({
  searchResources: searchResourcesSpy
}))

describe('ResourceView', () => {
  beforeEach(() => {
    searchResourcesSpy.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  test('renders Chinese summary for resource cards', async () => {
    searchResourcesSpy.mockResolvedValue([
      {
        id: 1,
        title: 'Spring Cloud Alibaba 概览 / Spring Cloud Alibaba Overview',
        description: '梳理 Spring Cloud Alibaba 的核心组件与常见使用场景，适合入门阶段快速建立整体认知。',
        source: 'Spring 官方',
        level: '入门',
        type: 'ARTICLE',
        tags: ['Spring Cloud', 'Microservice'],
        url: 'https://spring.io/projects/spring-cloud'
      }
    ])

    render(ResourceView)

    expect(await screen.findByText('Spring Cloud Alibaba 概览 / Spring Cloud Alibaba Overview')).toBeInTheDocument()
    expect(screen.getByText('梳理 Spring Cloud Alibaba 的核心组件与常见使用场景，适合入门阶段快速建立整体认知。')).toBeInTheDocument()
    expect(screen.getByText('文章来源：Spring 官方')).toBeInTheDocument()
    expect(screen.getByText('适合阶段：入门')).toBeInTheDocument()
  })
})
