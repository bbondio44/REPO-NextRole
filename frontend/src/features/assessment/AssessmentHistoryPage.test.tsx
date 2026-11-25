import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentHistoryPage } from '@/features/assessment/AssessmentHistoryPage'
import { getAssessmentHistory } from '@/services/api'
import type { AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/services/api', () => ({ getAssessmentHistory: vi.fn() }))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/habilidades/historial']}><AssessmentHistoryPage /></MemoryRouter>
    </QueryClientProvider>,
  )
}

const history = [
  {
    attemptId: 'completed-2', completedAt: '2026-11-22T12:00:00Z', questionnaireVersion: 'Q2', matrixVersion: 'M2',
    results: [{ roleId: 'qa-jr', displayPercentage: '72.0', rankPosition: 1 }],
  },
  {
    attemptId: 'completed-1', completedAt: '2026-11-20T12:00:00Z', questionnaireVersion: 'Q1', matrixVersion: 'M1',
    results: [{ roleId: 'backend-dev-jr', displayPercentage: '68.1', rankPosition: 1 }],
  },
] as AssessmentHistoryItem[]

describe('AssessmentHistoryPage', () => {
  afterEach(() => cleanup())

  it('lists completed evaluations with a detail link and no draft management actions', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue(history)
    renderPage()

    expect(await screen.findByText(/Cuestionario Q2 · Matriz M2/)).toBeInTheDocument()
    expect(screen.getByText(/Cuestionario Q1 · Matriz M1/)).toBeInTheDocument()
    const detailLinks = screen.getAllByRole('link', { name: 'Ver detalle' })
    expect(detailLinks).toHaveLength(2)
    expect(detailLinks[0]).toHaveAttribute('href', '/habilidades/historial/completed-2')
    expect(detailLinks[1]).toHaveAttribute('href', '/habilidades/historial/completed-1')
    expect(screen.queryByRole('button', { name: /Continuar evaluación|Comenzar nueva evaluación/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Continuarla' })).not.toBeInTheDocument()
  })

  it('shows an empty state when no completed evaluations exist', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPage()

    expect(await screen.findByText('Todavía no hay evaluaciones completas.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Continuar evaluación|Comenzar nueva evaluación/ })).not.toBeInTheDocument()
  })
})
