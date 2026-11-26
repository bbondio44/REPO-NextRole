import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { LearningIndexPage } from '@/features/learning/LearningIndexPage'
import { getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt, AssessmentHistoryItem } from '@/types/domain'

vi.mock('@/services/api', () => ({
  getAssessmentHistory: vi.fn(),
  getCurrentAssessmentAttempt: vi.fn(),
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const rendered = render(<QueryClientProvider client={client}><MemoryRouter><LearningIndexPage /></MemoryRouter></QueryClientProvider>)
  return { ...rendered, queryClient: client }
}

describe('LearningIndexPage V1 entry states', () => {
  afterEach(() => cleanup())

  it('guides a new user to Habilidades without asking for a target role', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Prepará tu próximo paso' })).toBeInTheDocument()
    expect(screen.getByText('Completá tu autoevaluación para descubrir qué habilidades podés seguir desarrollando.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ir a Habilidades' })).toHaveAttribute('href', '/habilidades')
    expect(screen.queryByText(/rol objetivo/i)).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Ver roles sugeridos/ })).not.toBeInTheDocument()
  })

  it('shows the informative empty state and no role exploration while a draft is active', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue({ status: 'DRAFT' } as AssessmentAttempt)
    renderPage()

    expect(await screen.findByText('Completá tu autoevaluación para descubrir qué habilidades podés seguir desarrollando.')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'Continuar evaluación' })).toHaveAttribute('href', '/habilidades/evaluacion')
    expect(screen.queryByRole('link', { name: 'Explorar roles' })).not.toBeInTheDocument()
  })

  it('does not reuse completed-evaluation context while a new draft is active, and restores exploration after discard', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([{ attemptId: 'previous' } as AssessmentHistoryItem])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue({ status: 'DRAFT' } as AssessmentAttempt)
    const { queryClient } = renderPage()

    expect(await screen.findByText('Completá tu autoevaluación para descubrir qué habilidades podés seguir desarrollando.')).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Explorar roles' })).not.toBeInTheDocument()

    act(() => queryClient.setQueryData(['assessment-attempt'], null))
    await screen.findByRole('link', { name: 'Explorar roles' })
    expect(screen.getByText('Explorá los roles disponibles y conocé las habilidades que podés seguir desarrollando.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Explorar roles' })).toHaveAttribute('href', '/roles')
  })

  it('invites a user with completed history to explore roles without requiring a permanent objective', async () => {
    vi.mocked(getAssessmentHistory).mockResolvedValue([{ attemptId: 'done' } as AssessmentHistoryItem])
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    renderPage()

    expect(await screen.findByText('Explorá los roles disponibles y conocé las habilidades que podés seguir desarrollando.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Explorar roles' })).toHaveAttribute('href', '/roles')
    expect(screen.queryByText(/rol objetivo/i)).not.toBeInTheDocument()
  })

  it('does not present an empty state as if evaluation history had loaded when a request fails', async () => {
    vi.mocked(getAssessmentHistory).mockRejectedValue(new Error('offline'))
    vi.mocked(getCurrentAssessmentAttempt).mockResolvedValue(null)
    renderPage()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos consultar tus evaluaciones.')
    expect(screen.queryByRole('heading', { name: 'Prepará tu próximo paso' })).not.toBeInTheDocument()
  })

  it('shows a loading state while assessment data is pending', async () => {
    vi.mocked(getAssessmentHistory).mockReturnValue(new Promise(() => {}))
    vi.mocked(getCurrentAssessmentAttempt).mockReturnValue(new Promise(() => {}))
    renderPage()

    expect(await screen.findByRole('status')).toHaveTextContent('Consultando tus evaluaciones…')
    expect(screen.queryByRole('heading', { name: 'Prepará tu próximo paso' })).not.toBeInTheDocument()
  })
})
