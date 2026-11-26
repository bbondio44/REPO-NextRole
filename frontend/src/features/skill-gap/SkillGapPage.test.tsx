import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

import { SkillGapPage } from '@/features/skill-gap/SkillGapPage'
import { getRoleById, getSkillGap } from '@/services/api'

vi.mock('@/services/api', () => ({ getRoleById: vi.fn(), getSkillGap: vi.fn() }))

describe('SkillGapPage', () => {
  afterEach(() => cleanup())

  it('shows the diagnostic and competency progress without the obsolete orientation indicators', async () => {
    vi.mocked(getRoleById).mockResolvedValue({
      id: 'backend-dev-jr',
      name: 'Backend Developer Jr.',
      area: 'Desarrollo',
      icon: 'code',
      description: 'Construye servicios.',
      responsibilities: [],
      demandTag: 'Alta',
    })
    vi.mocked(getSkillGap).mockResolvedValue([{
      skill: { id: 'programming-fundamentals', categoryId: 'programming', name: 'Fundamentos de programación', description: 'Bases.' },
      requiredLevel: 3,
      currentLevel: 2,
      status: 'parcial',
      priority: 'alta',
    }])

    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    const { container } = render(
      <QueryClientProvider client={client}>
        <MemoryRouter initialEntries={['/roles/backend-dev-jr/brechas']}>
          <Routes><Route path="/roles/:roleId/brechas" element={<SkillGapPage />} /></Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect(await screen.findByRole('heading', { name: 'Comparativa de brechas' })).toBeInTheDocument()
    expect(await screen.findByText('Fundamentos de programación')).toBeInTheDocument()
    expect(screen.getByText('Requerido: Intermedio')).toBeInTheDocument()
    expect(screen.getByText('Tu nivel: Básico')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver cursos recomendados →' })).toHaveAttribute('href', '/roles/backend-dev-jr/formacion')
    expect(screen.queryByText(/PASO 5 DE 6/)).not.toBeInTheDocument()
    expect(container.querySelectorAll('.h-1\\.5')).toHaveLength(0)
    expect(container.querySelector('.h-2')).toBeInTheDocument()
    expect(getRoleById).toHaveBeenCalledWith('backend-dev-jr')
    expect(getSkillGap).toHaveBeenCalledWith('backend-dev-jr')
  })
})
