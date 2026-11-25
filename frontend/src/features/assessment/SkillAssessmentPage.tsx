import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { LevelPicker } from '@/components/ui/LevelPicker'
import { AssessmentProgress } from '@/features/assessment/AssessmentProgress'
import { countSavedAnswers } from '@/features/assessment/assessmentAreas'
import { completeAssessmentAttempt, createAssessmentReevaluation, discardAssessmentAttempt, getCurrentAssessmentAttempt, saveAssessmentAnswer } from '@/services/api'
import type { AssessmentAttempt, KnowledgeLevel } from '@/types/domain'

const ATTEMPT_QUERY_KEY = ['assessment-attempt']

export function SkillAssessmentPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [currentPosition, setCurrentPosition] = useState(1)
  const [savingCompetencies, setSavingCompetencies] = useState<Set<string>>(() => new Set())
  const [failedCompetencies, setFailedCompetencies] = useState<Set<string>>(() => new Set())
  const [pendingSelections, setPendingSelections] = useState<Record<string, KnowledgeLevel>>({})
  const [saveError, setSaveError] = useState<string | null>(null)
  const [completionError, setCompletionError] = useState<string | null>(null)
  const [isCompleting, setIsCompleting] = useState(false)
  const [confirmDiscard, setConfirmDiscard] = useState(false)
  const [isRestarting, setIsRestarting] = useState(false)
  const initializedAttemptId = useRef<string | null>(null)
  const { data: attempt, isLoading, isError, error } = useQuery({
    queryKey: ATTEMPT_QUERY_KEY,
    queryFn: getCurrentAssessmentAttempt,
  })
  const attemptQuestions = attempt?.questions
  const questions = useMemo(
    () => attemptQuestions ? [...attemptQuestions].sort((left, right) => left.position - right.position) : [],
    [attemptQuestions],
  )
  const savedAnswerCount = attempt ? countSavedAnswers(questions, attempt.answers) : 0

  useEffect(() => {
    if (!attempt || initializedAttemptId.current === attempt.attemptId || questions.length === 0) return
    initializedAttemptId.current = attempt.attemptId
    const firstPending = questions.find((question) => !Object.hasOwn(attempt.answers, question.competencyCode))
    setCurrentPosition((firstPending ?? questions[questions.length - 1]).position)
  }, [attempt, questions])

  async function handleSelect(competencyCode: string, level: KnowledgeLevel) {
    if (!attempt || attempt.status !== 'DRAFT' || savingCompetencies.has(competencyCode)) return
    setPendingSelections((current) => ({ ...current, [competencyCode]: level }))
    setFailedCompetencies((current) => {
      const next = new Set(current)
      next.delete(competencyCode)
      return next
    })
    setSavingCompetencies((current) => new Set(current).add(competencyCode))
    setSaveError(null)
    try {
      const saved = await saveAssessmentAnswer(attempt.attemptId, competencyCode, level)
      queryClient.setQueryData<AssessmentAttempt>(ATTEMPT_QUERY_KEY, (current) => {
        if (!current) return current
        const answers = { ...current.answers, [saved.competencyCode]: saved.level }
        return {
          ...current,
          answers,
          answeredCount: countSavedAnswers(current.questions, answers),
        }
      })
      setPendingSelections((current) => {
        const next = { ...current }
        delete next[competencyCode]
        return next
      })
    } catch {
      setSaveError('No pudimos guardar tu respuesta. Revisá tu conexión e intentá de nuevo.')
      setFailedCompetencies((current) => new Set(current).add(competencyCode))
    } finally {
      setSavingCompetencies((current) => {
        const next = new Set(current)
        next.delete(competencyCode)
        return next
      })
    }
  }

  async function handleComplete() {
    if (
      !attempt
      || attempt.status !== 'DRAFT'
      || questions.length !== attempt.totalQuestions
      || savedAnswerCount !== attempt.totalQuestions
      || savingCompetencies.size > 0
      || failedCompetencies.size > 0
    ) return

    setIsCompleting(true)
    setCompletionError(null)
    try {
      const completed = await completeAssessmentAttempt(attempt.attemptId)
      queryClient.setQueryData(['assessment-attempt', completed.attemptId], completed)
      queryClient.setQueryData(ATTEMPT_QUERY_KEY, null)
      await queryClient.invalidateQueries({ queryKey: ['assessment-history'] })
      navigate(`/habilidades/completada?attemptId=${encodeURIComponent(completed.attemptId)}`)
    } catch (cause) {
      setCompletionError(cause instanceof Error ? cause.message : 'No pudimos finalizar tu evaluación. Intentá de nuevo.')
    } finally {
      setIsCompleting(false)
    }
  }

  async function handleRestart() {
    if (!attempt || attempt.status !== 'DRAFT') return
    setIsRestarting(true)
    setCompletionError(null)
    try {
      await discardAssessmentAttempt(attempt.attemptId)
      queryClient.setQueryData(ATTEMPT_QUERY_KEY, null)
      const nextAttempt = await createAssessmentReevaluation()
      queryClient.setQueryData(ATTEMPT_QUERY_KEY, nextAttempt)
      initializedAttemptId.current = null
      await queryClient.invalidateQueries({ queryKey: ['assessment-history'] })
      setConfirmDiscard(false)
    } catch (cause) {
      setCompletionError(cause instanceof Error ? cause.message : 'No pudimos iniciar otra evaluación. Intentá de nuevo.')
    } finally {
      setIsRestarting(false)
    }
  }

  if (isLoading) {
    return <AppShell title="Autoevaluación técnica" showBack><p role="status">Cargando tu evaluación…</p></AppShell>
  }

  if (!isError && !attempt && !isLoading) {
    return <AppShell title="Autoevaluacion tecnica" showBack><p role="status">No hay una evaluacion en curso. Desde Habilidades podes comenzar o continuar una evaluacion.</p></AppShell>
  }

  if (isError || !attempt) {
    return (
      <AppShell title="Autoevaluación técnica" showBack>
        <p role="alert">{error instanceof Error ? error.message : 'No pudimos cargar tu evaluación.'}</p>
      </AppShell>
    )
  }

  const currentQuestion = questions.find((question) => question.position === currentPosition) ?? questions[0]

  if (!currentQuestion) {
    return <AppShell title="Autoevaluación técnica" showBack><p role="alert">No hay preguntas disponibles.</p></AppShell>
  }

  const currentQuestionIndex = questions.findIndex((question) => question.position === currentQuestion.position)
  const isLastQuestion = currentQuestionIndex === questions.length - 1
  const allAnswersSaved = savedAnswerCount === attempt.totalQuestions && questions.length === attempt.totalQuestions
  const currentQuestionHasSavedAnswer = Object.prototype.hasOwnProperty.call(attempt.answers, currentQuestion.competencyCode)
  const currentQuestionIsSaving = savingCompetencies.has(currentQuestion.competencyCode)
  const currentQuestionSaveFailed = failedCompetencies.has(currentQuestion.competencyCode)
  const canComplete = allAnswersSaved && savingCompetencies.size === 0 && failedCompetencies.size === 0
  const selectedLevel = pendingSelections[currentQuestion.competencyCode]
    ?? attempt.answers[currentQuestion.competencyCode]
    ?? null

  return (
    <AppShell title="Autoevaluación técnica" subtitle={`Cuestionario ${attempt.questionnaireVersion}`} showBack compactBottomNavSpacing>
      <div className="flex min-h-[calc(100dvh-8.5rem-env(safe-area-inset-bottom))] flex-col gap-5">
        <AssessmentProgress
          currentPosition={currentQuestion.position}
          questions={questions}
          answers={attempt.answers}
        />

        <section aria-labelledby="assessment-question-title" className="mt-2 space-y-3 py-1">
          <h2 id="assessment-question-title" className="text-[22px] font-bold leading-snug text-neutral-950 sm:text-2xl">
            {currentQuestion.title}
          </h2>
          <p className="text-base leading-relaxed text-neutral-700">
            {currentQuestion.question}
          </p>
          <div className="pt-1">
            <LevelPicker
              value={selectedLevel}
              options={currentQuestion.options}
              disabled={currentQuestionIsSaving}
              onChange={(level) => handleSelect(currentQuestion.competencyCode, level)}
            />
          </div>
          {currentQuestionIsSaving && (
            <p className="text-sm text-neutral-500" role="status">Guardando respuesta…</p>
          )}
          {saveError && <p className="text-sm text-red-600" role="alert">{saveError}</p>}
        </section>

        <div className="mt-auto space-y-3 pt-4">
          {isLastQuestion && !canComplete && (
            <p className="text-sm leading-relaxed text-neutral-600" role="status">
              Completá las competencias pendientes para finalizar. Podés volver con Anterior.
            </p>
          )}
          {completionError && <p className="text-sm text-red-600" role="alert">{completionError}</p>}

          {confirmDiscard ? (
            <div className="space-y-2 rounded-xl border border-amber-200 bg-amber-50 p-3">
              <p className="text-sm text-neutral-700">Se conservará esta evaluación como descartada. Podés comenzar otra con las respuestas de tu última evaluación completa.</p>
              <div className="grid grid-cols-2 gap-2">
                <Button variant="secondary" disabled={isRestarting} onClick={() => setConfirmDiscard(false)}>Seguir evaluación</Button>
                <Button disabled={isRestarting} onClick={handleRestart}>{isRestarting ? 'Preparando…' : 'Confirmar y empezar otra'}</Button>
              </div>
            </div>
          ) : (
            <Button fullWidth variant="secondary" disabled={isCompleting || isRestarting} onClick={() => setConfirmDiscard(true)}>
              Descartar borrador y empezar otra
            </Button>
          )}

          {isLastQuestion && canComplete && (
            <Button
              fullWidth
              className="min-h-12"
              disabled={attempt.status !== 'DRAFT' || isCompleting || currentQuestionIsSaving || currentQuestionSaveFailed}
              onClick={handleComplete}
            >
              {isCompleting ? 'Finalizando…' : 'Finalizar evaluación'}
            </Button>
          )}

          <div className="grid grid-cols-2 gap-3">
            <Button
              variant="secondary"
              className="min-h-12 px-3"
              disabled={currentQuestionIndex === 0}
              onClick={() => setCurrentPosition(questions[currentQuestionIndex - 1].position)}
            >
              ← Anterior
            </Button>
            <Button
              variant="secondary"
              className="min-h-12 px-3"
              disabled={isLastQuestion || !currentQuestionHasSavedAnswer || currentQuestionIsSaving || currentQuestionSaveFailed}
              aria-label="Siguiente pregunta"
              onClick={() => setCurrentPosition(questions[currentQuestionIndex + 1].position)}
            >
              Siguiente →
            </Button>
          </div>
        </div>
      </div>
    </AppShell>
  )
}
