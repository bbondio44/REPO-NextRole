import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, fireEvent, render, screen, within } from '@testing-library/react'
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
          <Route path="/habilidades/evaluacion" element={<><p>Cuestionario</p><Location /></>} />
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

function expectDomOrder(...elements: Element[]) {
  for (let index = 1; index < elements.length; index += 1) {
    expect(elements[index - 1].compareDocumentPosition(elements[index]) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
  }
}

describe('HomePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useSession).mockReturnValue({ account: { firstName: 'Luz' } } as ReturnType<typeof useSession>)
  })

  afterEach(() => cleanup())

  it('shows the full path card and circular Play on a first visit', async () => {
    configureState(null, [])
    renderHome()

    expect(await screen.findByText('Tu camino con NextRole')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Hola, Luz/ })).toBeInTheDocument()
    expect(screen.getByRole('main').parentElement).toHaveClass('bg-app-background')
    expect(screen.getByText('Tu próximo paso profesional empieza acá.')).toBeInTheDocument()
    expect(screen.getByText('Tu futuro profesional en cuatro pasos')).toBeInTheDocument()
    expect(screen.queryByText('¡Descubrí tu potencial!')).not.toBeInTheDocument()
    expect(screen.getByText('Conocé tus habilidades')).toBeInTheDocument()
    expect(screen.getByText('Explorá roles IT')).toBeInTheDocument()
    expect(screen.getByText('Identificá qué mejorar')).toBeInTheDocument()
    expect(screen.getByText('Seguí creciendo')).toBeInTheDocument()
    const journey = screen.getByRole('region', { name: 'Tu camino con NextRole' })
    expect(journey.querySelectorAll('article')).toHaveLength(4)
    expect(journey.querySelector('article')).toHaveClass('home-step-card')
    expect(journey.querySelectorAll('a, button')).toHaveLength(0)
    expect(journey.firstElementChild).toHaveClass('home-journey-card')
    expect(journey.querySelectorAll('article p')).toHaveLength(4)
    expect(screen.getByTestId('home-journey-mascot')).toHaveAttribute('data-pose', 'wave')
    expect(screen.getByTestId('home-journey-mascot').querySelector('img')).toHaveClass('h-24', 'w-24')
    const play = screen.getByRole('button', { name: 'Comenzar autoevaluación' })
    expect(play.querySelector('.home-play-button')).toBeInTheDocument()
    expectDomOrder(
      screen.getByRole('heading', { name: /Hola, Luz/ }),
      journey,
      play.closest('.home-start-action')!,
    )
    expect(screen.queryByRole('progressbar', { name: 'Progreso de autoevaluación' })).not.toBeInTheDocument()
    expect(screen.queryByText('Tu última evaluación')).not.toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('shows the active draft followed by compact steps without repeating the mascot or Play', async () => {
    configureState(draft, [])
    renderHome()

    expect(await screen.findByRole('heading', { name: 'Tu autoevaluación' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toHaveAttribute('aria-valuenow', '25')
    expect(screen.getByText('¡Cada paso cuenta!')).toBeInTheDocument()
    expect(screen.queryByText('25%')).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toHaveAttribute('href', '/habilidades/evaluacion')
    expect(screen.queryByRole('button', { name: /Comenzar autoevaluación/ })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Descartar borrador' })).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(screen.queryByText('¡Descubrí tu potencial!')).not.toBeInTheDocument()
    const steps = screen.getByRole('region', { name: 'Tu camino con NextRole' })
    expect(steps.querySelectorAll('article.home-step-card--compact')).toHaveLength(4)
    expect(steps.querySelectorAll('article p')).toHaveLength(0)
    expect(screen.queryByTestId('home-journey-mascot')).not.toBeInTheDocument()
    expect(screen.queryByText('Tu última evaluación')).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: '¡Cada paso cuenta!' })).toBeInTheDocument()
    expect(screen.getByText('Seguí descubriendo tus habilidades.')).toBeInTheDocument()
    expectDomOrder(
      screen.getByRole('heading', { name: /Hola, Luz/ }),
      screen.getByRole('heading', { name: 'Tu autoevaluación' }),
      steps,
    )
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('cancels the introduction from Home without creating or changing an assessment', async () => {
    configureState(null, [])
    renderHome()

    fireEvent.click(await screen.findByRole('button', { name: 'Comenzar autoevaluación' }))
    const dialog = await screen.findByRole('dialog', { name: '¡Descubrí tu potencial!' })
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    fireEvent.click(within(dialog).getByRole('button', { name: 'Ahora no' }))

    expect(screen.queryByRole('dialog', { name: '¡Descubrí tu potencial!' })).not.toBeInTheDocument()
    expect(screen.getByTestId('location')).toHaveTextContent('/')
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
  })

  it('shows the introduction first and starts an initial assessment only after confirmation', async () => {
    configureState(null, [])
    let resolveAttempt: ((value: AssessmentAttempt) => void) | undefined
    vi.mocked(createAssessmentAttempt).mockReturnValue(new Promise((resolve) => { resolveAttempt = resolve }))
    renderHome()

    const start = await screen.findByRole('button', { name: 'Comenzar autoevaluación' })
    fireEvent.click(start)
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    const dialog = await screen.findByRole('dialog', { name: '¡Descubrí tu potencial!' })
    const confirm = within(dialog).getByRole('button', { name: 'Comenzar autoevaluación' })
    fireEvent.click(confirm)
    fireEvent.click(confirm)
    expect(createAssessmentAttempt).toHaveBeenCalledTimes(1)
    expect(confirm).toBeDisabled()
    expect(within(dialog).getByRole('button', { name: 'Preparando evaluación…' })).toBeDisabled()

    await act(async () => resolveAttempt?.(draft))
    expect(await screen.findByText('Cuestionario')).toBeInTheDocument()
    expect(screen.getByTestId('location')).toHaveTextContent('/habilidades/evaluacion')
  })

  it('reports a start error and lets the user retry from Home', async () => {
    configureState(null, [])
    vi.mocked(createAssessmentAttempt).mockRejectedValueOnce(new Error('No pudimos iniciar por conexión.'))
    renderHome()

    fireEvent.click(await screen.findByRole('button', { name: 'Comenzar autoevaluación' }))
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    fireEvent.click(within(await screen.findByRole('dialog')).getByRole('button', { name: 'Comenzar autoevaluación' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos iniciar por conexión.')
    expect(within(screen.getByRole('dialog')).getByRole('button', { name: 'Comenzar autoevaluación' })).toBeEnabled()
    expect(screen.getByTestId('location')).toHaveTextContent('/')
  })

  it('shows compact steps with the thumbs-up mascot, then latest results and Play for a returning user', async () => {
    configureState(null, [completed])
    renderHome()

    expect(await screen.findByText('Descubrí tu potencial y seguí creciendo')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: '¡Seguí creciendo!' })).not.toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' })).toBeInTheDocument()
    expect(screen.getByText(/Backend/)).toBeInTheDocument()
    expect(screen.getByText('82.0%')).toBeInTheDocument()
    expect(screen.getByText(/22 de nov de 2026/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
    expect(screen.getByRole('link', { name: 'Ver historial de evaluaciones' })).toHaveAttribute('href', '/habilidades/historial')
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' }).closest('.home-secondary-card')).toBeInTheDocument()
    expect(screen.getByText('82.0%').closest('.home-info-panel')).toBeInTheDocument()
    const play = screen.getByRole('button', { name: 'Comenzar nueva autoevaluación' })
    expect(play.querySelector('.home-play-button')).toBeInTheDocument()
    const steps = screen.getByRole('region', { name: 'Tu camino con NextRole' })
    expect(steps.querySelectorAll('article.home-step-card--compact')).toHaveLength(4)
    expect(steps.querySelectorAll('article p')).toHaveLength(0)
    expect(screen.getByTestId('home-journey-mascot')).toHaveAttribute('data-pose', 'thumbsUp')
    const latest = screen.getByRole('heading', { name: 'Tu última evaluación' }).closest('.home-secondary-card')!
    expect(latest.querySelector('.home-start-action')).toBeNull()
    expectDomOrder(
      screen.getByRole('heading', { name: /Hola, Luz/ }),
      steps,
      latest,
      play.closest('.home-start-action')!,
    )
    expect(screen.queryByRole('progressbar', { name: 'Progreso de autoevaluación' })).not.toBeInTheDocument()
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('opens the introduction for a returning Home user before creating a reevaluation', async () => {
    configureState(null, [completed])
    vi.mocked(createAssessmentReevaluation).mockResolvedValue(draft)
    renderHome()

    fireEvent.click(await screen.findByRole('button', { name: 'Comenzar nueva autoevaluación' }))
    const dialog = await screen.findByRole('dialog', { name: '¡Descubrí tu potencial!' })
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
    fireEvent.click(within(dialog).getByRole('button', { name: 'Comenzar autoevaluación' }))

    await screen.findByText('Cuestionario')
    expect(createAssessmentReevaluation).toHaveBeenCalledTimes(1)
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
  })

  it('shows the active draft and latest completed evaluation together without mixing their data', async () => {
    configureState(draft, [completed])
    renderHome()

    expect(await screen.findByRole('heading', { name: 'Tu autoevaluación' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu última evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })).toHaveAttribute('aria-valuenow', '25')
    expect(screen.getByText('82.0%')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    const steps = screen.getByRole('region', { name: 'Tu camino con NextRole' })
    expect(steps.querySelectorAll('article.home-step-card--compact')).toHaveLength(4)
    expect(steps.querySelectorAll('article p')).toHaveLength(0)
    expect(screen.queryByTestId('home-journey-mascot')).not.toBeInTheDocument()
    const latest = screen.getByRole('heading', { name: 'Tu última evaluación' }).closest('.home-secondary-card')!
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-1')
    expect(screen.queryByRole('button', { name: /Comenzar/ })).not.toBeInTheDocument()
    expectDomOrder(
      screen.getByRole('heading', { name: /Hola, Luz/ }),
      screen.getByRole('heading', { name: 'Tu autoevaluación' }),
      steps,
      latest,
    )
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

    expect(await screen.findByText('Descubrí tu potencial y seguí creciendo')).toBeInTheDocument()
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

    expect(await screen.findByText('Descubrí tu potencial y seguí creciendo')).toBeInTheDocument()
    expect(screen.queryByRole('progressbar', { name: 'Progreso de autoevaluación' })).not.toBeInTheDocument()
    expect(screen.getByText('91.0%')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver resultados' })).toHaveAttribute('href', '/roles?attemptId=completed-new')
    expect(createAssessmentAttempt).not.toHaveBeenCalled()
    expect(createAssessmentReevaluation).not.toHaveBeenCalled()
  })
})
