import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { SkillAssessmentPage } from '@/features/assessment/SkillAssessmentPage'
import { completeAssessmentAttempt, getCurrentAssessmentAttempt, saveAssessmentAnswer } from '@/services/api'
import type { AssessmentAttempt, AssessmentQuestion, AssessmentSavedAnswer } from '@/types/domain'

vi.mock('@/services/api', () => ({
  completeAssessmentAttempt: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
  saveAssessmentAnswer: vi.fn(),
}))

const questions: AssessmentQuestion[] = Array.from({ length: 31 }, (_, index) => ({
  competencyCode: `competency-${index + 1}`,
  position: index + 1,
  title: `Competencia ${index + 1}`,
  question: `Pregunta técnica ${index + 1}`,
  options: Array.from({ length: 5 }, (_, level) => ({ level: level as 0 | 1 | 2 | 3 | 4, description: `Descripción específica ${index + 1}, nivel ${level}` })),
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

async function nextQuestion() {
  await screen.findByRole('heading', { name: /Competencia \d+/ })
  fireEvent.click(screen.getByRole('button', { name: 'Siguiente pregunta' }))
}

async function advanceToNextQuestion() {
  await screen.findByRole('heading', { name: /Competencia \d+/ })
  const next = screen.getByRole('button', { name: 'Siguiente pregunta' })
  if (next.hasAttribute('disabled')) {
    fireEvent.click(screen.getByRole('button', { name: 'Inicial' }))
    await waitFor(() => expect(next).toBeEnabled())
  }
  fireEvent.click(next)
}

async function nextQuestions(count: number) {
  for (let index = 0; index < count; index += 1) await advanceToNextQuestion()
}

function progressBar() {
  return screen.getByRole('progressbar', { name: 'Progreso de la evaluación' })
}

describe('SkillAssessmentPage V2', () => {
  afterEach(() => cleanup())

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt())
    vi.mocked(completeAssessmentAttempt).mockImplementation(async (attemptId) => ({
      ...attempt(),
      attemptId,
      status: 'COMPLETED',
      completedAt: '2026-11-21T12:00:00Z',
      results: [],
    }))
    vi.mocked(saveAssessmentAnswer).mockImplementation(async (_attemptId, competencyCode, level) => ({
      competencyCode,
      level,
      answeredCount: 1,
      totalQuestions: 31,
    }))
  })

  it('shows one question, the active area, and area-specific progress without numbered navigation', async () => {
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Desarrollo' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Competencia 1' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Competencia 2' })).not.toBeInTheDocument()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    expect(progressBar()).toHaveAttribute('aria-valuemax', '16')
    expect(progressBar()).toHaveAttribute('aria-valuetext', 'Desarrollo')
    expect(screen.queryByText('0 de 16')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Ir a pregunta/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /temática/i })).not.toBeInTheDocument()
    expect(screen.queryByText(/Pregunta \d+ de 31/)).not.toBeInTheDocument()
  })

  it('blocks moving to the next question until the current answer is saved', async () => {
    renderPage()

    await screen.findByRole('heading', { name: 'Competencia 1' })
    const next = screen.getByRole('button', { name: 'Siguiente pregunta' })
    expect(next).toBeDisabled()
    fireEvent.click(next)
    expect(screen.getByRole('heading', { name: 'Competencia 1' })).toBeInTheDocument()
    expect(saveAssessmentAnswer).not.toHaveBeenCalled()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    expect(screen.getByRole('button', { name: 'Inicial' })).toHaveAttribute('aria-pressed', 'false')
  })

  it('counts level zero only after autosave succeeds and does not advance', async () => {
    renderPage()

    fireEvent.click(await screen.findByRole('button', { name: 'Sin conocimientos' }))
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    await waitFor(() => expect(saveAssessmentAnswer).toHaveBeenCalledWith('attempt-1', 'competency-1', 0))
    await waitFor(() => expect(progressBar()).toHaveAttribute('aria-valuenow', '1'))
    expect(screen.getByRole('heading', { name: 'Competencia 1' })).toBeInTheDocument()
    expect(screen.getByText('Descripción específica 1, nivel 0')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Siguiente pregunta' })).toBeEnabled()
  })

  it.each([
    ['Inicial', 1],
    ['Básico', 2],
    ['Práctico', 3],
    ['Avanzado', 4],
  ] as const)('enables advancement after level %s is saved', async (label, level) => {
    renderPage()

    const next = await screen.findByRole('button', { name: 'Siguiente pregunta' })
    expect(next).toBeDisabled()
    fireEvent.click(screen.getByRole('button', { name: label }))
    await waitFor(() => expect(saveAssessmentAnswer).toHaveBeenCalledWith('attempt-1', 'competency-1', level))
    await waitFor(() => expect(next).toBeEnabled())
  })

  it('allows advancing from an earlier saved answer without selecting it again', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt({ 'competency-1': 0 }))
    renderPage()

    await screen.findByRole('heading', { name: 'Competencia 2' })
    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    await screen.findByRole('heading', { name: 'Competencia 1' })
    const next = await screen.findByRole('button', { name: 'Siguiente pregunta' })
    expect(next).toBeEnabled()
    fireEvent.click(next)
    expect(await screen.findByRole('heading', { name: 'Competencia 2' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    expect(await screen.findByRole('heading', { name: 'Competencia 1' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Sin conocimientos' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: 'Siguiente pregunta' })).toBeEnabled()
  })

  it('does not advance progress when modifying an already answered competency', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt({ 'competency-1': 1 }))
    vi.mocked(saveAssessmentAnswer).mockResolvedValue({
      competencyCode: 'competency-1', level: 4, answeredCount: 1, totalQuestions: 31,
    })
    renderPage()

    await screen.findByRole('heading', { name: 'Competencia 2' })
    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    await screen.findByRole('heading', { name: 'Competencia 1' })
    expect(progressBar()).toHaveAttribute('aria-valuenow', '1')
    fireEvent.click(await screen.findByRole('button', { name: 'Avanzado' }))
    await waitFor(() => expect(saveAssessmentAnswer).toHaveBeenCalledWith('attempt-1', 'competency-1', 4))
    await waitFor(() => expect(screen.getByText('Descripción específica 1, nivel 4')).toBeInTheDocument())
    expect(progressBar()).toHaveAttribute('aria-valuenow', '1')
  })

  it('restores progress from persisted answers and treats level zero as answered', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt({ 'competency-1': 0, 'competency-3': 2 }))
    renderPage()

    await screen.findByRole('heading', { name: 'Competencia 2' })
    expect(progressBar()).toHaveAttribute('aria-valuenow', '2')
    expect(progressBar()).toHaveAttribute('aria-valuemax', '16')
    expect(screen.queryByText('2 de 16')).not.toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    await screen.findByRole('heading', { name: 'Competencia 1' })
    expect(screen.getByRole('button', { name: 'Sin conocimientos' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByText('Descripción específica 1, nivel 0')).toBeInTheDocument()
  })

  it('keeps persisted progress while a save is pending or fails', async () => {
    let resolveSave!: (value: AssessmentSavedAnswer) => void
    vi.mocked(saveAssessmentAnswer).mockReturnValue(new Promise((resolve) => { resolveSave = resolve }))
    renderPage()

    fireEvent.click(await screen.findByRole('button', { name: 'Práctico' }))
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    expect(screen.getByRole('button', { name: 'Siguiente pregunta' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Práctico' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Práctico' })).toHaveAttribute('aria-pressed', 'true')
    resolveSave({ competencyCode: 'competency-1', level: 3, answeredCount: 1, totalQuestions: 31 })
    await waitFor(() => expect(progressBar()).toHaveAttribute('aria-valuenow', '1'))
    expect(screen.getByRole('button', { name: 'Práctico' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Siguiente pregunta' })).toBeEnabled()

    await nextQuestion()
    await screen.findByRole('heading', { name: 'Competencia 2' })
    vi.mocked(saveAssessmentAnswer).mockRejectedValueOnce(new Error('offline'))
    fireEvent.click(screen.getByRole('button', { name: 'Práctico' }))
    await screen.findByRole('alert')
    expect(progressBar()).toHaveAttribute('aria-valuenow', '1')
    expect(screen.getByRole('button', { name: 'Práctico' })).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByRole('button', { name: 'Siguiente pregunta' })).toBeDisabled()
  })

  it('uses a touch-friendly responsive selector and shows only the selected level description', async () => {
    renderPage()

    const levelGroup = await screen.findByRole('group', { name: 'Nivel declarado' })
    expect(levelGroup).toHaveClass(
      'auto-rows-fr', 'grid-cols-2', '@min-[360px]:grid-cols-6', '@min-[640px]:grid-cols-5',
    )
    const practical = screen.getByRole('button', { name: 'Práctico' })
    expect(practical).toHaveClass('min-h-11', 'w-full', 'text-[15px]', '@min-[360px]:col-span-2', '@min-[640px]:col-span-1')
    expect(practical.firstElementChild).toHaveClass('w-full', 'min-w-0', 'whitespace-normal')
    expect(screen.getByRole('button', { name: 'Práctico' })).toHaveClass('@min-[360px]:col-start-2', '@min-[640px]:col-start-auto')
    expect(screen.getByRole('button', { name: 'Avanzado' })).toHaveClass('col-start-2', '@min-[360px]:col-start-4', '@min-[640px]:col-start-auto')
    expect(levelGroup.parentElement).toHaveClass('@container', 'w-full', 'min-w-0', 'max-w-full', 'space-y-4')
    fireEvent.click(practical)
    const selectedDescription = await screen.findByText('Descripción específica 1, nivel 3')
    expect(selectedDescription).toHaveClass('min-w-0', 'break-words', 'text-[15px]')
    expect(screen.queryByText('Descripción específica 1, nivel 2')).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Competencia 1' })).toBeInTheDocument()
    const next = screen.getByRole('button', { name: 'Siguiente pregunta' })
    expect(next.parentElement?.parentElement).toHaveClass('mt-auto', 'pt-4')
    expect(next.closest('main')).toHaveClass('pb-[calc(4.5rem_+_env(safe-area-inset-bottom))]')
    expect(screen.getByRole('heading', { name: 'Competencia 1' })).toHaveClass('text-[22px]', 'sm:text-2xl')
  })

  it('changes area automatically when moving between questions in either direction', async () => {
    renderPage()

    await nextQuestions(16)
    expect(await screen.findByRole('heading', { name: 'QA' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Competencia 17' })).toBeInTheDocument()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    expect(progressBar()).toHaveAttribute('aria-valuemax', '5')
    expect(screen.queryByText('0 de 5')).not.toBeInTheDocument()
    await nextQuestions(5)
    expect(await screen.findByRole('heading', { name: 'Datos' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Competencia 22' })).toBeInTheDocument()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    expect(progressBar()).toHaveAttribute('aria-valuemax', '5')

    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    expect(await screen.findByRole('heading', { name: 'Competencia 21' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'QA' })).toBeInTheDocument()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '5')
    expect(screen.queryByText('5 de 5')).not.toBeInTheDocument()
    expect(progressBar().firstElementChild).toHaveStyle({ width: '100%' })
    fireEvent.click(screen.getByRole('button', { name: 'Siguiente pregunta' }))
    expect(await screen.findByRole('heading', { name: 'Competencia 22' })).toBeInTheDocument()
    expect(screen.queryByText('0 de 5')).not.toBeInTheDocument()

    await nextQuestions(5)
    expect(await screen.findByRole('heading', { name: 'Cloud' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Competencia 27' })).toBeInTheDocument()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '0')
    expect(progressBar()).toHaveAttribute('aria-valuemax', '5')

    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    expect(await screen.findByRole('heading', { name: 'Datos' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Competencia 26' })).toBeInTheDocument()
    expect(progressBar()).toHaveAttribute('aria-valuenow', '5')
    expect(screen.queryByText('5 de 5')).not.toBeInTheDocument()
  })

  it('does not show finalization before the last question and explains pending answers there', async () => {
    renderPage()

    expect(screen.queryByRole('button', { name: 'Finalizar evaluación' })).not.toBeInTheDocument()
    await nextQuestions(30)
    expect(await screen.findByRole('heading', { name: 'Competencia 31' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Finalizar evaluación' })).not.toBeInTheDocument()
    expect(screen.getByText(/Completá las competencias pendientes para finalizar/i)).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /Anterior/ }))
    expect(await screen.findByRole('heading', { name: 'Competencia 30' })).toBeInTheDocument()
  })

  it('shows finalization only on the last question after all 31 answers are saved', async () => {
    const answers = Object.fromEntries(questions.map((question) => [question.competencyCode, 2])) as AssessmentAttempt['answers']
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt(answers))
    renderPage()

    await screen.findByRole('heading', { name: 'Competencia 31' })
    expect(progressBar()).toHaveAttribute('aria-valuenow', '5')
    expect(progressBar()).toHaveAttribute('aria-valuemax', '5')
    expect(screen.queryByText('16 de 16')).not.toBeInTheDocument()
    const submit = await screen.findByRole('button', { name: 'Finalizar evaluación' })
    expect(submit).toBeEnabled()
    fireEvent.click(submit)
    await waitFor(() => expect(completeAssessmentAttempt).toHaveBeenCalledWith('attempt-1'))
  })

  it('keeps the completed answers available and allows retry when finalization fails', async () => {
    const answers = Object.fromEntries(questions.map((question) => [question.competencyCode, 0])) as AssessmentAttempt['answers']
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt(answers))
    vi.mocked(completeAssessmentAttempt).mockRejectedValueOnce(new Error('offline'))
    renderPage()

    const finish = await screen.findByRole('button', { name: 'Finalizar evaluación' })
    fireEvent.click(finish)
    expect(await screen.findByRole('alert')).toHaveTextContent('offline')
    expect(screen.getByRole('button', { name: 'Sin conocimientos' })).toHaveAttribute('aria-pressed', 'true')
    fireEvent.click(screen.getByRole('button', { name: 'Finalizar evaluación' }))
    await waitFor(() => expect(completeAssessmentAttempt).toHaveBeenCalledTimes(2))
  })

  it('does not expose the discard action on the question screen', async () => {
    renderPage()

    await screen.findByRole('heading', { name: 'Competencia 1' })
    expect(screen.queryByRole('button', { name: /Descartar borrador/ })).not.toBeInTheDocument()
  })
})
