import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentAccessPanel } from '@/features/assessment/AssessmentAccessPanel'
import { createAssessmentAttempt, createAssessmentReevaluation, discardAssessmentAttempt, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt, AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/services/api', () => ({
  createAssessmentAttempt: vi.fn(),
  createAssessmentReevaluation: vi.fn(),
  discardAssessmentAttempt: vi.fn(),
  getAssessmentHistory: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
}))

function Location() {
  const location = useLocation()
  return <output>{location.pathname}</output>
}

function renderPanel(showDraftDiscardAction = false) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/habilidades']}>
        <Routes>
          <Route path="/habilidades" element={<><AssessmentAccessPanel showDraftDiscardAction={showDraftDiscardAction} /><Location /></>} />
          <Route path="/habilidades/evaluacion" element={<Location />} />
          <Route path="/habilidades/historial" element={<Location />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const draft = {
  attemptId: 'draft-1',
  status: 'DRAFT',
  createdAt: '2026-01-01T00:00:00Z',
  completedAt: null,
  questionnaireVersion: 'v1',
  matrixVersion: 'v1',
  answeredCount: 1,
  totalQuestions: 4,
  answers: { 'programming-fundamentals': 0 },
  questions: [],
} as AssessmentAttempt
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

  it('uses the active attempt saved-answer ratio, including an explicit zero, without extra data requests', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPanel()

    const progress = await screen.findByRole('progressbar', { name: 'Progreso de autoevaluación' })
    expect(progress).toHaveAttribute('aria-valuenow', '25')
    expect(screen.queryByText('25%')).not.toBeInTheDocument()
    expect(getCurrentAssessmentAttempt).toHaveBeenCalledTimes(1)
    expect(getAssessmentHistory).toHaveBeenCalledTimes(1)
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'idle')
  })

  it('shows the first-evaluation mascot without a journey when no draft or history exists', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPanel()

    expect(await screen.findByRole('heading', { name: 'Descubrí tus habilidades' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Comenzar evaluación' })).toBeInTheDocument()
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument()
    expect(getCurrentAssessmentAttempt).toHaveBeenCalledTimes(1)
    expect(getAssessmentHistory).toHaveBeenCalledTimes(1)
  })

  it('shows the recurring static mascot and history link without a journey after completion', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    renderPanel()

    expect(await screen.findByRole('heading', { name: 'Seguí evolucionando' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver historial de evaluaciones' })).toHaveAttribute('href', '/habilidades/historial')
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument()
  })

  it('keeps a completed but unfinalized draft at 100% without celebrating', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue({ ...draft, answeredCount: 4, totalQuestions: 4 })
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPanel()

    const progress = await screen.findByRole('progressbar', { name: 'Progreso de autoevaluación' })
    expect(progress).toHaveAttribute('aria-valuenow', '100')
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'idle')
  })

  it('represents the active draft instead of previous completed results', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    renderPanel()

    const progress = await screen.findByRole('progressbar', { name: 'Progreso de autoevaluación' })
    expect(progress).toHaveAttribute('aria-valuenow', '25')
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'idle')
  })

  it('keeps completed evaluations accessible through history while a separate draft is in progress', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    renderPanel()

    expect(await screen.findByRole('link', { name: 'Ver historial de evaluaciones' })).toHaveAttribute('href', '/habilidades/historial')
    expect(screen.queryByRole('link', { name: /Ver mis resultados/ })).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('hides draft discard when the panel action is not enabled', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPanel()

    await screen.findByRole('link', { name: 'Continuar evaluación' })
    expect(screen.queryByRole('button', { name: 'Descartar borrador' })).not.toBeInTheDocument()
  })

  it('discards from Habilidades only after confirmation and leaves starting a new evaluation explicit', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    vi.mocked(discardAssessmentAttempt).mockResolvedValue(undefined)
    renderPanel(true)

    fireEvent.click(await screen.findByRole('button', { name: 'Descartar borrador' }))
    expect(screen.getByRole('dialog', { name: '¿Descartar autoevaluación?' })).toBeInTheDocument()
    expect(screen.getByText('¿Estás seguro? Se perderá el progreso de este borrador.')).toBeInTheDocument()
    expect(discardAssessmentAttempt).not.toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(screen.queryByText(/Se perderá el progreso guardado de este borrador/)).not.toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'Continuar evaluación' })).toBeInTheDocument()
    expect(discardAssessmentAttempt).not.toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', { name: 'Descartar borrador' }))
    fireEvent.click(screen.getByRole('button', { name: /^Descartar$/ }))

    await waitFor(() => expect(discardAssessmentAttempt).toHaveBeenCalledWith('draft-1', expect.anything()))
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(await screen.findByRole('button', { name: 'Comenzar nueva evaluación' })).toBeInTheDocument()
    expect(await screen.findByText('/habilidades')).toBeInTheDocument()
  })

  it('allows keyboard dismissal of the accessible discard dialog without changing the draft', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    renderPanel(true)

    fireEvent.click(await screen.findByRole('button', { name: 'Descartar borrador' }))
    expect(screen.getByRole('dialog', { name: '¿Descartar autoevaluación?' })).toBeInTheDocument()
    fireEvent.keyDown(window, { key: 'Escape' })

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toBeInTheDocument()
    expect(discardAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('keeps the draft visible and reports a backend error when discard fails', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    vi.mocked(discardAssessmentAttempt).mockRejectedValue(new Error('No se pudo descartar'))
    renderPanel(true)

    fireEvent.click(await screen.findByRole('button', { name: 'Descartar borrador' }))
    fireEvent.click(screen.getByRole('button', { name: /^Descartar$/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo descartar')
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: '¿Descartar autoevaluación?' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /^Descartar$/ })).toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
  })

  it('does not submit a second discard while the first request is pending', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(draft)
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    let resolveDiscard: (() => void) | undefined
    vi.mocked(discardAssessmentAttempt).mockReturnValue(new Promise<void>((resolve) => { resolveDiscard = resolve }))
    renderPanel(true)

    fireEvent.click(await screen.findByRole('button', { name: 'Descartar borrador' }))
    const confirmButton = screen.getByRole('button', { name: /^Descartar$/ })
    expect(confirmButton).not.toBeDisabled()
    fireEvent.click(confirmButton)
    await waitFor(() => expect(discardAssessmentAttempt).toHaveBeenCalledTimes(1))
    fireEvent.click(confirmButton)
    expect(discardAssessmentAttempt).toHaveBeenCalledTimes(1)
    resolveDiscard?.()
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
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

    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(await screen.findByRole('button', { name: 'Comenzar nueva evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver historial de evaluaciones' })).toHaveAttribute('href', '/habilidades/historial')
  })

  it('creates a new empty reevaluation only after explicit action', async () => {
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
