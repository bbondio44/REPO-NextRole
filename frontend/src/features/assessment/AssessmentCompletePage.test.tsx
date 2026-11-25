import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentCompletePage } from '@/features/assessment/AssessmentCompletePage'
import { getAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

vi.mock('@/services/api', () => ({ getAssessmentAttempt: vi.fn() }))

const completed = { attemptId: 'attempt-1', status: 'COMPLETED' } as AssessmentAttempt

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/habilidades/completada?attemptId=attempt-1']}><AssessmentCompletePage /></MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('AssessmentCompletePage', () => {
  afterEach(() => cleanup())

  it('shows the confirmation and results action only after the backend reports completion', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue(completed)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Evaluación completada' })).toBeInTheDocument()
    expect(screen.getByText('Tus respuestas se guardaron correctamente. Ya podés consultar tus resultados.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver mis resultados' })).toHaveAttribute('href', '/roles?attemptId=attempt-1')
  })

  it('does not show the success message when the backend does not confirm completion', async () => {
    vi.mocked(getAssessmentAttempt).mockResolvedValue({ ...completed, status: 'DRAFT' })
    renderPage()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos confirmar la finalización')
    expect(screen.queryByRole('heading', { name: 'Evaluación completada' })).not.toBeInTheDocument()
  })
})
