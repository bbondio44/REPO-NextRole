import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { SkillAssessmentPage } from '@/features/assessment/SkillAssessmentPage'
import { getOrStartAssessment, saveAssessmentAnswer } from '@/services/api'
import type { AssessmentAttempt, AssessmentQuestion } from '@/types/domain'

vi.mock('@/services/api', () => ({
  getOrStartAssessment: vi.fn(),
  saveAssessmentAnswer: vi.fn(),
}))

const questions: AssessmentQuestion[] = Array.from({ length: 31 }, (_, index) => ({
  competencyCode: `competency-${index + 1}`,
  position: index + 1,
  title: `Competencia ${index + 1}`,
  question: `Pregunta técnica ${index + 1}`,
  options: Array.from({ length: 5 }, (_, level) => ({ level: level as 0 | 1 | 2 | 3 | 4, description: `Descripción nivel ${level}` })),
}))

function attempt(answers: Record<string, AssessmentAttempt['answers'][string]> = {}): AssessmentAttempt {
  return {
    attemptId: 'attempt-1',
    status: 'DRAFT',
    createdAt: '2026-11-20T12:00:00Z',
    completedAt: null,
    questionnaireVersion: 'Q1',
    matrixVersion: 'M1',
    answeredCount: Object.keys(answers).length,
    totalQuestions: 31,
    answers,
    questions,
  }
}

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter><SkillAssessmentPage /></MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('SkillAssessmentPage V2', () => {
  afterEach(() => cleanup())

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(getOrStartAssessment).mockResolvedValue(attempt())
    vi.mocked(saveAssessmentAnswer).mockImplementation(async (_attemptId, competencyCode, level) => ({
      competencyCode,
      level,
      answeredCount: 1,
      totalQuestions: 31,
    }))
  })

  it('renders 31 catalog questions and five levels for each question', async () => {
    renderPage()

    expect(await screen.findByText('Competencia 1')).toBeInTheDocument()
    expect(screen.getByText('Competencia 31')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /Nivel [0-4]:/ })).toHaveLength(31 * 5)
  })

  it('saves level zero as an explicit answer', async () => {
    renderPage()

    const levelZero = await screen.findAllByRole('button', { name: /Nivel 0:/ })
    fireEvent.click(levelZero[0])

    await waitFor(() => expect(saveAssessmentAnswer).toHaveBeenCalledWith('attempt-1', 'competency-1', 0))
  })

  it('restores a persisted level zero when recovering the draft', async () => {
    vi.mocked(getOrStartAssessment).mockResolvedValue(attempt({ 'competency-1': 0 }))
    renderPage()

    const levelZero = await screen.findAllByRole('button', { name: /Nivel 0:/ })
    expect(levelZero[0]).toHaveAttribute('aria-pressed', 'true')
    expect(levelZero[1]).toHaveAttribute('aria-pressed', 'false')
  })

  it('keeps results navigation disabled while the attempt is a draft', async () => {
    renderPage()

    expect(await screen.findByRole('button', { name: /Ver resultados/ })).toBeDisabled()
  })
})
