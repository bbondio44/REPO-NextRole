import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { RolesSuggestedPage } from '@/features/roles/RolesSuggestedPage'
import { getAssessmentAttempt, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt, AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/services/api', () => ({
  getAssessmentAttempt: vi.fn(),
  getAssessmentHistory: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
}))

const completedAttempt: AssessmentAttempt = {
  attemptId: 'attempt-77',
  status: 'COMPLETED',
  createdAt: '2026-11-21T10:00:00Z',
  completedAt: '2026-11-21T10:10:00Z',
  questionnaireVersion: 'Q1',
  matrixVersion: 'M1',
  answeredCount: 31,
  totalQuestions: 31,
  answers: {},
  questions: [],
  results: [
    { roleId: 'backend-dev-jr', scoreNumerator: '95', scoreDenominator: '1', displayPercentage: '95.0', rankPosition: 1 },
    { roleId: 'frontend-dev-jr', scoreNumerator: '70', scoreDenominator: '1', displayPercentage: '70.0', rankPosition: 2 },
    { roleId: 'fullstack-dev-jr', scoreNumerator: '60', scoreDenominator: '1', displayPercentage: '60.0', rankPosition: 3 },
    { roleId: 'qa-jr', scoreNumerator: '55', scoreDenominator: '1', displayPercentage: '55.0', rankPosition: 4 },
    { roleId: 'data-analyst-jr', scoreNumerator: '45', scoreDenominator: '1', displayPercentage: '45.0', rankPosition: 5 },
    { roleId: 'cloud-jr', scoreNumerator: '30', scoreDenominator: '1', displayPercentage: '30.0', rankPosition: 6 },
  ],
}

function renderPage(path = '/roles?attemptId=attempt-77') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const rendered = render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[path]}><RolesSuggestedPage /></MemoryRouter>
    </QueryClientProvider>,
  )
  return { ...rendered, queryClient }
}

describe('RolesSuggestedPage V2', () => {
  afterEach(() => cleanup())

  it('features rank one, shows ranks two and three as alternatives, and keeps ranks four to six expandable', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Backend', level: 2 })).toBeInTheDocument()
    expect(screen.queryByText('Mayor coincidencia')).not.toBeInTheDocument()
    expect(screen.getByText('95.0%')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Frontend', level: 3 })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Full Stack', level: 3 })).toBeInTheDocument()
    const secondaryGrid = screen.getByRole('heading', { name: 'Frontend', level: 3 }).parentElement?.parentElement
    expect(secondaryGrid).toHaveClass('grid-cols-1', 'min-[360px]:grid-cols-2')
    expect(screen.getByText('Ver otros roles').closest('details')).not.toHaveAttribute('open')
    fireEvent.click(screen.getByText('Ver otros roles'))
    expect(screen.getByRole('heading', { name: 'QA', level: 3 })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Data Analyst', level: 3 })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Cloud', level: 3 })).toBeInTheDocument()
    expect(screen.getAllByRole('link').filter((link) => link.getAttribute('href')?.startsWith('/roles/'))).toHaveLength(6)
    expect(screen.getByText('Puesto 1')).toBeInTheDocument()
    expect(screen.getByText(/no son una certificación/)).toBeInTheDocument()
  })

  it('chooses the highlighted role from the persisted rank instead of a fixed role ID', async () => {
    const alternateTop: AssessmentAttempt = {
      ...completedAttempt,
      results: [
        { ...completedAttempt.results![3], scoreNumerator: '96', displayPercentage: '96.0', rankPosition: 1 },
        { ...completedAttempt.results![0], rankPosition: 2 },
        { ...completedAttempt.results![1], rankPosition: 3 },
        { ...completedAttempt.results![2], rankPosition: 4 },
        { ...completedAttempt.results![4], rankPosition: 5 },
        { ...completedAttempt.results![5], rankPosition: 6 },
      ],
    }
    vi.mocked(getAssessmentAttempt).mockResolvedValue(alternateTop)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'QA', level: 2 })).toBeInTheDocument()
    expect(screen.getByText('96.0%')).toBeInTheDocument()
  })

  it('links role detail to the same persisted assessment attempt', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    renderPage()

    await screen.findByRole('heading', { name: 'QA' })
    const qaLink = screen.getAllByRole('link').find((link) => link.getAttribute('href') === '/roles/qa-jr?attemptId=attempt-77')
    expect(qaLink).toBeInTheDocument()
  })

  it('shows the persisted latest rank on direct Roles entry, even while another draft exists', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([{
      attemptId: 'latest-1',
      completedAt: '2026-11-25T10:00:00Z',
      questionnaireVersion: 'Q1',
      matrixVersion: 'M1',
      results: completedAttempt.results!,
    } as AssessmentHistoryItem])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue({ ...completedAttempt, attemptId: 'draft-1', status: 'DRAFT', results: undefined })
    const { queryClient } = renderPage('/roles')

    expect(await screen.findByRole('heading', { name: 'Resultados de tu última evaluación' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Backend', level: 2 })).toBeInTheDocument()
    expect(screen.getByText('Estos resultados corresponden a tu última evaluación completada.')).toBeInTheDocument()
    expect(screen.getByText('95.0%')).toBeInTheDocument()
    expect(screen.queryAllByRole('link', { name: /Explorar rol/ })).toHaveLength(0)
    expect(getAssessmentAttempt).not.toHaveBeenCalled()

    act(() => queryClient.setQueryData(['assessment-attempt'], null))
    await waitFor(() => expect(screen.queryByText('Estos resultados corresponden a tu última evaluación completada.')).not.toBeInTheDocument())
    expect(screen.getAllByRole('link', { name: /Explorar rol/ })).toHaveLength(6)
  })

  it('shows the approved empty state when no completed evaluation exists', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    renderPage('/roles')

    expect(await screen.findByRole('heading', { name: 'Descubrí tu próximo rol' })).toBeInTheDocument()
    expect(screen.getByText('Completá tu autoevaluación para conocer qué perfiles profesionales se adaptan mejor a tus habilidades.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ir a Habilidades' })).toHaveAttribute('href', '/habilidades')
  })

  it('offers continuation rather than an empty-new-user message when a draft exists', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue({ attemptId: 'draft-1', status: 'DRAFT' } as AssessmentAttempt)
    renderPage('/roles')

    expect(await screen.findByRole('heading', { name: 'Tu próximo rol está a un paso' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Continuar evaluación' })).toHaveAttribute('href', '/habilidades/evaluacion')
    expect(screen.queryByText(/Puesto 1/)).not.toBeInTheDocument()
  })

  it('does not show empty state while direct-entry history is loading or failed', async () => {
    vi.mocked(getAssessmentHistory).mockReturnValue(new Promise(() => {}))
    vi.mocked(getCurrentAssessmentAttempt).mockReturnValue(new Promise(() => {}))
    const { unmount } = renderPage('/roles')
    expect(await screen.findByRole('status')).toHaveTextContent('Cargando tus resultados…')
    expect(screen.queryByRole('heading', { name: 'Descubrí tu próximo rol' })).not.toBeInTheDocument()
    unmount()

    vi.mocked(getAssessmentHistory).mockRejectedValue(new Error('offline'))
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    renderPage('/roles')
    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos consultar tus evaluaciones.')
    expect(screen.queryByRole('heading', { name: 'Descubrí tu próximo rol' })).not.toBeInTheDocument()
  })

  it('does not replace an explicitly requested historical attempt with the latest one', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    vi.mocked(getAssessmentHistory).mockResolvedValue([{ attemptId: 'latest-1' } as AssessmentHistoryItem])
    renderPage('/roles?attemptId=attempt-77')

    expect(await screen.findByRole('heading', { name: 'Backend', level: 2 })).toBeInTheDocument()
    expect(getAssessmentAttempt).toHaveBeenCalledWith('attempt-77')
    expect(getAssessmentHistory).not.toHaveBeenCalled()
  })
})
