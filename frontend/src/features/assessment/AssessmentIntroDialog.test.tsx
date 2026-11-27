import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentIntroDialog } from '@/features/assessment/AssessmentIntroDialog'

const originalMatchMedia = Object.getOwnPropertyDescriptor(window, 'matchMedia')

function setReducedMotion(matches: boolean) {
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    value: vi.fn().mockReturnValue({
      matches,
      media: '(prefers-reduced-motion: reduce)',
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }),
  })
}

function renderDialog(open = true, starting = false, error: string | null = null, playbackRun = 1) {
  const props = { open, starting, error, playbackRun, onClose: vi.fn(), onConfirm: vi.fn() }
  const result = render(<AssessmentIntroDialog {...props} />)
  return { ...result, props }
}

describe('AssessmentIntroDialog', () => {
  afterEach(() => {
    cleanup()
    vi.restoreAllMocks()
    if (originalMatchMedia) Object.defineProperty(window, 'matchMedia', originalMatchMedia)
    else Reflect.deleteProperty(window, 'matchMedia')
  })

  it('shows the approved copy and all four explanatory steps in an accessible modal', () => {
    setReducedMotion(false)
    renderDialog()

    expect(screen.getByRole('dialog', { name: '¡Descubrí tu potencial!' })).toHaveAttribute('aria-modal', 'true')
    expect(screen.getByText('Tu próximo paso empieza acá.')).toBeInTheDocument()
    expect(screen.getByText('Conocé cómo funciona NextRole en cuatro simples pasos.')).toBeInTheDocument()
    expect(screen.getByText('Identificá qué conocimientos tenés y cuál es tu nivel actual.')).toBeInTheDocument()
    expect(screen.getByText('Descubrí qué perfiles profesionales se ajustan mejor a tus habilidades.')).toBeInTheDocument()
    expect(screen.getByText('Conocé qué competencias necesitás fortalecer para acercarte a cada rol.')).toBeInTheDocument()
    expect(screen.getByText('Accedé a recomendaciones de formación orientadas a tu desarrollo profesional.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Comenzar autoevaluación' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ahora no' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cerrar introducción' })).toBeInTheDocument()
    expect(screen.getByTestId('assessment-intro-mascot-animation')).toHaveAttribute('src', expect.stringContaining('mascot-quiz.webp'))
  })

  it('closes by the secondary action, close control or Escape without confirming', () => {
    setReducedMotion(true)
    const first = renderDialog()

    fireEvent.click(screen.getByRole('button', { name: 'Ahora no' }))
    expect(first.props.onClose).toHaveBeenCalledTimes(1)
    expect(first.props.onConfirm).not.toHaveBeenCalled()
    first.unmount()

    const second = renderDialog()
    fireEvent.click(screen.getByRole('button', { name: 'Cerrar introducción' }))
    expect(second.props.onClose).toHaveBeenCalledTimes(1)
    second.unmount()

    const third = renderDialog()
    const dialog = screen.getByTestId('assessment-intro-dialog')
    fireEvent(dialog, new Event('cancel', { bubbles: true, cancelable: true }))
    expect(third.props.onClose).toHaveBeenCalledTimes(1)
    expect(third.props.onConfirm).not.toHaveBeenCalled()

    third.unmount()
    const pending = renderDialog(true, true)
    fireEvent(screen.getByTestId('assessment-intro-dialog'), new Event('cancel', { bubbles: true, cancelable: true }))
    expect(pending.props.onClose).not.toHaveBeenCalled()
  })

  it('confirms only on the primary action and shows pending/error state', () => {
    setReducedMotion(false)
    const { props, rerender } = renderDialog()
    fireEvent.click(screen.getByRole('button', { name: 'Comenzar autoevaluación' }))
    expect(props.onConfirm).toHaveBeenCalledTimes(1)
    expect(props.onClose).not.toHaveBeenCalled()

    rerender(<AssessmentIntroDialog open starting error="No pudimos comenzar." playbackRun={props.playbackRun} onClose={props.onClose} onConfirm={props.onConfirm} />)
    expect(screen.getByRole('button', { name: 'Preparando evaluación…' })).toBeDisabled()
    expect(screen.getByRole('alert')).toHaveTextContent('No pudimos comenzar.')
    expect(screen.getByRole('button', { name: 'Cerrar introducción' })).toBeDisabled()
  })

  it('uses the static final pose for reduced motion and falls back if WebP loading fails', () => {
    setReducedMotion(true)
    const first = renderDialog()
    expect(screen.queryByTestId('assessment-intro-mascot-animation')).not.toBeInTheDocument()
    expect(screen.getByTestId('assessment-intro-mascot-static')).toHaveAttribute('src', expect.stringContaining('mascot-quiz-final.webp'))

    first.unmount()
    setReducedMotion(false)
    renderDialog()
    fireEvent.error(screen.getByTestId('assessment-intro-mascot-animation'))
    expect(screen.queryByTestId('assessment-intro-mascot-animation')).not.toBeInTheDocument()
    expect(screen.getByTestId('assessment-intro-mascot-static')).toBeInTheDocument()
  })

  it('marks the animated image ready after load and remounts it for each opening', () => {
    setReducedMotion(false)
    const { rerender, props } = renderDialog(false)
    rerender(<AssessmentIntroDialog open starting={false} error={null} playbackRun={props.playbackRun} onClose={props.onClose} onConfirm={props.onConfirm} />)

    const firstImage = screen.getByTestId('assessment-intro-mascot-animation')
    const firstRun = firstImage.getAttribute('data-playback-run')
    fireEvent.load(firstImage)
    expect(firstImage).toHaveClass('is-ready')

    rerender(<AssessmentIntroDialog open={false} starting={false} error={null} playbackRun={props.playbackRun} onClose={props.onClose} onConfirm={props.onConfirm} />)
    rerender(<AssessmentIntroDialog open starting={false} error={null} playbackRun={props.playbackRun + 1} onClose={props.onClose} onConfirm={props.onConfirm} />)
    const secondImage = screen.getByTestId('assessment-intro-mascot-animation')

    expect(secondImage).not.toBe(firstImage)
    expect(Number(secondImage.getAttribute('data-playback-run'))).toBeGreaterThan(Number(firstRun))
    expect(secondImage).not.toHaveClass('is-ready')
  })
})
