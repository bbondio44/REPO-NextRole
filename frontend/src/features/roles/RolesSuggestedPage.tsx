import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { EmptyStateCard } from '@/components/ui/EmptyStateCard'
import { AssessmentResultsRanking } from '@/features/roles/AssessmentResultsRanking'
import { getAssessmentAttempt, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'

export function RolesSuggestedPage() {
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const { data: attempt, isLoading, isError } = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })
  const historyQuery = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory, enabled: !attemptId })
  const currentQuery = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt, enabled: !attemptId })

  if (!attemptId) {
    if (historyQuery.isLoading || currentQuery.isLoading) {
      return <AppShell title="Roles" subtitle="Roles IT sugeridos"><p role="status">Cargando tus resultados…</p></AppShell>
    }
    if (historyQuery.isError || currentQuery.isError) {
      return <AppShell title="Roles" subtitle="Roles IT sugeridos"><p role="alert">No pudimos consultar tus evaluaciones.</p></AppShell>
    }
    const latest = historyQuery.data?.[0]
    if (!latest) {
      const hasDraft = currentQuery.data?.status === 'DRAFT'
      return (
        <AppShell title="Roles" subtitle="Roles IT sugeridos">
          <EmptyStateCard
            icon={<svg viewBox="0 0 24 24" className="h-7 w-7" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true"><path d="M4 19V5m0 14h16M8 15v-4m4 4V7m4 8V9" strokeLinecap="round" strokeLinejoin="round" /></svg>}
            title={hasDraft ? 'Tu próximo rol está a un paso' : 'Descubrí tu próximo rol'}
            description={hasDraft ? 'Continuá tu autoevaluación para conocer qué perfiles profesionales se adaptan mejor a tus habilidades.' : 'Completá tu autoevaluación para conocer qué perfiles profesionales se adaptan mejor a tus habilidades.'}
            actionLabel={hasDraft ? 'Continuar evaluación' : 'Ir a Habilidades'}
            to={hasDraft ? '/habilidades/evaluacion' : '/habilidades'}
          />
        </AppShell>
      )
    }
    const hasDraft = currentQuery.data?.status === 'DRAFT'
    return (
      <AppShell title="Roles" subtitle="Roles IT sugeridos">
        <h1 className="text-lg font-extrabold text-neutral-950">Resultados de tu última evaluación</h1>
        <p className="mt-1 text-sm text-neutral-600">Porcentajes persistidos de preparación técnica. No son una certificación.</p>
        {hasDraft && <p className="mt-2 text-sm text-neutral-600">Estos resultados corresponden a tu última evaluación completada.</p>}
        <div className="mt-4"><AssessmentResultsRanking attemptId={latest.attemptId} results={latest.results} showExploreActions={!hasDraft} /></div>
      </AppShell>
    )
  }
  if (isLoading) return <AppShell title="Roles" subtitle="Roles IT sugeridos" showBack><p role="status">Cargando tus resultados…</p></AppShell>
  if (isError || !attempt || attempt.status !== 'COMPLETED' || !attempt.results) {
    return <AppShell title="Roles" subtitle="Roles IT sugeridos" showBack><p role="alert">No pudimos cargar los resultados de esta evaluación.</p></AppShell>
  }

  return (
    <AppShell title="Roles" subtitle="Preparación técnica declarada" showBack>
      <h1 className="text-lg font-extrabold text-neutral-950">Resultados para los seis roles IT</h1>
      <p className="mt-1 text-sm text-neutral-600">
        Porcentajes calculados a partir de tu autoevaluación. Orientan tu preparación técnica y no son una certificación.
      </p>
      <p className="mt-2 text-xs text-neutral-400">Evaluación del {new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium' }).format(new Date(attempt.completedAt ?? attempt.createdAt))} · Cuestionario {attempt.questionnaireVersion} · Matriz {attempt.matrixVersion}</p>

      <div className="mt-4">
        <AssessmentResultsRanking attemptId={attempt.attemptId} results={attempt.results} />
      </div>

    </AppShell>
  )
}
