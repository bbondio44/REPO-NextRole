import type { ReactNode } from 'react'

import { Card } from '@/components/ui/Card'
import { Mascot, type MascotPose } from '@/components/ui/Mascot'
import { NEXTROLE_STEPS, type NextRoleStep } from '@/features/assessment/NextRoleSteps.data'

import './NextRoleSteps.css'

export function NextRoleStepIcon({ name }: { name: NextRoleStep['icon'] }) {
  const paths: Record<NextRoleStep['icon'], ReactNode> = {
    chart: <path d="M4 19V5m0 14h16M8 15v-4m4 4V7m4 8V9" />,
    compass: <><circle cx="12" cy="12" r="9" /><path d="m15.8 8.2-2.3 5.3-5.3 2.3 2.3-5.3 5.3-2.3Z" /></>,
    target: <><circle cx="12" cy="12" r="9" /><circle cx="12" cy="12" r="5.5" /><circle cx="12" cy="12" r="1.7" /></>,
    cap: <><path d="m2.5 9 9.5-5 9.5 5-9.5 5-9.5-5Z" /><path d="M6 11v5c3.8 2.7 8.2 2.7 12 0v-5M21.5 9v6" /></>,
  }

  return (
    <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {paths[name]}
    </svg>
  )
}

export function HomeJourneySteps({
  compact,
  mascotPose,
  subtitle,
}: {
  compact: boolean
  mascotPose?: MascotPose
  subtitle?: string
}) {
  return (
    <section className="mt-5 sm:mt-6" aria-labelledby="home-journey-heading" role="region">
      <Card className="home-journey-card space-y-3 p-3.5 sm:space-y-4 sm:p-4">
        <div className={`home-journey-header${mascotPose ? ' home-journey-header--with-mascot' : ''}`}>
          <div className="min-w-0 flex-1">
            <h2 id="home-journey-heading" className="text-base font-extrabold text-[#12304A] sm:text-lg">Tu camino con NextRole</h2>
            {subtitle && <p className="mt-0.5 text-xs leading-relaxed text-neutral-600 sm:text-sm">{subtitle}</p>}
          </div>
          {mascotPose && (
            <div className="home-journey-mascot shrink-0" data-testid="home-journey-mascot" data-pose={mascotPose}>
              <Mascot className="h-24 w-24 sm:h-28 sm:w-28" pose={mascotPose} />
            </div>
          )}
        </div>
        <div className="grid grid-cols-2 gap-2.5 sm:gap-3">
          {NEXTROLE_STEPS.map((step) => (
            <article key={step.number} className={`home-step-card home-step-card--${step.tone}${compact ? ' home-step-card--compact' : ''}`}>
              <div className="flex items-start justify-between gap-2">
                <span className="home-step-icon" aria-hidden="true"><NextRoleStepIcon name={step.icon} /></span>
                <span className="home-step-number">{step.number}</span>
              </div>
              <h3>{step.title}</h3>
              {!compact && <p>{step.homeDescription}</p>}
            </article>
          ))}
        </div>
      </Card>
    </section>
  )
}

export function IntroductionSteps() {
  return (
    <ol className="assessment-intro-steps">
      {NEXTROLE_STEPS.map((step) => (
        <li key={step.number} className={`assessment-intro-step assessment-intro-step--${step.tone}`}>
          <div className="assessment-intro-step__topline">
            <span className="assessment-intro-step__icon" aria-hidden="true"><NextRoleStepIcon name={step.icon} /></span>
            <span className="assessment-intro-step__number">{step.number}</span>
          </div>
          <h3>{step.title}</h3>
          <p>{step.introDescription}</p>
        </li>
      ))}
    </ol>
  )
}
