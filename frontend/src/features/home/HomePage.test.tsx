import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { useSession } from '@/app/SessionContext'
import { HomePage } from '@/features/home/HomePage'
import {
  createAssessmentAttempt,
  createAssessmentReevaluation,
  discardAssessmentAttempt,
  getAssessmentHistory,
  getCurrentAssessmentAttempt,
} from '@/services/api'
import type { AssessmentAttempt, AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/app/SessionContext', () => ({ useSession: vi.fn() }))
vi.mock('@/services/api', () => ({
  createAssessmentAttempt: vi.fn(),
  createAssessmentReevaluation: vi.fn(),
  discardAssessmentAttempt: vi.fn(),
  getAssessmentHistory: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
}))

function Location() {
  const location = useLocation()
  return <output data-testid="location">{location.pathname}{location.search}</output>
}

function renderHome() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const result = render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<><HomePage /><Location /></>} />
          <Route path="/roles" element={<><p>Resultados históricos</p><Location /></>} />
          <Route path="/habilidades/historial" element={<><p>Historial</p><Location /></>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
  return { ...result, client }
}

const draft: AssessmentAttempt = {
  attemptId: 'draft-1',
  status: 'DRAFT',
  createdAt: '2026-11-20T10:00:00Z',
  completedAt: null,
  questionnaireVersion: 'Q2',
  matrixVersion: 'M2',
  answeredCount: 1,
  totalQuestions: 4,
  answers: { 'programming-fundamentals': 0 },
  questions: [],
}

const completed: AssessmentHistoryItem = {
  attemptId: 'completed-1',
  completedAt: '2026-11-22T12:00:00Z',
  questionnaireVersion: 'Q1',
  matrixVersion: 'M1',
  results: [
    { roleId: 'backend-dev-jr', scoreNumerator: '82', scoreDenominator: '1', displayPercentage: '82.0', rankPosition: 1 },
    { roleId: 'frontend-dev-jr', scoreNumerator: '72', scoreDenominator: '1', displayPercentage: '72.0', rankPosition: 2 },
  ],
}

function configureState(attempt: AssessmentAttempt | null, history: AssessmentHistoryItem[]) {
  vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(attempt)
  vi.mocked(getAssessmentHistory).mockResolvedValue(history)
}

describe('HomePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useSession).mockReturnValue({ account: { firstName: 'Luz' } } as ReturnType<typeof useSession>)
  })

  afterEach(() => cleanup())

  it('shows new-user motivation when there is no draft or completed history', async () => {
    configureState(null, [])
    renderHome()

    expect(await screen.findByText('¡Descubrí tu potencial!')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Hola, Luz/ })).toBeInTheDocument()
    expect(screen.queryByRole('progressbar', { name: 'Progreso de autoevaluación' })).not.toBeInTheDocument()
    expect(screen.queryByText('Tu última evaluación')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Comenzar evaluación/ })).not.toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('shows only the active-draft card for a user without completed history', async () => {
    configureState(draft, [])
    renderHome()

    expect(await screen.findByRole('heading', { name: 'Tu autoevaluación' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toHaveAttribute('aria-valuenow', '25')
    expect(screen.getByText('¡Cada paso cuenta!')).toBeInTheDocument()
    expect(screen.queryByText('25%')).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toHaveAttribute('href', '/habilidades/evaluacion')
    expect(screen.getByRole('button', { name: 'Descartar borrador' })).toBeInTheDocument()
    expect(screen.queryByText('¡Descubrí tu potencial!')).not.toBeInTheDocument()
    expect(screen.queryByText('Tu última evaluación')).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: '¡Cada paso cuenta!' })).toBeInTheDocument()
    expect(screen.getByText('Seguí descubriendo tus habilidades.')).toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('shows recurrent motivation and stored latest results when there is history but no draft', async () => {
    configureState(null, [completed])
    renderHome()

    expect(await screen.findByText('¡Seguí creciendo!')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' })).toBeInTheDocument()
    expect(screen.getByText(/Backend/)).toBeInTheDocument()
    expect(screen.getByText('82.0%')).toBeInTheDocument()
    expect(screen.getByText(/22 de nov de 2026/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
    expect(screen.getByRole('link', { name: 'Ver historial de evaluaciones' })).toHaveAttribute('href', '/habilidades/historial')
    expect(screen.queryByRole('progressbar', { name: 'Progreso de autoevaluación' })).not.toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('shows the active draft and latest completed evaluation together without mixing their data', async () => {
    configureState(draft, [completed])
    renderHome()

    expect(await screen.findByRole('heading', { name: 'Tu autoevaluación' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toHaveAttribute('aria-valuenow', '25')
    expect(screen.getByText('82.0%')).toBeInTheDocument()
    expect(screen.queryByText('¡Seguí creciendo!')).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
  })

  it('keeps a fully answered but unfinished draft at 100% without celebrating', async () => {
    configureState({ ...draft, answeredCount: 4, totalQuestions: 4 }, [completed])
    renderHome()

    expect(await screen.findByRole('progressbar', { name: 'Progreso de autoevaluación' })).toHaveAttribute('aria-valuenow', '100')
    expect(screen.getByText('¡Llegaste a la meta!')).toBeInTheDocument()
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'idle')
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Descartar borrador' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' })).toBeInTheDocument()
  })

  it('selects zero-progress copy by history and uses the approved intermediate progress messages', async () => {
    configureState({ ...draft, answeredCount: 0 }, [])
    const first = renderHome()
    expect(await screen.findByText('¡Empezá a descubrir tu potencial!')).toBeInTheDocument()
    expect(screen.getByText('Conocé tus habilidades y descubrí qué roles se adaptan a vos.')).toBeInTheDocument()
    first.unmount()

    configureState({ ...draft, answeredCount: 0 }, [completed])
    const reevaluation = renderHome()
    expect(await screen.findByText('¡Un nuevo comienzo!')).toBeInTheDocument()
    expect(screen.getByText('Volvé a evaluar tus habilidades y descubrí cuánto avanzaste.')).toBeInTheDocument()
    reevaluation.unmount()

    configureState({ ...draft, answeredCount: 2, totalQuestions: 5 }, [])
    const middle = renderHome()
    expect(await screen.findByText('¡Vas por buen camino!')).toBeInTheDocument()
    expect(screen.getByText('Seguí evaluando tus habilidades para descubrir tu perfil profesional.')).toBeInTheDocument()
    middle.unmount()

    configureState({ ...draft, answeredCount: 3, totalQuestions: 4 }, [])
    renderHome()
    expect(await screen.findByText('¡Ya casi llegás!')).toBeInTheDocument()
    expect(screen.getByText('Te falta poco para conocer tus resultados.')).toBeInTheDocument()
  })

  it('does not render empty states or actions while assessment data is loading', () => {
    vi.mocked(getCurrentAssessmentAttempt).mockReturnValue(new Promise(() => {}))
    vi.mocked(getAssessmentHistory).mockReturnValue(new Promise(() => {}))
    renderHome()

    expect(screen.getByText('Consultando tus evaluaciones…')).toBeInTheDocument()
    expect(screen.queryByText('¡Descubrí tu potencial!')).not.toBeInTheDocument()
    expect(screen.queryByText('¡Seguí creciendo!')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Descartar borrador' })).not.toBeInTheDocument()
  })

  it('does not infer an empty history or expose actions when a request fails', async () => {
    vi.mocked(getCurrentAssessmentAttempt).mockRejectedValue(new Error('offline'))
    vi.mocked(getAssessmentHistory).mockResolvedValue([completed])
    renderHome()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos consultar el estado de tus evaluaciones.')
    expect(screen.queryByText('¡Seguí creciendo!')).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Ver resultados' })).not.toBeInTheDocument()
  })

  it('stays on Home after confirmed discard and reveals motivation without starting another attempt', async () => {
    configureState(draft, [completed])
    vi.mocked(discardAssessmentAttempt).mockResolvedValue(undefined)
    renderHome()

    fireEvent.click(await screen.findByRole('button', { name: 'Descartar borrador' }))
    expect(screen.getByRole('dialog', { name: '¿Descartar autoevaluación?' })).toBeInTheDocument()
    expect(screen.getByText('¿Estás seguro? Se perderá el progreso de este borrador.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(discardAssessmentAttempt).not.toHaveBeenCalled()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Descartar borrador' }))
    fireEvent.click(screen.getByRole('button', { name: /^Descartar$/ }))

    expect(await screen.findByText('¡Seguí creciendo!')).toBeInTheDocument()
    expect(screen.getByTestId('location')).toHaveTextContent('/')
    expect(discardAssessmentAttempt).toHaveBeenCalledWith('draft-1', expect.anything())
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
  })

  it('keeps the draft card and history when backend discard fails', async () => {
    configureState(draft, [completed])
    vi.mocked(discardAssessmentAttempt).mockRejectedValue(new Error('No se pudo descartar'))
    renderHome()

    fireEvent.click(await screen.findByRole('button', { name: 'Descartar borrador' }))
    fireEvent.click(screen.getByRole('button', { name: /^Descartar$/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo descartar')
    expect(screen.getByRole('heading', { name: 'Tu autoevaluación' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toHaveAttribute('aria-valuenow', '25')
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
  })

  it('reflects a server-confirmed completion when shared assessment queries are refreshed', async () => {
    configureState(draft, [completed])
    const { client } = renderHome()
    expect(await screen.findByRole('heading', { name: 'Tu autoevaluación' })).toBeInTheDocument()

    const newest: AssessmentHistoryItem = {
      ...completed,
      attemptId: 'completed-new',
      completedAt: '2026-11-25T12:00:00Z',
      results: [{ ...completed.results[0], displayPercentage: '91.0' }],
    }
    act(() => {
      client.setQueryData(['assessment-attempt'], null)
      client.setQueryData(['assessment-history'], [newest, completed])
    })

    expect(await screen.findByText('¡Seguí creciendo!')).toBeInTheDocument()
    expect(screen.queryByRole('progressbar', { name: 'Progreso de autoevaluación' })).not.toBeInTheDocument()
    expect(screen.getByText('91.0%')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-new')
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
  })
})
