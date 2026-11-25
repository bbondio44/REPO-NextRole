import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { createAssessmentAttempt, createAssessmentReevaluation, discardAssessmentAttempt, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

interface AssessmentAccessPanelProps {
  showDraftDiscardAction?: boolean
}

export function AssessmentAccessPanel({ showDraftDiscardAction = false }: AssessmentAccessPanelProps) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [starting, setStarting] = useState(false)
  const [startError, setStartError] = useState<string | null>(null)
  const [confirmDiscard, setConfirmDiscard] = useState(false)
  const [discarding, setDiscarding] = useState(false)
  const current = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt })
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })

  const draft = current.data
  const latestCompleted = history.data?.[0]

  async function startAssessment() {
    setStarting(true)
    setStartError(null)
    try {
      const attempt = latestCompleted
        ? await createAssessmentReevaluation()
        : await createAssessmentAttempt()
      queryClient.setQueryData<AssessmentAttempt | null>(['assessment-attempt'], attempt)
      navigate('/habilidades/evaluacion')
    } catch (error) {
      setStartError(error instanceof Error ? error.message : 'No pudimos comenzar la evaluación.')
    } finally {
      setStarting(false)
    }
  }

  async function discardAndRestart() {
    if (!draft || draft.status !== 'DRAFT') return
    setDiscarding(true)
    setStartError(null)
    try {
      await discardAssessmentAttempt(draft.attemptId)
      queryClient.setQueryData<AssessmentAttempt | null>(['assessment-attempt'], null)
      const nextAttempt = await createAssessmentReevaluation()
      queryClient.setQueryData<AssessmentAttempt | null>(['assessment-attempt'], nextAttempt)
      await queryClient.invalidateQueries({ queryKey: ['assessment-history'] })
      navigate('/habilidades/evaluacion')
    } catch (error) {
      setStartError(error instanceof Error ? error.message : 'No pudimos iniciar otra evaluación.')
    } finally {
      setDiscarding(false)
      setConfirmDiscard(false)
    }
  }

  if (current.isLoading || history.isLoading) return <p role="status">Consultando tu autoevaluación…</p>
  if (current.isError || history.isError) return <p role="alert">No pudimos consultar el estado de tu autoevaluación.</p>

  return (
    <Card className="mt-4 space-y-3">
      <div>
        <h2 className="text-base font-bold text-neutral-950">Autoevaluación de habilidades</h2>
        <p className="mt-1 text-sm text-neutral-600">
          {draft
            ? 'Tenés una evaluación en curso. Tus respuestas guardadas siguen disponibles.'
            : latestCompleted
              ? 'Tu evaluación está completada. Podés consultar sus resultados.'
              : 'Completá la autoevaluación para descubrir roles compatibles con tu perfil.'}
        </p>
      </div>

      {draft && (
        <Link to="/habilidades/evaluacion" className="block">
          <Button fullWidth>Continuar evaluación</Button>
        </Link>
      )}

      {showDraftDiscardAction && draft && !confirmDiscard && (
        <button
          type="button"
          className="text-xs font-medium text-neutral-500 underline-offset-2 hover:text-neutral-700 hover:underline disabled:opacity-50"
          disabled={discarding}
          onClick={() => setConfirmDiscard(true)}
        >
          Descartar borrador
        </button>
      )}

      {showDraftDiscardAction && draft && confirmDiscard && (
        <div className="space-y-2 rounded-lg border border-amber-200 bg-amber-50 p-3" aria-label="Confirmar descarte de evaluación">
          <p className="text-sm text-neutral-700">Se conservará como descartada. La nueva evaluación usará las respuestas de tu última evaluación completa.</p>
          <div className="flex items-center justify-between gap-3">
            <button
              type="button"
              className="text-sm text-neutral-600 hover:text-neutral-900 disabled:opacity-50"
              disabled={discarding}
              onClick={() => setConfirmDiscard(false)}
            >
              Cancelar
            </button>
            <Button variant="secondary" disabled={discarding} onClick={discardAndRestart}>
              {discarding ? 'Preparando…' : 'Confirmar y empezar otra'}
            </Button>
          </div>
        </div>
      )}

      {latestCompleted && (
        <Link to={`/roles?attemptId=${encodeURIComponent(latestCompleted.attemptId)}`} className="block">
          <Button fullWidth variant={draft ? 'secondary' : 'primary'}>
            {draft ? 'Ver mis resultados anteriores' : 'Ver mis resultados'}
          </Button>
        </Link>
      )}

      {!draft && (
        <Button fullWidth disabled={starting} onClick={startAssessment}>
          {starting ? 'Preparando evaluación…' : latestCompleted ? 'Comenzar nueva evaluación' : 'Comenzar evaluación'}
        </Button>
      )}
      {latestCompleted && (
        <Link to="/habilidades/historial" className="block text-center text-sm font-semibold text-primary-700">
          Ver historial de evaluaciones
        </Link>
      )}
      {startError && <p role="alert" className="text-sm text-red-600">{startError}</p>}
    </Card>
  )
}
