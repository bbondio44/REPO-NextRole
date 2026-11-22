import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
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
    { roleId: 'qa-jr', scoreNumerator: '55', scoreDenominator: '1', displayPercentage: '55.0', rankPosition: 1 },
    { roleId: 'backend-dev-jr', scoreNumerator: '95', scoreDenominator: '1', displayPercentage: '95.0', rankPosition: 2 },
    { roleId: 'frontend-dev-jr', scoreNumerator: '70', scoreDenominator: '1', displayPercentage: '70.0', rankPosition: 3 },
    { roleId: 'fullstack-dev-jr', scoreNumerator: '60', scoreDenominator: '1', displayPercentage: '60.0', rankPosition: 4 },
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

  it('renders the six persisted results in server rank order without sorting display values', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    renderPage()

    const roleNames = await screen.findAllByRole('heading', { level: 2 })
    expect(roleNames.map((heading) => heading.textContent)).toEqual([
      'QA', 'Backend', 'Frontend', 'Full Stack', 'Data Analyst', 'Cloud',
    ])
    expect(screen.getByText('Puesto 1')).toBeInTheDocument()
    expect(screen.getByText('55.0%')).toBeInTheDocument()
    expect(screen.getByText(/no son una certificación/)).toBeInTheDocument()
  })

  it('links role detail to the same persisted assessment attempt', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completedAttempt)
    renderPage()

    await screen.findByRole('heading', { name: 'QA' })
    const qaLink = screen.getAllByRole('link').find((link) => link.getAttribute('href') === '/roles/qa-jr?attemptId=attempt-77')
    expect(qaLink).toBeInTheDocument()
  })
})
