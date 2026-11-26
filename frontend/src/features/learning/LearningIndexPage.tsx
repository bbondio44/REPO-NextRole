import { useQuery } from '@tanstack/react-query'
import { AppShell } from '@/components/layout/AppShell'
import { EmptyStateCard } from '@/components/ui/EmptyStateCard'
import { getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'

export function LearningIndexPage() {
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })
  const current = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt })

  if (history.isLoading || current.isLoading) return <AppShell title="Formación"><p role="status">Consultando tus evaluaciones…</p></AppShell>
  if (history.isError || current.isError) return <AppShell title="Formación"><p role="alert">No pudimos consultar tus evaluaciones.</p></AppShell>

  const hasDraft = current.data?.status === 'DRAFT'
  if (hasDraft || !history.data?.length) {
    return (
      <AppShell title="Formación">
        <EmptyStateCard
          icon={<svg viewBox="0 0 24 24" className="h-7 w-7" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true"><path d="M4 5.5 12 3l8 2.5V19l-8 2-8-2V5.5Z" strokeLinejoin="round"/><path d="M8 9h8M8 13h5M12 3v18" strokeLinecap="round"/></svg>}
          title="Prepará tu próximo paso"
          description="Completá tu autoevaluación para descubrir qué habilidades podés seguir desarrollando."
          actionLabel={hasDraft ? 'Continuar evaluación' : 'Ir a Habilidades'}
          to={hasDraft ? '/habilidades/evaluacion' : '/habilidades'}
        />
      </AppShell>
    )
  }
  return (
    <AppShell title="Formación">
      <EmptyStateCard
        icon={<svg viewBox="0 0 24 24" className="h-7 w-7" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true"><path d="M4 5.5 12 3l8 2.5V19l-8 2-8-2V5.5Z" strokeLinejoin="round"/><path d="M8 9h8M8 13h5M12 3v18" strokeLinecap="round"/></svg>}
        title="Prepará tu próximo paso"
        description="Explorá los roles disponibles y conocé las habilidades que podés seguir desarrollando."
        actionLabel="Explorar roles"
        to="/roles"
      />
    </AppShell>
  )
}
