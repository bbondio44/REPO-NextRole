import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'

import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { ConfirmDiscardDialog } from '@/components/ui/ConfirmDiscardDialog'
import { Mascot } from '@/components/ui/Mascot'
import { AssessmentIntroDialog } from '@/features/assessment/AssessmentIntroDialog'
import { AssessmentJourney } from '@/features/assessment/AssessmentJourney'
import { getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import { useDiscardAssessmentAttempt } from '@/features/assessment/useDiscardAssessmentAttempt'
import { useStartAssessmentAttempt } from '@/features/assessment/useStartAssessmentAttempt'

interface AssessmentAccessPanelProps {
  showDraftDiscardAction?: boolean
}

export function AssessmentAccessPanel({ showDraftDiscardAction = false }: AssessmentAccessPanelProps) {
  const [confirmDiscard, setConfirmDiscard] = useState(false)
  const [introOpen, setIntroOpen] = useState(false)
  const [introRun, setIntroRun] = useState(0)
  const { discard, isDiscarding, discardError, clearDiscardError } = useDiscardAssessmentAttempt()
  const current = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt })
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })

  const draft = current.data
  const latestCompleted = history.data?.[0]
  const { startAssessment, starting, startError, clearStartError } = useStartAssessmentAttempt(Boolean(latestCompleted))
  const hasDraft = draft?.status === 'DRAFT'
  const journeyProgress = hasDraft && draft.totalQuestions > 0 ? draft.answeredCount / draft.totalQuestions : 0

  async function discardDraft() {
    if (!draft || draft.status !== 'DRAFT') return
    clearStartError()
    try {
      await discard(draft.attemptId)
      setConfirmDiscard(false)
    } catch {
      // The shared dialog presents the error and keeps the draft visible.
    }
  }

  function openIntro() {
    clearStartError()
    setIntroRun((run) => run + 1)
    setIntroOpen(true)
  }

  function closeIntro() {
    setIntroOpen(false)
    clearStartError()
  }

  if (current.isLoading || history.isLoading) return <p role="status">Consultando tu autoevaluación…</p>
  if (current.isError || history.isError) return <p role="alert">No pudimos consultar el estado de tu autoevaluación.</p>

  return (
    <Card className="mt-4 space-y-3">
      {hasDraft ? (
        <>
          <div>
            <h2 className="text-base font-bold text-neutral-950">Autoevaluación de habilidades</h2>
            <p className="mt-1 text-sm text-neutral-600">Tenés una evaluación en curso. Tus respuestas guardadas siguen disponibles.</p>
          </div>
          <AssessmentJourney progress={journeyProgress} completed={false} />
        </>
      ) : (
        <div className="flex flex-col items-center gap-3 py-3 text-center">
          <Mascot className="h-24 w-24" pose="wave" />
          <h2 className="text-lg font-extrabold text-[#12304A]">
            {latestCompleted ? 'Seguí evolucionando' : 'Descubrí tus habilidades'}
          </h2>
          <p className="max-w-sm text-sm leading-relaxed text-neutral-600">
            {latestCompleted
              ? 'Volvé a evaluar tus habilidades y descubrí cuánto avanzaste.'
              : 'Completá tu primera autoevaluación para conocer tus habilidades y los roles que se adaptan a vos.'}
          </p>
        </div>
      )}

      {draft && (
        <Link to="/habilidades/evaluacion" className="block">
          <Button fullWidth>Continuar evaluación</Button>
        </Link>
      )}

      {showDraftDiscardAction && draft && !confirmDiscard && (
        <button
          type="button"
          className="text-xs font-medium text-neutral-500 underline-offset-2 hover:text-neutral-700 hover:underline disabled:opacity-50"
          disabled={isDiscarding}
          onClick={() => { clearDiscardError(); clearStartError(); setConfirmDiscard(true) }}
        >
          Descartar borrador
        </button>
      )}

      {!draft && (
        <Button fullWidth disabled={starting} onClick={openIntro}>
          {starting ? 'Preparando evaluación…' : latestCompleted ? 'Comenzar nueva evaluación' : 'Comenzar evaluación'}
        </Button>
      )}
      {latestCompleted && (
        <Link to="/habilidades/historial" className="block text-center text-sm font-semibold text-primary-700">
          Ver historial de evaluaciones
        </Link>
      )}
      <AssessmentIntroDialog
        key={introRun}
        open={introOpen}
        playbackRun={introRun}
        starting={starting}
        error={startError}
        onClose={closeIntro}
        onConfirm={startAssessment}
      />
      <ConfirmDiscardDialog
        open={confirmDiscard && Boolean(hasDraft)}
        pending={isDiscarding}
        error={discardError}
        onCancel={() => setConfirmDiscard(false)}
        onConfirm={discardDraft}
      />
    </Card>
  )
}
