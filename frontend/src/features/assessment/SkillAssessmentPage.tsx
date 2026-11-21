import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { LevelPicker } from '@/components/ui/LevelPicker'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { getOrStartAssessment, saveAssessmentAnswer } from '@/services/api'
import type { AssessmentAttempt, KnowledgeLevel } from '@/types/domain'

const ATTEMPT_QUERY_KEY = ['assessment-attempt']

export function SkillAssessmentPage() {
  const queryClient = useQueryClient()
  const [savingCompetency, setSavingCompetency] = useState<string | null>(null)
  const [saveError, setSaveError] = useState<string | null>(null)
  const { data: attempt, isLoading, isError, error } = useQuery({
    queryKey: ATTEMPT_QUERY_KEY,
    queryFn: getOrStartAssessment,
  })

  async function handleSelect(competencyCode: string, level: KnowledgeLevel) {
    if (!attempt || attempt.status !== 'DRAFT') return
    setSavingCompetency(competencyCode)
    setSaveError(null)
    try {
      const saved = await saveAssessmentAnswer(attempt.attemptId, competencyCode, level)
      queryClient.setQueryData<AssessmentAttempt>(ATTEMPT_QUERY_KEY, (current) => {
        if (!current) return current
        return {
          ...current,
          answers: { ...current.answers, [saved.competencyCode]: saved.level },
          answeredCount: saved.answeredCount,
        }
      })
    } catch {
      setSaveError('No pudimos guardar tu respuesta. Revisá tu conexión e intentá de nuevo.')
    } finally {
      setSavingCompetency(null)
    }
  }

  if (isLoading) {
    return <AppShell title="Autoevaluación técnica" showBack><p role="status">Cargando tu evaluación…</p></AppShell>
  }

  if (isError || !attempt) {
    return (
      <AppShell title="Autoevaluación técnica" showBack>
        <p role="alert">{error instanceof Error ? error.message : 'No pudimos cargar tu evaluación.'}</p>
      </AppShell>
    )
  }

  const progress = attempt.totalQuestions > 0 ? (attempt.answeredCount / attempt.totalQuestions) * 100 : 0

  return (
    <AppShell title="Autoevaluación técnica" subtitle={`Cuestionario ${attempt.questionnaireVersion}`} showBack>
      <div className="flex items-center justify-between text-xs font-semibold text-primary-700">
        <span>{attempt.answeredCount} de {attempt.totalQuestions} respondidas</span>
        <span>{Math.round(progress)}%</span>
      </div>
      <div className="mt-2"><ProgressBar value={progress} /></div>
      <p className="mt-3 text-sm text-neutral-600">
        Declarás tu nivel en cada competencia. El nivel 0 es una respuesta válida; podés cambiar tus respuestas
        mientras esta evaluación esté en curso.
      </p>

      {saveError && <p className="mt-3 text-sm text-red-600" role="alert">{saveError}</p>}

      <div className="mt-4 space-y-3">
        {attempt.questions.map((question) => (
          <Card key={question.competencyCode}>
            <p className="text-xs font-semibold text-primary-700">Competencia {question.position} de {attempt.totalQuestions}</p>
            <h2 className="mt-1 text-sm font-bold text-neutral-950">{question.title}</h2>
            <p className="mt-2 text-sm leading-relaxed text-neutral-700">{question.question}</p>
            <div className="mt-3">
              <LevelPicker
                value={attempt.answers[question.competencyCode] ?? null}
                options={question.options}
                onChange={(level) => handleSelect(question.competencyCode, level)}
              />
            </div>
            {savingCompetency === question.competencyCode && <p className="mt-2 text-xs text-neutral-500" role="status">Guardando…</p>}
          </Card>
        ))}
      </div>

      <div className="mt-6">
        <Button fullWidth disabled={attempt.status !== 'COMPLETED'}>
          Ver resultados cuando finalices las 31 preguntas
        </Button>
      </div>
    </AppShell>
  )
}
