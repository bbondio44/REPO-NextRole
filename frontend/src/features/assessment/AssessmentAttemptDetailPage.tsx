import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Card } from '@/components/ui/Card'
import { AssessmentResultsRanking } from '@/features/roles/AssessmentResultsRanking'
import { getAssessmentAttempt } from '@/services/api'

const LEVEL_NAME = ['Sin conocimientos', 'Inicial', 'Básico', 'Práctico', 'Avanzado']

export function AssessmentAttemptDetailPage() {
  const { attemptId } = useParams()
  const query = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })

  if (query.isLoading) return <AppShell title="Evaluación" showBack><p role="status">Cargando evaluación…</p></AppShell>
  if (query.isError || !query.data || query.data.status !== 'COMPLETED' || !query.data.results?.length) {
    return <AppShell title="Evaluación" showBack><p role="alert">No pudimos cargar esta evaluación completa.</p></AppShell>
  }

  const attempt = query.data
  const results = attempt.results ?? []
  const questions = [...attempt.questions].sort((a, b) => a.position - b.position)

  return (
    <AppShell title="Detalle de evaluación" showBack>
      <div className="space-y-4">
        <Card className="space-y-2">
          <p className="font-bold text-neutral-950">Evaluación realizada el {new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(attempt.completedAt ?? attempt.createdAt))}</p>
          <p className="text-xs text-neutral-500">Cuestionario {attempt.questionnaireVersion} · Matriz {attempt.matrixVersion}</p>
        </Card>

        <section aria-labelledby="history-results-heading" className="space-y-2">
          <h2 id="history-results-heading" className="font-bold text-neutral-950">Resultados de esta evaluación</h2>
          <AssessmentResultsRanking attemptId={attempt.attemptId} results={results} />
        </section>

        <details className="rounded-2xl border border-neutral-100 bg-white p-4 shadow-sm">
          <summary className="cursor-pointer font-bold text-primary-700">Ver respuestas de esta evaluación</summary>
          <div className="mt-3 space-y-3">
          {questions.map((question) => {
            const level = attempt.answers[question.competencyCode]
            const answer = level === undefined ? undefined : question.options.find((option) => option.level === level)
            return (
              <Card key={question.competencyCode} className="py-3">
                <h3 className="text-sm font-semibold text-neutral-900">{question.position}. {question.title}</h3>
                <p className="mt-1 text-sm text-neutral-600">{question.question}</p>
                <p className="mt-2 text-sm font-semibold text-neutral-800">
                  {level === undefined ? 'Sin respuesta' : `${LEVEL_NAME[level]} (nivel ${level})`}
                </p>
                {answer && <p className="mt-1 text-sm text-neutral-600">{answer.description}</p>}
              </Card>
            )
          })}
          </div>
        </details>

        <Link className="block text-center text-sm font-semibold text-primary-700" to="/habilidades/historial">Volver al historial</Link>
      </div>
    </AppShell>
  )
}
