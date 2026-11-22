import { useQuery } from '@tanstack/react-query'
import { Link, useSearchParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getAssessmentAttempt } from '@/services/api'
import { KNOWLEDGE_LEVEL_LABEL } from '@/types/domain'

export function SkillSummaryPage() {
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const { data: attempt, isLoading, isError } = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })

  if (!attemptId) {
    return (
      <AppShell title="Habilidades" subtitle="Resumen de habilidades" showBack>
        <p role="alert">Abrí el resumen desde una evaluación finalizada.</p>
      </AppShell>
    )
  }
  if (isLoading) return <AppShell title="Habilidades" subtitle="Resumen de habilidades" showBack><p role="status">Cargando tu evaluación…</p></AppShell>
  if (isError || !attempt || attempt.status !== 'COMPLETED') {
    return <AppShell title="Habilidades" subtitle="Resumen de habilidades" showBack><p role="alert">No pudimos cargar una evaluación finalizada.</p></AppShell>
  }

  return (
    <AppShell title="Habilidades" subtitle="Resumen de competencias" showBack>
      <Card>
        <h1 className="text-lg font-extrabold text-neutral-950">Tus niveles declarados</h1>
        <p className="mt-2 text-sm text-neutral-600">
          Esta autoevaluación refleja los niveles que declaraste. No es una certificación y se muestra separada del matching por rol.
        </p>
        <p className="mt-2 text-xs text-neutral-400">
          Cuestionario {attempt.questionnaireVersion} · Matriz {attempt.matrixVersion}
        </p>
      </Card>

      <div className="mt-4 space-y-3">
        {attempt.questions.map((question) => {
          const level = attempt.answers[question.competencyCode]
          const option = question.options.find((item) => item.level === level)
          return (
            <Card key={question.competencyCode}>
              <p className="text-xs font-semibold text-primary-700">Competencia {question.position}</p>
              <h2 className="mt-1 text-sm font-bold text-neutral-950">{question.title}</h2>
              <p className="mt-2 text-sm font-semibold text-neutral-800">
                {level === undefined ? 'Sin respuesta' : `Nivel ${level}: ${KNOWLEDGE_LEVEL_LABEL[level]}`}
              </p>
              {option && <p className="mt-1 text-sm text-neutral-600">{option.description}</p>}
            </Card>
          )
        })}
      </div>

      <Link to={`/roles?attemptId=${encodeURIComponent(attempt.attemptId)}`} className="mt-6 block">
        <Button fullWidth>Ver matching para los seis roles →</Button>
      </Link>
    </AppShell>
  )
}
