import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { RoleDetailPage } from '@/features/roles/RoleDetailPage'
import { getAssessmentAttempt, getRoleById, getRoleMatching, getSkillGap, selectRole } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

vi.mock('@/services/api', () => ({
  getAssessmentAttempt: vi.fn(),
  getRoleById: vi.fn(),
  getRoleMatching: vi.fn(),
  getSkillGap: vi.fn(),
  selectRole: vi.fn(),
}))

const historicalAttempt: AssessmentAttempt = {
  attemptId: 'historical-2',
  status: 'COMPLETED',
  createdAt: '2026-11-20T10:00:00Z',
  completedAt: '2026-11-22T12:00:00Z',
  questionnaireVersion: 'Q2',
  matrixVersion: 'M2',
  answeredCount: 31,
  totalQuestions: 31,
  answers: {},
  questions: [],
  results: [{
    roleId: 'backend-dev-jr',
    scoreNumerator: '82',
    scoreDenominator: '1',
    displayPercentage: '82.0',
    rankPosition: 2,
  }],
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/roles/backend-dev-jr?attemptId=historical-2']}>
        <Routes><Route path="/roles/:roleId" element={<RoleDetailPage />} /></Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('RoleDetailPage historical assessment context', () => {
  afterEach(() => cleanup())

  it('keeps the selected attempt score and does not mix in current-profile gaps or matching', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(historicalAttempt)
    vi.mocked(getRoleById).mockResolvedValue(undefined)
    renderPage()

    expect(await screen.findByText(/82.0% preparación técnica/)).toBeInTheDocument()
    expect(screen.getByText(/Evaluación del 22 nov 2026/)).toBeInTheDocument()
    expect(screen.getByText(/Cuestionario Q2 · Matriz M2/)).toBeInTheDocument()
    expect(screen.queryByText('Requisitos y habilidades requeridas')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Ver diferencias con mi perfil/ })).not.toBeInTheDocument()
    expect(getSkillGap).not.toHaveBeenCalled()
    expect(getRoleMatching).not.toHaveBeenCalled()
    expect(selectRole).not.toHaveBeenCalled()
  })
})
