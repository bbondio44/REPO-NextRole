import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { StrictMode } from 'react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { RoleFormationPage } from '@/features/explainability/RoleFormationPage'
import { generateDiagnosis, getAssessmentAttempt, getDiagnosis, getRoleCatalogEntry, retryDiagnosis } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

vi.mock('@/services/api', () => ({
  generateDiagnosis: vi.fn(),
  getAssessmentAttempt: vi.fn(),
  getDiagnosis: vi.fn(),
  getRoleCatalogEntry: vi.fn(),
  retryDiagnosis: vi.fn(),
}))

const attempt: AssessmentAttempt = {
  attemptId: 'historic-22', status: 'COMPLETED', createdAt: '2026-11-20T10:00:00Z',
  completedAt: '2026-11-20T12:00:00Z', questionnaireVersion: 'Q1', matrixVersion: 'M1',
  answeredCount: 31, totalQuestions: 31, answers: {}, questions: [],
  results: [{ roleId: 'qa-jr', scoreNumerator: '72', scoreDenominator: '1', displayPercentage: '72', rankPosition: 1 }],
}

const diagnosis = {
  currentSituation: 'Tenés una base sólida para el rol.',
  recommendedFocus: 'Profundizar en pruebas de APIs.',
  importanceExplanation: 'Permite verificar servicios.',
  professionalRelevance: 'Se aplica al validar integraciones.',
  highlightedCompetencyIds: ['api-testing'],
}

const success = {
  attemptId: 'historic-22', roleId: 'qa-jr', status: 'SUCCEEDED', roleName: 'QA histórico',
  roleDescription: 'Descripción histórica del rol.', diagnosis, model: 'model', promptVersion: 'v5',
  generatedAt: '2026-11-20T12:30:00Z',
} as const

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false }, mutations: { retry: false } } })
  const view = render(
    <QueryClientProvider client={client}>
      <StrictMode>
        <MemoryRouter initialEntries={['/formacion/roles/qa-jr?attemptId=historic-22']}>
          <Routes><Route path="/formacion/roles/:roleId" element={<RoleFormationPage />} /></Routes>
        </MemoryRouter>
      </StrictMode>
    </QueryClientProvider>,
  )
  return { ...view, client }
}

describe('RoleFormationPage', () => {
  afterEach(() => { cleanup(); vi.clearAllMocks() })

  it('shows deterministic historical header and saved diagnosis without generating again', async () => {
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA vigente', description: 'Descripción vigente.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue(success)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'QA histórico' })).toBeInTheDocument()
    expect(screen.getByText('72%')).toBeInTheDocument()
    expect(screen.getByText(/20 nov 2026/)).toBeInTheDocument()
    expect(screen.getByText('Cuestionario Q1 · Matriz M1')).toBeInTheDocument()
    expect(screen.getByText('Descripción histórica del rol.')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Emblema de QA' })).toBeInTheDocument()
    expect(screen.getByRole('progressbar', { name: 'Porcentaje de preparación' })).toHaveAttribute('aria-valuenow', '72')
    expect(screen.getByRole('heading', { name: 'Tu situación actual' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'En qué deberías enfocarte' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Por qué es importante' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Cómo te acerca al mundo laboral' })).toBeInTheDocument()
    expect(screen.getByText(diagnosis.currentSituation)).toBeInTheDocument()
    expect(document.querySelector('img[aria-hidden="true"]')?.getAttribute('src')).toContain('mascot-pointing')
    expect(screen.getByRole('heading', { name: 'Tu plan de formación' })).toBeInTheDocument()
    const backLink = screen.getByRole('link', { name: 'Volver' })
    expect(backLink).toHaveAttribute('href', '/roles?attemptId=historic-22')
    backLink.focus()
    expect(document.activeElement).toBe(backLink)
    expect(await screen.findByRole('link', { name: 'Formación' })).toHaveAttribute('aria-current', 'page')
    expect(generateDiagnosis).not.toHaveBeenCalled()
  })

  it('starts initial generation once from NOT_STARTED and keeps deterministic content visible', async () => {
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'NOT_STARTED' })
    vi.mocked(generateDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'GENERATING', retryAfterSeconds: 2 })
    renderPage()

    expect(await screen.findByText('Estamos preparando tu diagnóstico')).toBeInTheDocument()
    expect(document.querySelector('img[aria-hidden="true"]')).toHaveClass('motion-safe:animate-pulse', 'motion-reduce:animate-none')
    expect(screen.getByRole('heading', { name: 'QA' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu plan de formación' })).toBeInTheDocument()
    expect(screen.getByText('Próximamente')).toBeInTheDocument()
    expect(screen.queryByText(diagnosis.currentSituation)).not.toBeInTheDocument()
    await waitFor(() => expect(generateDiagnosis).toHaveBeenCalledTimes(1))
  })

  it('does not send another generation request when the role page remounts', async () => {
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'NOT_STARTED' })
    vi.mocked(generateDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'GENERATING', retryAfterSeconds: 2 })
    const first = renderPage()
    expect(await screen.findByText('Estamos preparando tu diagnóstico')).toBeInTheDocument()
    await waitFor(() => expect(generateDiagnosis).toHaveBeenCalledTimes(1))
    first.unmount()

    render(
      <QueryClientProvider client={first.client}>
        <StrictMode>
          <MemoryRouter initialEntries={['/formacion/roles/qa-jr?attemptId=historic-22']}>
            <Routes><Route path="/formacion/roles/:roleId" element={<RoleFormationPage />} /></Routes>
          </MemoryRouter>
        </StrictMode>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('Estamos preparando tu diagnóstico')).toBeInTheDocument()
    expect(generateDiagnosis).toHaveBeenCalledTimes(1)
    expect(getDiagnosis).toHaveBeenCalled()
  })

  it('only retries a failed diagnosis after the user activates the retry button', async () => {
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'FAILED', failureCode: 'PROVIDER_ERROR', canRetry: true })
    vi.mocked(retryDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'GENERATING', retryAfterSeconds: 2 })
    renderPage()

    expect(await screen.findByText('No pudimos preparar tu diagnóstico')).toBeInTheDocument()
    expect(screen.getByText('Tus resultados están guardados. Podés volver a intentarlo.')).toBeInTheDocument()
    expect(retryDiagnosis).not.toHaveBeenCalled()
    expect(screen.getByText('Próximamente')).toBeInTheDocument()
    expect(screen.queryByText(diagnosis.currentSituation)).not.toBeInTheDocument()
    fireEvent.focus(window)
    expect(retryDiagnosis).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: 'Volver a intentar' }))
    await waitFor(() => expect(retryDiagnosis).toHaveBeenCalledTimes(1))
    expect(generateDiagnosis).not.toHaveBeenCalled()
  })

  it('keeps the future formation card separate in loading, success and failure states', async () => {
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'NOT_STARTED' })
    vi.mocked(generateDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'GENERATING', retryAfterSeconds: 2 })
    const loading = renderPage()
    const loadingPlan = await screen.findByRole('region', { name: 'Tu plan de formación' })
    expect(loadingPlan).toHaveTextContent('Próximamente')
    expect(loadingPlan).not.toHaveTextContent(diagnosis.currentSituation)
    loading.unmount()

    vi.clearAllMocks()
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue(success)
    const completed = renderPage()
    const successPlan = await screen.findByRole('region', { name: 'Tu plan de formación' })
    expect(successPlan.textContent).toBe('Tu plan de formaciónPróximamente')
    expect(successPlan).not.toHaveTextContent(diagnosis.recommendedFocus)
    completed.unmount()

    vi.clearAllMocks()
    vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
    vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
    vi.mocked(getDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'FAILED', failureCode: 'PROVIDER_ERROR', canRetry: true })
    renderPage()
    const errorPlan = await screen.findByRole('region', { name: 'Tu plan de formación' })
    expect(errorPlan.textContent).toBe('Tu plan de formaciónPróximamente')
    expect(errorPlan).not.toHaveTextContent('No pudimos preparar tu diagnóstico')
  })

  it('polls a generating diagnosis and stops visual waiting after two minutes without inventing progress', async () => {
    const nativeSetTimeout = window.setTimeout.bind(window)
    let waitLimitCallback: (() => void) | undefined
    const setTimeoutSpy = vi.spyOn(window, 'setTimeout').mockImplementation((handler, timeout, ...args) => {
      if (timeout === 120_000) {
        waitLimitCallback = handler as () => void
        return 0
      }
      return nativeSetTimeout(handler, timeout, ...args)
    })
    try {
      vi.mocked(getRoleCatalogEntry).mockResolvedValue({ roleId: 'qa-jr', name: 'QA', description: 'Rol QA.' })
      vi.mocked(getAssessmentAttempt).mockResolvedValue(attempt)
      vi.mocked(getDiagnosis).mockResolvedValue({ attemptId: 'historic-22', roleId: 'qa-jr', status: 'GENERATING', retryAfterSeconds: 2 })
      const view = renderPage()
      expect(await screen.findByText('Estamos preparando tu diagnóstico')).toBeInTheDocument()
      await new Promise((resolve) => nativeSetTimeout(resolve, 2_100))
      expect(getDiagnosis).toHaveBeenCalledTimes(2)
      expect(waitLimitCallback).toBeDefined()
      act(() => waitLimitCallback?.())
      expect(screen.getByText('Tu diagnóstico continúa en preparación')).toBeInTheDocument()
      expect(screen.queryByRole('progressbar', { name: /diagnóstico/i })).not.toBeInTheDocument()
      view.unmount()
    } finally {
      setTimeoutSpy.mockRestore()
    }
  })
})
