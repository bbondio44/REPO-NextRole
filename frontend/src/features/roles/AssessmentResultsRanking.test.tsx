import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it } from 'vitest'

import { AssessmentResultsRanking } from '@/features/roles/AssessmentResultsRanking'
import type { AssessmentRoleResult } from '@/types/domain'

const results: AssessmentRoleResult[] = [
  { roleId: 'qa-jr', scoreNumerator: '81', scoreDenominator: '1', displayPercentage: '81', rankPosition: 1 },
  { roleId: 'data-analyst-jr', scoreNumerator: '75', scoreDenominator: '1', displayPercentage: '75', rankPosition: 2 },
]

describe('AssessmentResultsRanking explainability navigation', () => {
  afterEach(() => cleanup())

  it.each(['current-attempt', 'historical-attempt'])('opens Formation directly with the %s identifier', (attemptId) => {
    render(<MemoryRouter><AssessmentResultsRanking attemptId={attemptId} results={results} /></MemoryRouter>)

    expect(screen.getAllByRole('link', { name: /Explorar rol/ })[0]).toHaveAttribute(
      'href',
      `/formacion/roles/qa-jr?attemptId=${attemptId}`,
    )
    expect(screen.getAllByRole('link', { name: /Explorar rol/ })[0]).toHaveTextContent('Explorar rol')
  })
})
