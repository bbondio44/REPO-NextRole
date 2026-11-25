import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentHistoryPage } from '@/features/assessment/AssessmentHistoryPage'
import { createAssessmentReevaluation, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt, AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/services/api', () => ({
  createAssessmentReevaluation: vi.fn(),
  getAssessmentHistory: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
}))

function Location() {
  return <output>{useLocation().pathname}</output>
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/habilidades/historial']}>
        <Routes>
          <Route path="/habilidades/historial" element={<><AssessmentHistoryPage /><Location /></>} />
          <Route path="/habilidades/evaluacion" element={<Location />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const history = [{
  attemptId: 'completed-1', completedAt: '2026-11-20T12:00:00Z', questionnaireVersion: 'Q1', matrixVersion: 'M1',
  results: [{ roleId: 'backend-dev-jr', displayPercentage: '68.1', rankPosition: 1 }],
}] as AssessmentHistoryItem[]

describe('AssessmentHistoryPage', () => {
  afterEach(() => cleanup())

  it('lists completed evaluations with a detail link and starts reevaluation on demand', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue(history)
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    vi.mocked(createAssessmentReevaluation).mockResolvedValue({ attemptId: 'draft-1' } as AssessmentAttempt)
    renderPage()

    expect(await screen.findByText(/Cuestionario Q1 · Matriz M1/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver detalle' })).toHaveAttribute('href', '/habilidades/historial/completed-1')
    fireEvent.click(screen.getByRole('button', { name: 'Comenzar nueva evaluación' }))
    await waitFor(() => expect(createAssessmentReevaluation).toHaveBeenCalledTimes(1))
    expect(await screen.findByText('/habilidades/evaluacion')).toBeInTheDocument()
  })

  it('shows recovered draft access and excludes draft entries from the history list', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue(history)
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue({ attemptId: 'draft-2', status: 'DRAFT' } as AssessmentAttempt)
    renderPage()

    expect(await screen.findByRole('link', { name: 'Continuarla' })).toHaveAttribute('href', '/habilidades/evaluacion')
    expect(screen.getAllByRole('link', { name: 'Ver detalle' })).toHaveLength(1)
    expect(screen.getByRole('button', { name: 'Continuar evaluación' })).toBeInTheDocument()
  })
})
