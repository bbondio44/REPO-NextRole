import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AppRouter } from '@/app/router'

vi.mock('@/app/RequireAuth', () => ({
  RequireAuth: ({ children }: { children: React.ReactNode }) => children,
  RequireProfile: ({ children }: { children: React.ReactNode }) => children,
}))
vi.mock('@/features/explainability/RoleFormationPage', () => ({ RoleFormationPage: () => <div>Role Formation V2</div> }))
vi.mock('@/features/learning/LearningIndexPage', () => ({ LearningIndexPage: () => <div>General Formation V1</div> }))
vi.mock('@/features/learning/LearningPage', () => ({ LearningPage: () => <div>Learning V1</div> }))
vi.mock('@/features/roles/RoleDetailPage', () => ({ RoleDetailPage: () => <div>Role detail V1</div> }))

describe('application route preservation', () => {
  afterEach(() => cleanup())

  it('routes the role-specific V2 path to Formation while retaining direct Formation and V1 consumers', () => {
    const routeCases = [
      ['/formacion/roles/qa-jr?attemptId=attempt-1', 'Role Formation V2'],
      ['/formacion', 'General Formation V1'],
      ['/roles/qa-jr', 'Role detail V1'],
      ['/roles/qa-jr/formacion', 'Learning V1'],
    ] as const

    for (const [path, content] of routeCases) {
      const view = render(<MemoryRouter initialEntries={[path]}><AppRouter /></MemoryRouter>)
      expect(screen.getByText(content)).toBeInTheDocument()
      view.unmount()
    }
  })
})
