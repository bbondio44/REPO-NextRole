import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { AssessmentResultsRanking } from '@/features/roles/AssessmentResultsRanking'
import { getAssessmentAttempt, getSelectedRoleId } from '@/services/api'

export function RolesSuggestedPage() {
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const { data: attempt, isLoading, isError } = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })
  const { data: selectedRoleId } = useQuery({ queryKey: ['selected-role'], queryFn: getSelectedRoleId, enabled: !attemptId })

  if (!attemptId) {
    return <AppShell title="Roles" subtitle="Roles IT sugeridos" showBack><p role="alert">Abrí los resultados desde una evaluación finalizada.</p></AppShell>
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
      <p className="mt-2 text-xs text-neutral-400">
        Cuestionario {attempt.questionnaireVersion} · Matriz {attempt.matrixVersion}
      </p>

      <div className="mt-4">
        <AssessmentResultsRanking attemptId={attempt.attemptId} results={attempt.results} />
      </div>

      {selectedRoleId && !attemptId && (
        <Link to={`/roles/${selectedRoleId}/brechas`} className="mt-6 block">
          <Button fullWidth>Continuar con mi rol elegido →</Button>
        </Link>
      )}
    </AppShell>
  )
}
