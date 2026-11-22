import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getAssessmentAttempt, getSelectedRoleId } from '@/services/api'
import { ASSESSMENT_ROLE_NAME } from '@/types/domain'

function scoreTone(score: number): 'success' | 'primary' | 'warning' {
  if (score >= 80) return 'success'
  if (score >= 50) return 'primary'
  return 'warning'
}

export function RolesSuggestedPage() {
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const { data: attempt, isLoading, isError } = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })
  const { data: selectedRoleId } = useQuery({ queryKey: ['selected-role'], queryFn: getSelectedRoleId })

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

      <div className="mt-4 space-y-3">
        {attempt.results.map((result) => {
          const numericScore = Number(result.displayPercentage)
          return (
            <Card key={result.roleId}>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="text-xs font-semibold text-primary-700">Puesto {result.rankPosition}</p>
                  <h2 className="mt-1 text-sm font-bold text-neutral-950">
                    {ASSESSMENT_ROLE_NAME[result.roleId] ?? result.roleId}
                  </h2>
                </div>
                <Badge tone={scoreTone(numericScore)}>{result.displayPercentage}%</Badge>
              </div>
              <div className="mt-3 flex items-center justify-between">
                <span className="text-xs text-neutral-500">Porcentaje de preparación técnica</span>
                <Link to={`/roles/${encodeURIComponent(result.roleId)}?attemptId=${encodeURIComponent(attempt.attemptId)}`}>
                  <Button variant="secondary">Explorar rol →</Button>
                </Link>
              </div>
            </Card>
          )
        })}
      </div>

      {selectedRoleId && (
        <Link to={`/roles/${selectedRoleId}/brechas`} className="mt-6 block">
          <Button fullWidth>Continuar con mi rol elegido →</Button>
        </Link>
      )}
    </AppShell>
  )
}
