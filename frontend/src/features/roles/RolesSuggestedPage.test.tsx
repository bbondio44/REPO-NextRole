import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { RolesSuggestedPage } from '@/features/roles/RolesSuggestedPage'
import { getAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

vi.mock('@/services/api', () => ({
  getAssessmentAttempt: vi.fn(),
  getSelectedRoleId: vi.fn().mockResolvedValue(null),
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

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/roles?attemptId=attempt-77']}><RolesSuggestedPage /></MemoryRouter>
    </QueryClientProvider>,
  )
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
})
