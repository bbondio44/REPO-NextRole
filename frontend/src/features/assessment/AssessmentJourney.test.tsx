import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { AssessmentJourney } from '@/features/assessment/AssessmentJourney'

const originalGetTotalLength = Object.getOwnPropertyDescriptor(SVGElement.prototype, 'getTotalLength')
const originalGetPointAtLength = Object.getOwnPropertyDescriptor(SVGElement.prototype, 'getPointAtLength')
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

function setPathGeometry() {
  Object.defineProperty(SVGElement.prototype, 'getTotalLength', {
    configurable: true,
    value: () => 600,
  })
  Object.defineProperty(SVGElement.prototype, 'getPointAtLength', {
    configurable: true,
    value: (length: number) => ({ x: 28 + length * 0.8, y: 178 - length * 0.08 }),
  })
}

describe('AssessmentJourney', () => {
  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
    if (originalGetTotalLength) {
      Object.defineProperty(SVGElement.prototype, 'getTotalLength', originalGetTotalLength)
    } else {
      Reflect.deleteProperty(SVGElement.prototype, 'getTotalLength')
    }
    if (originalGetPointAtLength) {
      Object.defineProperty(SVGElement.prototype, 'getPointAtLength', originalGetPointAtLength)
    } else {
      Reflect.deleteProperty(SVGElement.prototype, 'getPointAtLength')
    }
    if (originalMatchMedia) {
      Object.defineProperty(window, 'matchMedia', originalMatchMedia)
    } else {
      Reflect.deleteProperty(window, 'matchMedia')
    }
  })

  it('renders one accessible path whose color endpoint and mascot share the same progress', () => {
    setReducedMotion(true)
    render(<AssessmentJourney progress={0.42} completed={false} />)

    const progress = screen.getByRole('progressbar', { name: 'Progreso de autoevaluación' })
    const mascot = screen.getByTestId('assessment-journey-mascot')
    const path = screen.getByTestId('assessment-journey-progress-path')

    expect(progress).toHaveAttribute('aria-valuenow', '42')
    expect(screen.queryByText('42%')).not.toBeInTheDocument()
    expect(progress).toHaveAttribute('aria-valuetext', '42% completado')
    expect(mascot).toHaveAttribute('data-progress', '0.42')
    expect(path).toHaveAttribute('data-progress', '0.42')
    expect(screen.getAllByTestId(/assessment-journey-(?:track|progress)-path/)).toHaveLength(2)
  })

  it('shows the mascot resting at a fully saved draft without celebrating', () => {
    setReducedMotion(true)
    render(<AssessmentJourney progress={1} completed={false} />)

    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '100')
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'idle')
  })

  it('celebrates only when completion is confirmed', () => {
    setReducedMotion(true)
    render(<AssessmentJourney progress={1} completed />)

    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '100')
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'celebration')
  })

  it('shows the target position immediately when reduced motion is preferred', () => {
    setReducedMotion(true)
    render(<AssessmentJourney progress={0.7} completed={false} />)

    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '70')
    expect(screen.getByTestId('assessment-journey-mascot')).toHaveAttribute('data-pose', 'idle')
    expect(screen.getByTestId('assessment-journey-mascot')).not.toHaveAttribute('data-walking', 'true')
  })

  it('synchronizes a finite walk with the path and stops at its target', async () => {
    setReducedMotion(false)
    expect(window.matchMedia('(prefers-reduced-motion: reduce)').matches).toBe(false)
    setPathGeometry()
    let nextFrameId = 0
    const frames = new Map<number, FrameRequestCallback>()
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => {
      const id = nextFrameId++
      frames.set(id, callback)
      return id
    })
    vi.stubGlobal('cancelAnimationFrame', (id: number) => frames.delete(id))
    render(<AssessmentJourney progress={0.6} completed={false} />)

    const mascot = await screen.findByTestId('assessment-journey-mascot')
    expect(mascot).toHaveAttribute('data-walking', 'false')
    fireEvent.load(screen.getByTestId('assessment-journey-walk-preload'))
    await waitFor(() => expect(mascot).toHaveAttribute('data-walking', 'true'))
    expect(Number(mascot.style.getPropertyValue('--walk-cycle-count'))).toBe(2)
    expect(mascot.style.getPropertyValue('--walk-cycle-duration')).toBe('1000ms')
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '60')

    const runFrame = (time: number) => {
      const [id, callback] = [...frames.entries()][0] ?? []
      if (id === undefined || !callback) throw new Error('Expected a scheduled animation frame')
      frames.delete(id)
      act(() => callback(time))
    }

    runFrame(0)
    runFrame(1000)
    expect(Number(mascot.getAttribute('data-progress'))).toBeCloseTo(0.3)
    expect(Number(screen.getByTestId('assessment-journey-progress-path').getAttribute('data-progress'))).toBeCloseTo(0.3)
    runFrame(2000)

    expect(mascot).toHaveAttribute('data-walking', 'false')
    expect(mascot).toHaveAttribute('data-pose', 'idle')
    expect(Number(mascot.getAttribute('data-progress'))).toBeCloseTo(0.6)
    expect(Number(screen.getByTestId('assessment-journey-progress-path').getAttribute('data-progress'))).toBeCloseTo(0.6)
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '60')
  })

  it('continues the entry animation without restarting when saved progress refreshes', async () => {
    setReducedMotion(false)
    setPathGeometry()
    let nextFrameId = 0
    const frames = new Map<number, FrameRequestCallback>()
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => {
      const id = nextFrameId++
      frames.set(id, callback)
      return id
    })
    vi.stubGlobal('cancelAnimationFrame', (id: number) => frames.delete(id))
    const { rerender } = render(<AssessmentJourney progress={0.6} completed={false} />)

    const mascot = await screen.findByTestId('assessment-journey-mascot')
    fireEvent.load(screen.getByTestId('assessment-journey-walk-preload'))
    await waitFor(() => expect(mascot).toHaveAttribute('data-walking', 'true'))

    const runFrame = (time: number) => {
      const [id, callback] = [...frames.entries()][0] ?? []
      if (id === undefined || !callback) throw new Error('Expected a scheduled animation frame')
      frames.delete(id)
      act(() => callback(time))
    }

    runFrame(0)
    runFrame(1000)
    expect(Number(mascot.getAttribute('data-progress'))).toBeCloseTo(0.3)
    rerender(<AssessmentJourney progress={0.8} completed={false} />)
    expect(mascot).toHaveAttribute('data-walking', 'true')
    expect(Number(mascot.getAttribute('data-progress'))).toBeCloseTo(0.3)
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '80')
    expect(frames.size).toBe(1)

    runFrame(1500)
    expect(Number(mascot.getAttribute('data-progress'))).toBeCloseTo(0.6)
    runFrame(2000)
    expect(mascot).toHaveAttribute('data-walking', 'false')
    expect(Number(mascot.getAttribute('data-progress'))).toBeCloseTo(0.8)
  })

  it('stays still at the beginning when saved progress is zero', async () => {
    setReducedMotion(false)
    setPathGeometry()
    render(<AssessmentJourney progress={0} completed={false} />)

    const mascot = await screen.findByTestId('assessment-journey-mascot')
    expect(mascot).toHaveAttribute('data-walking', 'false')
    expect(mascot).toHaveAttribute('data-pose', 'pointing')
    expect(mascot).toHaveAttribute('data-progress', '0')
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '0')
  })

  it('plays the entry walk again after the journey is mounted again', async () => {
    setReducedMotion(false)
    setPathGeometry()
    let nextFrameId = 0
    const frames = new Map<number, FrameRequestCallback>()
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => {
      const id = nextFrameId++
      frames.set(id, callback)
      return id
    })
    vi.stubGlobal('cancelAnimationFrame', (id: number) => frames.delete(id))

    const first = render(<AssessmentJourney progress={0.25} completed={false} />)
    fireEvent.load(screen.getByTestId('assessment-journey-walk-preload'))
    const firstMascot = await screen.findByTestId('assessment-journey-mascot')
    await waitFor(() => expect(firstMascot).toHaveAttribute('data-walking', 'true'))
    first.unmount()

    render(<AssessmentJourney progress={0.25} completed={false} />)
    fireEvent.load(screen.getByTestId('assessment-journey-walk-preload'))
    const secondMascot = await screen.findByTestId('assessment-journey-mascot')
    await waitFor(() => expect(secondMascot).toHaveAttribute('data-walking', 'true'))
    expect(secondMascot).toHaveAttribute('data-progress', '0')
  })
})
