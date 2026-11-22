import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { SkillSummaryPage } from '@/features/assessment/SkillSummaryPage'
import { getAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

vi.mock('@/services/api', () => ({ getAssessmentAttempt: vi.fn() }))

const completedAttempt: AssessmentAttempt = {
  attemptId: 'attempt-42',
  status: 'COMPLETED',
  createdAt: '2026-11-21T10:00:00Z',
  completedAt: '2026-11-21T10:10:00Z',
  questionnaireVersion: 'Q1',
  matrixVersion: 'M1',
  answeredCount: 31,
  totalQuestions: 31,
  answers: { 'programming-fundamentals': 0 },
  questions: [{
    competencyCode: 'programming-fundamentals',
    position: 1,
    title: 'Fundamentos de programación',
    question: '¿Qué experiencia tenés?',
    options: [
      { level: 0, description: 'Todavía no conozco estos conceptos.' },
      { level: 1, description: 'Reconozco los conceptos básicos.' },
    ],
  }],
  results: [{
    roleId: 'backend-dev-jr',
    scoreNumerator: '681',
    scoreDenominator: '10',
    displayPercentage: '68.1',
    rankPosition: 1,
  }],
}

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/habilidades/resumen?attemptId=attempt-42']}>
        <SkillSummaryPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('SkillSummaryPage V2', () => {
  afterEach(() => cleanup())

  it('shows declared V2 levels and descriptions separately from role matching', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    renderPage()

    expect(await screen.findByText('Nivel 0: Sin conocimientos')).toBeInTheDocument()
    expect(screen.getByText('Todavía no conozco estos conceptos.')).toBeInTheDocument()
    expect(screen.getByText(/No es una certificación/)).toBeInTheDocument()
    expect(screen.queryByText('68.1%')).not.toBeInTheDocument()
    expect(screen.queryByText(/General \d+%/)).not.toBeInTheDocument()
  })

  it('carries the completed attempt ID into the matching flow', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    renderPage()

    expect(await screen.findByRole('link', { name: /matching para los seis roles/i }))
      .toHaveAttribute('href', '/roles?attemptId=attempt-42')
  })
})
