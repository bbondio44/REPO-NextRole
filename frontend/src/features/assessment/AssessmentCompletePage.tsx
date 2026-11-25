import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getAssessmentAttempt } from '@/services/api'

export function AssessmentCompletePage() {
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const { data: attempt, isLoading, isError } = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })

  if (isLoading) return <AppShell title="Autoevaluación"><p role="status">Confirmando tu evaluación…</p></AppShell>
  if (isError || !attemptId || attempt?.status !== 'COMPLETED') {
    return <AppShell title="Autoevaluación"><p role="alert">No pudimos confirmar la finalización de esta evaluación.</p></AppShell>
  }

  return (
    <AppShell title="Autoevaluación completada">
      <Card className="space-y-3 text-center">
        <h1 className="text-xl font-extrabold text-neutral-950">Evaluación completada</h1>
        <p className="text-sm text-neutral-600">Tus respuestas se guardaron correctamente. Ya podés consultar tus resultados.</p>
        <Link to={`/roles?attemptId=${encodeURIComponent(attemptId)}`} className="block pt-2">
          <Button fullWidth>Ver mis resultados</Button>
        </Link>
        <Link to="/habilidades/historial" className="block text-sm font-semibold text-primary-700">Ver historial de evaluaciones</Link>
        <Link to="/habilidades" className="block text-sm font-semibold text-primary-700">Volver a Habilidades</Link>
      </Card>
    </AppShell>
  )
}
