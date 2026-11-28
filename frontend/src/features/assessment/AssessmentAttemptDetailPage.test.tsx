import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentAttemptDetailPage } from '@/features/assessment/AssessmentAttemptDetailPage'
import { getAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt, AssessmentRoleResult } from '@/types/domain'

vi.mock('@/services/api', () => ({ getAssessmentAttempt: vi.fn() }))

const resultSets: Record<string, AssessmentRoleResult[]> = {
  'attempt-old': [
    { roleId: 'backend-dev-jr', scoreNumerator: '951', scoreDenominator: '10', displayPercentage: '95.1', rankPosition: 1 },
    { roleId: 'frontend-dev-jr', scoreNumerator: '72', scoreDenominator: '1', displayPercentage: '72.0', rankPosition: 2 },
    { roleId: 'fullstack-dev-jr', scoreNumerator: '64', scoreDenominator: '1', displayPercentage: '64.0', rankPosition: 3 },
    { roleId: 'qa-jr', scoreNumerator: '50', scoreDenominator: '1', displayPercentage: '50.0', rankPosition: 4 },
    { roleId: 'data-analyst-jr', scoreNumerator: '40', scoreDenominator: '1', displayPercentage: '40.0', rankPosition: 5 },
    { roleId: 'cloud-jr', scoreNumerator: '30', scoreDenominator: '1', displayPercentage: '30.0', rankPosition: 6 },
  ],
  'attempt-new': [
    { roleId: 'qa-jr', scoreNumerator: '881', scoreDenominator: '10', displayPercentage: '88.1', rankPosition: 1 },
    { roleId: 'cloud-jr', scoreNumerator: '77', scoreDenominator: '1', displayPercentage: '77.0', rankPosition: 2 },
    { roleId: 'backend-dev-jr', scoreNumerator: '64', scoreDenominator: '1', displayPercentage: '64.0', rankPosition: 3 },
    { roleId: 'frontend-dev-jr', scoreNumerator: '50', scoreDenominator: '1', displayPercentage: '50.0', rankPosition: 4 },
    { roleId: 'fullstack-dev-jr', scoreNumerator: '40', scoreDenominator: '1', displayPercentage: '40.0', rankPosition: 5 },
    { roleId: 'data-analyst-jr', scoreNumerator: '30', scoreDenominator: '1', displayPercentage: '30.0', rankPosition: 6 },
  ],
}

function attempt(attemptId: string): AssessmentAttempt {
  return {
    attemptId,
    status: 'COMPLETED',
    createdAt: '2026-11-20T10:00:00Z',
    completedAt: attemptId === 'attempt-old' ? '2026-11-20T12:00:00Z' : '2026-11-22T12:00:00Z',
    questionnaireVersion: attemptId === 'attempt-old' ? 'Q1' : 'Q2',
    matrixVersion: attemptId === 'attempt-old' ? 'M1' : 'M2',
    answeredCount: 31,
    totalQuestions: 31,
    answers: { 'programming-fundamentals': 2 },
    questions: [{
      competencyCode: 'programming-fundamentals',
      position: 1,
      title: 'Fundamentos de programación',
      question: '¿Qué experiencia tenés utilizando los fundamentos de programación?',
      options: [
        { level: 0, description: 'No conozco estos conceptos.' },
        { level: 2, description: 'Puedo escribir o modificar programas sencillos.' },
      ],
    }],
    results: resultSets[attemptId],
  }
}

function renderDetail(attemptId: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[`/habilidades/historial/${attemptId}`]}>
        <Routes><Route path="/habilidades/historial/:attemptId" element={<AssessmentAttemptDetailPage />} /></Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('AssessmentAttemptDetailPage', () => {
  afterEach(() => cleanup())

  it('shows persisted ranking, evaluation date and pinned versions, with links tied to the same attempt', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt('attempt-old'))
    renderDetail('attempt-old')

    expect(await screen.findByText(/Evaluación realizada el 20 nov 2026/)).toBeInTheDocument()
    expect(screen.getByText('Cuestionario Q1 · Matriz M1')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Backend', level: 2 })).toBeInTheDocument()
    expect(screen.getByText('95.1%')).toBeInTheDocument()
    expect(screen.getByText('72.0%')).toBeInTheDocument()
    expect(screen.getByText('Ver otros roles').closest('details')).not.toHaveAttribute('open')
    expect(screen.getAllByRole('link', { name: /Explorar rol/ })).toHaveLength(6)
    expect(screen.getAllByRole('link', { name: /Explorar rol/ })[0]).toHaveAttribute('href', '/formacion/roles/backend-dev-jr?attemptId=attempt-old')
  })

  it('shows answers from the selected evaluation only after opening the disclosure', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt('attempt-old'))
    renderDetail('attempt-old')

    const answers = await screen.findByText('Ver respuestas de esta evaluación')
    expect(answers.closest('details')).not.toHaveAttribute('open')
    fireEvent.click(answers)
    expect(screen.getByText('¿Qué experiencia tenés utilizando los fundamentos de programación?')).toBeInTheDocument()
    expect(screen.getByText('Puedo escribir o modificar programas sencillos.')).toBeInTheDocument()
  })

  it('loads a different attempt with its own ranking instead of reusing the previous evaluation', async () => {
    vi.mocked(getAssessmentAttempt).mockImplementation(async (attemptId) => attempt(attemptId))
    const first = renderDetail('attempt-old')
    expect(await screen.findByRole('heading', { name: 'Backend', level: 2 })).toBeInTheDocument()
    first.unmount()

    renderDetail('attempt-new')
    expect(await screen.findByRole('heading', { name: 'QA', level: 2 })).toBeInTheDocument()
    expect(screen.getByText('88.1%')).toBeInTheDocument()
    expect(screen.queryByText('95.1%')).not.toBeInTheDocument()
    expect(screen.getByText('Cuestionario Q2 · Matriz M2')).toBeInTheDocument()
  })
})
