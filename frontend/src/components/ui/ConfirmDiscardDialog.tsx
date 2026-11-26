import { useEffect, useRef } from 'react'

import { Button } from '@/components/ui/Button'

interface ConfirmDiscardDialogProps {
  open: boolean
  pending: boolean
  error: string | null
  onCancel: () => void
  onConfirm: () => void
}

export function ConfirmDiscardDialog({ open, pending, error, onCancel, onConfirm }: ConfirmDiscardDialogProps) {
  const cancelRef = useRef<HTMLButtonElement>(null)
  const dialogRef = useRef<HTMLElement>(null)
  const cancelActionRef = useRef(onCancel)

  useEffect(() => {
    cancelActionRef.current = onCancel
  }, [onCancel])

  useEffect(() => {
    if (!open) return
    cancelRef.current?.focus()
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !pending) {
        cancelActionRef.current()
        return
      }
      if (event.key !== 'Tab') return
      const focusable = dialogRef.current?.querySelectorAll<HTMLElement>('button:not(:disabled), [href], [tabindex]:not([tabindex="-1"])')
      if (!focusable?.length) return
      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [open, pending])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 p-4" data-testid="discard-dialog-backdrop">
      <section
        role="dialog"
        aria-modal="true"
        aria-busy={pending}
        aria-labelledby="discard-dialog-title"
        aria-describedby="discard-dialog-description"
        ref={dialogRef}
        className="w-full max-w-sm rounded-2xl bg-white p-5 shadow-xl shadow-slate-950/20"
      >
        <h2 id="discard-dialog-title" className="text-lg font-bold text-neutral-950">¿Descartar autoevaluación?</h2>
        <p id="discard-dialog-description" className="mt-2 text-sm leading-relaxed text-neutral-600">
          ¿Estás seguro? Se perderá el progreso de este borrador.
        </p>
        {error && <p className="mt-3 text-sm text-red-700" role="alert">{error}</p>}
        <div className="mt-5 flex justify-end gap-3">
          <Button ref={cancelRef} variant="secondary" disabled={pending} onClick={onCancel}>Cancelar</Button>
          <Button
            className="!bg-red-600 !text-white hover:!bg-red-700 disabled:!bg-red-300"
            disabled={pending}
            onClick={onConfirm}
          >
            {pending ? 'Descartando…' : 'Descartar'}
          </Button>
        </div>
      </section>
    </div>
  )
}
