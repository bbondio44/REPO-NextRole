import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import type { CSSProperties } from 'react'

import mascotIdle from '@/assets/nextrole-mascot-animation/mascot-animation/mascot-idle.png'
import mascotPointing from '@/assets/nextrole-mascot-animation/mascot-animation/mascot-pointing.png'
import mascotWalkSprite from '@/assets/nextrole-mascot-animation/mascot-animation/mascot-walk-spritesheet.png'
import mascotWave from '@/assets/brand/mascot-wave.png'

import './AssessmentJourney.css'

const ROUTE_PATH = 'M 28 178 C 112 190, 122 128, 220 136 S 322 204, 394 164 S 470 120, 566 132'
const VIEWBOX_WIDTH = 600
const VIEWBOX_HEIGHT = 240
const WALK_FRAME_COUNT = 8
const WALK_FRAME_DURATION_MS = 110
const WALK_CYCLE_DURATION_MS = WALK_FRAME_COUNT * WALK_FRAME_DURATION_MS
const MAX_WALK_CYCLES = 2
const MAX_TRAVEL_DURATION_MS = WALK_CYCLE_DURATION_MS * MAX_WALK_CYCLES
const START_POINT = { x: 28, y: 178 }

interface JourneyPoint {
  x: number
  y: number
}

interface AssessmentJourneyProps {
  /** Normalized saved-answer progress, from 0 to 1. */
  progress: number
  /** True only when completion has been confirmed by the application. */
  completed: boolean
}

function clampProgress(progress: number) {
  if (!Number.isFinite(progress)) return 0
  return Math.min(1, Math.max(0, progress))
}

function usePrefersReducedMotion() {
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return true
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches
  })

  useEffect(() => {
    if (!window.matchMedia) return
    const media = window.matchMedia('(prefers-reduced-motion: reduce)')
    const update = () => setPrefersReducedMotion(media.matches)
    update()
    media.addEventListener?.('change', update)
    return () => media.removeEventListener?.('change', update)
  }, [])

  return prefersReducedMotion
}

function getPointAtProgress(path: SVGPathElement | null, pathLength: number, value: number): JourneyPoint {
  if (path && pathLength > 0 && typeof path.getPointAtLength === 'function') {
    return path.getPointAtLength(pathLength * clampProgress(value))
  }
  const safeValue = clampProgress(value)
  return {
    x: START_POINT.x + (566 - START_POINT.x) * safeValue,
    y: START_POINT.y + (132 - START_POINT.y) * safeValue,
  }
}

export function AssessmentJourney({ progress, completed }: AssessmentJourneyProps) {
  const targetProgress = completed ? 1 : clampProgress(progress)
  const prefersReducedMotion = usePrefersReducedMotion()
  const routeRef = useRef<SVGPathElement>(null)
  const animationFrameRef = useRef<number | null>(null)
  const initialProgress = prefersReducedMotion || completed ? targetProgress : 0
  const progressRef = useRef(initialProgress)
  const [pathLength, setPathLength] = useState(0)
  const [visualProgress, setVisualProgress] = useState(initialProgress)
  const [point, setPoint] = useState<JourneyPoint>(START_POINT)
  const [walkingCycleCount, setWalkingCycleCount] = useState(0)
  const [isWalking, setIsWalking] = useState(false)

  useLayoutEffect(() => {
    const path = routeRef.current
    if (!path || typeof path.getTotalLength !== 'function') return
    setPathLength(path.getTotalLength())
    if (typeof path.getPointAtLength === 'function') setPoint(path.getPointAtLength(0))
  }, [])

  useEffect(() => {
    if (animationFrameRef.current !== null) {
      window.cancelAnimationFrame(animationFrameRef.current)
      animationFrameRef.current = null
    }

    const startProgress = progressRef.current
    if (pathLength <= 0) return

    if (completed || prefersReducedMotion || startProgress === targetProgress) {
      progressRef.current = targetProgress
      setVisualProgress(targetProgress)
      setPoint(getPointAtProgress(routeRef.current, pathLength, targetProgress))
      setWalkingCycleCount(0)
      setIsWalking(false)
      return
    }

    const distance = Math.abs(targetProgress - startProgress) * pathLength
    const distancePerCycle = pathLength * 0.42
    const cycleCount = Math.min(
      MAX_WALK_CYCLES,
      Math.max(1, Math.ceil(distance / distancePerCycle)),
    )
    const duration = Math.min(MAX_TRAVEL_DURATION_MS, cycleCount * WALK_CYCLE_DURATION_MS)
    const from = startProgress
    const delta = targetProgress - from
    let startTime: number | null = null

    setWalkingCycleCount(cycleCount)
    setIsWalking(true)

    const step = (timestamp: number) => {
      if (startTime === null) startTime = timestamp
      const elapsed = Math.min(1, (timestamp - startTime) / duration)
      const nextProgress = from + delta * elapsed
      progressRef.current = nextProgress
      setVisualProgress(nextProgress)
      setPoint(getPointAtProgress(routeRef.current, pathLength, nextProgress))

      if (elapsed < 1) {
        animationFrameRef.current = window.requestAnimationFrame(step)
        return
      }

      animationFrameRef.current = null
      progressRef.current = targetProgress
      setVisualProgress(targetProgress)
      setPoint(getPointAtProgress(routeRef.current, pathLength, targetProgress))
      setIsWalking(false)
    }

    animationFrameRef.current = window.requestAnimationFrame(step)
    return () => {
      if (animationFrameRef.current !== null) {
        window.cancelAnimationFrame(animationFrameRef.current)
        animationFrameRef.current = null
      }
    }
  }, [completed, pathLength, prefersReducedMotion, targetProgress])

  const safeProgress = clampProgress(visualProgress)
  const percentage = Math.round(safeProgress * 100)
  const mascotPose = completed ? 'celebration' : isWalking ? 'walking' : safeProgress === 0 ? 'pointing' : 'idle'
  const mascotLabel = mascotPose === 'celebration'
    ? 'Mascota celebrando la evaluación completada'
    : mascotPose === 'walking'
      ? 'Mascota avanzando por el recorrido'
      : 'Mascota en el recorrido'
  const mascotStyle = {
    left: `${(point.x / VIEWBOX_WIDTH) * 100}%`,
    top: `${(point.y / VIEWBOX_HEIGHT) * 100}%`,
    '--walk-cycle-duration': `${WALK_CYCLE_DURATION_MS}ms`,
    '--walk-cycle-count': walkingCycleCount,
    '--walk-sprite-url': `url("${mascotWalkSprite}")`,
  } as CSSProperties

  return (
    <section
      className="assessment-journey"
      role="progressbar"
      aria-label="Progreso de autoevaluación"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={percentage}
      aria-valuetext={`${percentage}% completado`}
      data-target-progress={targetProgress}
      data-completed={completed}
    >
      <div className="assessment-journey__scene">
        <svg
          className="assessment-journey__landscape"
          viewBox={`0 0 ${VIEWBOX_WIDTH} ${VIEWBOX_HEIGHT}`}
          preserveAspectRatio="none"
          aria-hidden="true"
        >
          <ellipse className="assessment-journey__hill assessment-journey__hill--back" cx="128" cy="242" rx="176" ry="77" />
          <ellipse className="assessment-journey__hill assessment-journey__hill--front" cx="506" cy="251" rx="211" ry="82" />
          <g className="assessment-journey__cloud" transform="translate(72 37)">
            <circle cx="20" cy="15" r="15" />
            <circle cx="40" cy="10" r="19" />
            <circle cx="61" cy="17" r="13" />
            <rect x="19" y="16" width="44" height="14" rx="7" />
          </g>
          <g className="assessment-journey__cloud assessment-journey__cloud--small" transform="translate(385 41)">
            <circle cx="15" cy="12" r="12" />
            <circle cx="32" cy="8" r="15" />
            <circle cx="49" cy="13" r="10" />
            <rect x="14" y="13" width="36" height="11" rx="5.5" />
          </g>
          <path
            ref={routeRef}
            data-testid="assessment-journey-track-path"
            d={ROUTE_PATH}
            className="assessment-journey__route assessment-journey__route--pending"
            pathLength={1000}
          />
          <path
            data-testid="assessment-journey-progress-path"
            d={ROUTE_PATH}
            className="assessment-journey__route assessment-journey__route--complete"
            pathLength={1000}
            strokeDasharray={`${safeProgress * 1000} 1000`}
            data-progress={safeProgress}
          />
          <g className="assessment-journey__goal" transform="translate(566 132)">
            <line x1="0" y1="1" x2="0" y2="-56" />
            <path d="M 1 -55 L 35 -47 L 1 -37 Z" />
            <circle cx="0" cy="1" r="4" />
          </g>
        </svg>
        <div
          className={`assessment-journey__mascot assessment-journey__mascot--${mascotPose}`}
          style={mascotStyle}
          role="img"
          aria-label={mascotLabel}
          data-testid="assessment-journey-mascot"
          data-pose={mascotPose}
          data-progress={safeProgress}
          data-walking={isWalking ? 'true' : 'false'}
        >
          {completed ? (
            <img className="assessment-journey__celebration" src={mascotWave} alt="" />
          ) : isWalking ? (
            <span className="assessment-journey__walk-sprite" aria-hidden="true" />
          ) : (
            <img
              className="assessment-journey__static-pose"
              src={safeProgress === 0 ? mascotPointing : mascotIdle}
              alt=""
            />
          )}
        </div>
      </div>
    </section>
  )
}
