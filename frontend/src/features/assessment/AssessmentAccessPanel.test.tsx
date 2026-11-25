import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentAccessPanel } from '@/features/assessment/AssessmentAccessPanel'
import { createAssessmentAttempt, createAssessmentReevaluation, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt, AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/services/api', () => ({
  createAssessmentAttempt: vi.fn(),
  createAssessmentReevaluation: vi.fn(),
  getAssessmentHistory: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
}))

function Location() {
  const location = useLocation()
  return <output>{location.pathname}</output>
}

function renderPanel() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<><AssessmentAccessPanel /><Location /></>} />
          <Route path="/habilidades/evaluacion" element={<Location />} />
          <Route path="/habilidades/historial" element={<Location />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const draft = { attemptId: 'draft-1', status: 'DRAFT' } as AssessmentAttempt
const completed = { attemptId: 'completed-1' } as AssessmentHistoryItem

describe('AssessmentAccessPanel', () => {
  afterEach(() => cleanup())

  it('offers resume for the active draft without creating a duplicate attempt', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPanel()

    expect(await screen.findByRole('link', { name: 'Continuar evaluación' })).toHaveAttribute('href', '/habilidades/evaluacion')
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('shows the latest completed results while a separate draft is in progress', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    renderPanel()

    expect(await screen.findByRole('link', { name: 'Ver mis resultados anteriores' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('starts the first assessment only after explicit user action', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    vi.mocked(createAssessmentAttempt).mockResolvedValue(draft)
    renderPanel()

    fireEvent.click(await screen.findByRole('button', { name: 'Comenzar evaluación' }))
    await waitFor(() => expect(createAssessmentAttempt).toHaveBeenCalledTimes(1))
    expect(await screen.findByText('/habilidades/evaluacion')).toBeInTheDocument()
  })

  it('shows completed results without creating a draft when returning to the tabs', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    renderPanel()

    expect(await screen.findByRole('link', { name: 'Ver mis resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(await screen.findByRole('button', { name: 'Comenzar nueva evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver historial de evaluaciones' })).toHaveAttribute('href', '/habilidades/historial')
  })

  it('creates a prefilled reevaluation only after explicit action', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    vi.mocked(createAssessmentReevaluation).mockResolvedValue(draft)
    renderPanel()

    fireEvent.click(await screen.findByRole('button', { name: 'Comenzar nueva evaluación' }))
    await waitFor(() => expect(createAssessmentReevaluation).toHaveBeenCalledTimes(1))
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(await screen.findByText('/habilidades/evaluacion')).toBeInTheDocument()
  })
})
