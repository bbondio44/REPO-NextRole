import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import type { SyntheticEvent } from 'react'

import { Button } from '@/components/ui/Button'
import mascotQuizAnimation from '@/assets/nextrole-quiz-assets/mascot-quiz.webp'
import mascotQuizFinal from '@/assets/nextrole-quiz-assets/mascot-quiz-final.webp'
import { IntroductionSteps } from '@/features/assessment/NextRoleSteps'
import './AssessmentIntroDialog.css'

function useReducedMotion() {
  const [reducedMotion, setReducedMotion] = useState(() => (
    typeof window !== 'undefined'
      && typeof window.matchMedia === 'function'
      && window.matchMedia('(prefers-reduced-motion: reduce)').matches
  ))

  useEffect(() => {
    if (typeof window.matchMedia !== 'function') return
    const media = window.matchMedia('(prefers-reduced-motion: reduce)')
    const update = () => setReducedMotion(media.matches)
    update()
    media.addEventListener?.('change', update)
    return () => media.removeEventListener?.('change', update)
  }, [])

  return reducedMotion
}

interface AssessmentIntroDialogProps {
  open: boolean
  playbackRun: number
  starting: boolean
  error: string | null
  onClose: () => void
  onConfirm: () => void
}

export function AssessmentIntroDialog({ open, playbackRun, starting, error, onClose, onConfirm }: AssessmentIntroDialogProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const titleRef = useRef<HTMLHeadingElement>(null)
  const previousFocusRef = useRef<HTMLElement | null>(null)
  const reducedMotion = useReducedMotion()
  const [readyRun, setReadyRun] = useState<number | null>(null)
  const [failedRun, setFailedRun] = useState<number | null>(null)

  useLayoutEffect(() => {
    const dialog = dialogRef.current
    if (!open || !dialog) return
    previousFocusRef.current = document.activeElement instanceof HTMLElement ? document.activeElement : null
    if (!dialog.open) {
      if (typeof dialog.showModal === 'function') dialog.showModal()
      else dialog.setAttribute('open', '')
    }
    titleRef.current?.focus()
    return () => {
      if (dialog.open) {
        if (typeof dialog.close === 'function') dialog.close()
        else dialog.removeAttribute('open')
      }
      previousFocusRef.current?.focus()
      previousFocusRef.current = null
    }
  }, [open])

  function handleCancel(event: SyntheticEvent<HTMLDialogElement>) {
    event.preventDefault()
    if (!starting) closeDialog()
  }

  function closeDialog() {
    const dialog = dialogRef.current
    if (dialog?.open) {
      if (typeof dialog.close === 'function') dialog.close()
      else dialog.removeAttribute('open')
    }
    previousFocusRef.current?.focus()
    previousFocusRef.current = null
    onClose()
  }

  const showAnimation = !reducedMotion && failedRun !== playbackRun
  const animationReady = readyRun === playbackRun

  if (!open) return null

  return (
    <dialog
      ref={dialogRef}
      className="assessment-intro-dialog"
      aria-modal="true"
      aria-labelledby="assessment-intro-title"
      aria-describedby="assessment-intro-description"
      aria-busy={starting}
      data-testid="assessment-intro-dialog"
      onCancel={handleCancel}
    >
      <div className="assessment-intro-dialog__content">
        <button
          type="button"
          className="assessment-intro-dialog__close"
          aria-label="Cerrar introducción"
          disabled={starting}
          onClick={closeDialog}
        >
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18" /></svg>
        </button>

        {open && (
          <div className="assessment-intro-dialog__mascot" data-testid="assessment-intro-mascot">
            {showAnimation ? (
              <>
                <img className="assessment-intro-dialog__mascot-final" src={mascotQuizFinal} alt="" aria-hidden="true" />
                <img
                  key={playbackRun}
                  className={`assessment-intro-dialog__mascot-animation${animationReady ? ' is-ready' : ''}`}
                  src={mascotQuizAnimation}
                  alt="Mascota NextRole leyendo un cuestionario y satisfecha con sus respuestas"
                  loading="eager"
                  decoding="async"
                  data-testid="assessment-intro-mascot-animation"
                  data-playback-run={playbackRun}
                  onLoad={() => setReadyRun(playbackRun)}
                  onError={() => setFailedRun(playbackRun)}
                />
              </>
            ) : (
              <img
                className="assessment-intro-dialog__mascot-static"
                src={mascotQuizFinal}
                alt="Mascota NextRole con una expresión de satisfacción"
                data-testid="assessment-intro-mascot-static"
              />
            )}
          </div>
        )}

        <header className="assessment-intro-dialog__header">
          <h2 id="assessment-intro-title" ref={titleRef} tabIndex={-1}>¡Descubrí tu potencial!</h2>
          <p className="assessment-intro-dialog__subtitle">Tu próximo paso empieza acá.</p>
          <p id="assessment-intro-description" className="assessment-intro-dialog__description">
            Conocé cómo funciona NextRole en cuatro simples pasos.
          </p>
        </header>

        <IntroductionSteps />

        {error && <p role="alert" className="assessment-intro-dialog__error">{error}</p>}

        <div className="assessment-intro-dialog__actions">
          <Button fullWidth disabled={starting} onClick={onConfirm}>
            {starting ? 'Preparando evaluación…' : 'Comenzar autoevaluación'}
          </Button>
          <Button variant="secondary" fullWidth disabled={starting} onClick={closeDialog}>Ahora no</Button>
        </div>
      </div>
    </dialog>
  )
}
