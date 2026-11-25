import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getAssessmentAttempt } from '@/services/api'
import { ASSESSMENT_ROLE_NAME } from '@/types/domain'

const LEVEL_NAME = ['Sin conocimientos', 'Inicial', 'Básico', 'Práctico', 'Avanzado']

export function AssessmentAttemptDetailPage() {
  const { attemptId } = useParams()
  const query = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })

  if (query.isLoading) return <AppShell title="Evaluación" showBack><p role="status">Cargando evaluación…</p></AppShell>
  if (query.isError || !query.data || query.data.status !== 'COMPLETED') {
    return <AppShell title="Evaluación" showBack><p role="alert">No pudimos cargar esta evaluación completa.</p></AppShell>
  }

  const attempt = query.data
  const questions = [...attempt.questions].sort((a, b) => a.position - b.position)

  return (
    <AppShell title="Detalle de evaluación" showBack>
      <div className="space-y-4">
        <Card className="space-y-2">
          <p className="font-bold text-neutral-950">Completada el {new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(attempt.completedAt ?? attempt.createdAt))}</p>
          <p className="text-xs text-neutral-500">Cuestionario {attempt.questionnaireVersion} · Matriz {attempt.matrixVersion}</p>
          <p className="text-sm text-neutral-600">Respuestas declaradas: {attempt.answeredCount} de {attempt.totalQuestions}</p>
        </Card>

        <section aria-labelledby="history-results-heading" className="space-y-2">
          <h2 id="history-results-heading" className="font-bold text-neutral-950">Resultados guardados</h2>
          {attempt.results?.map((result) => (
            <Card key={result.roleId} className="flex items-center justify-between gap-3 py-3">
              <span className="text-sm font-semibold text-neutral-800">{result.rankPosition}. {ASSESSMENT_ROLE_NAME[result.roleId] ?? result.roleId}</span>
              <span className="font-bold text-primary-700">{result.displayPercentage}%</span>
            </Card>
          ))}
        </section>

        <section aria-labelledby="history-answers-heading" className="space-y-2">
          <h2 id="history-answers-heading" className="font-bold text-neutral-950">Respuestas de esta evaluación</h2>
          {questions.map((question) => {
            const level = attempt.answers[question.competencyCode]
            return (
              <Card key={question.competencyCode} className="py-3">
                <h3 className="text-sm font-semibold text-neutral-900">{question.position}. {question.title}</h3>
                <p className="mt-1 text-sm text-neutral-600">{level === undefined ? 'Sin respuesta' : `${LEVEL_NAME[level]} (nivel ${level})`}</p>
              </Card>
            )
          })}
        </section>

        <Link to="/habilidades/historial"><Button fullWidth variant="secondary">Volver al historial</Button></Link>
      </div>
    </AppShell>
  )
}
