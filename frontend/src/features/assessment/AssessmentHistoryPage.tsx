import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getAssessmentHistory } from '@/services/api'
import { ASSESSMENT_ROLE_NAME } from '@/types/domain'

const dateFormat = new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' })

export function AssessmentHistoryPage() {
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })

  if (history.isLoading) return <AppShell title="Historial" showBack><p role="status">Cargando evaluaciones…</p></AppShell>
  if (history.isError) return <AppShell title="Historial" showBack><p role="alert">No pudimos cargar tu historial.</p></AppShell>

  return (
    <AppShell title="Historial de evaluaciones" showBack>
      <div className="space-y-4">
        <p className="text-sm text-neutral-600">Tus evaluaciones completas y sus resultados guardados.</p>
        {history.data?.length ? history.data.map((item) => {
          const first = item.results.find((result) => result.rankPosition === 1)
          return (
            <Card key={item.attemptId} className="space-y-3">
              <div>
                <h2 className="font-bold text-neutral-950">Evaluación del {dateFormat.format(new Date(item.completedAt))}</h2>
                <p className="mt-1 text-xs text-neutral-500">Cuestionario {item.questionnaireVersion} · Matriz {item.matrixVersion}</p>
              </div>
              {first && <p className="text-sm text-neutral-700">Mayor preparación: <strong>{ASSESSMENT_ROLE_NAME[first.roleId] ?? first.roleId} ({first.displayPercentage}%)</strong></p>}
              <Link to={`/habilidades/historial/${encodeURIComponent(item.attemptId)}`}>
                <Button fullWidth variant="secondary">Ver detalle</Button>
              </Link>
            </Card>
          )
        }) : <Card><p className="text-sm text-neutral-600">Todavía no hay evaluaciones completas.</p></Card>}
      </div>
    </AppShell>
  )
}
