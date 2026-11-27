import { useQuery } from '@tanstack/react-query'
import { useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { ConfirmDiscardDialog } from '@/components/ui/ConfirmDiscardDialog'
import { Mascot } from '@/components/ui/Mascot'
import { AssessmentJourney } from '@/features/assessment/AssessmentJourney'
import { useDiscardAssessmentAttempt } from '@/features/assessment/useDiscardAssessmentAttempt'
import { useStartAssessmentAttempt } from '@/features/assessment/useStartAssessmentAttempt'
import { getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import { ASSESSMENT_ROLE_NAME } from '@/types/domain'
import './HomePage.css'

function formatCompletionDate(value: string) {
  return new Date(value).toLocaleDateString('es-AR', { day: 'numeric', month: 'short', year: 'numeric' })
}

export function HomePage() {
  const { account } = useSession()
  const [confirmDiscard, setConfirmDiscard] = useState(false)
  const current = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt })
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })
  const { startAssessment, starting, startError } = useStartAssessmentAttempt(Boolean(history.data?.[0]))
  const { discard, isDiscarding, discardError, clearDiscardError } = useDiscardAssessmentAttempt()

  if (current.isLoading || history.isLoading) {
    return (
      <AppShell title="Inicio">
        <HomeGreeting name={account?.firstName} />
        <p role="status" className="mt-6 text-sm text-neutral-500">Consultando tus evaluaciones…</p>
      </AppShell>
    )
  }

  if (current.isError || history.isError) {
    return (
      <AppShell title="Inicio">
        <HomeGreeting name={account?.firstName} />
        <p role="alert" className="mt-6 text-sm text-red-700">No pudimos consultar el estado de tus evaluaciones.</p>
      </AppShell>
    )
  }

  const draft = current.data?.status === 'DRAFT' ? current.data : null
  const latestCompleted = history.data?.[0]
  const topResult = latestCompleted?.results.find((result) => result.rankPosition === 1)
  const progress = draft && draft.totalQuestions > 0 ? draft.answeredCount / draft.totalQuestions : 0
  const motivation = progress <= 0
    ? latestCompleted
      ? ['¡Un nuevo comienzo!', 'Volvé a evaluar tus habilidades y descubrí cuánto avanzaste.']
      : ['¡Empezá a descubrir tu potencial!', 'Conocé tus habilidades y descubrí qué roles se adaptan a vos.']
    : progress < 0.4
      ? ['¡Cada paso cuenta!', 'Seguí descubriendo tus habilidades.']
      : progress < 0.75
        ? ['¡Vas por buen camino!', 'Seguí evaluando tus habilidades para descubrir tu perfil profesional.']
        : progress < 1
          ? ['¡Ya casi llegás!', 'Te falta poco para conocer tus resultados.']
          : ['¡Llegaste a la meta!', 'Finalizá tu autoevaluación para conocer tus resultados.']

  async function discardDraft() {
    if (!draft) return
    try {
      await discard(draft.attemptId)
      setConfirmDiscard(false)
    } catch {
      // The shared hook retains the backend error and keeps the cached draft intact.
    }
  }

  return (
    <AppShell title="Inicio">
      <HomeGreeting name={account?.firstName} />

      {draft ? (
        <Card style={{ backgroundColor: '#006B9E' }} className="home-draft-card relative mt-6 space-y-4 overflow-hidden border-0 p-5 text-white shadow-md">
          <h2 className="pr-16 text-lg font-extrabold text-white">Tu autoevaluación</h2>
          <div className="space-y-1 text-white">
            <h3 className="text-base font-bold text-white">{motivation[0]}</h3>
            <p className="max-w-md text-sm leading-relaxed text-white/90">{motivation[1]}</p>
          </div>
          <AssessmentJourney progress={progress} completed={false} />
          <Link to="/habilidades/evaluacion" className="block">
            <Button fullWidth className="!bg-white !text-[#006B9E] hover:!bg-sky-50 disabled:!bg-white disabled:!text-[#006B9E]">Continuar evaluación</Button>
          </Link>
          <button
            type="button"
            className="text-left text-xs font-medium text-white/75 underline-offset-2 hover:text-white hover:underline disabled:opacity-50"
            disabled={isDiscarding}
            onClick={() => { clearDiscardError(); setConfirmDiscard(true) }}
          >
            Descartar borrador
          </button>
        </Card>
      ) : (
        latestCompleted ? (
          <Card className="mt-6 flex flex-col items-center gap-3 border-sky-100 bg-gradient-to-br from-sky-50 to-white py-6 text-center">
            <Mascot className="h-24 w-24" pose="wave" />
            <h2 className="text-lg font-extrabold text-[#12304A]">¡Seguí creciendo!</h2>
            <p className="max-w-sm text-sm leading-relaxed text-neutral-600">
              Actualizá tus habilidades y descubrí cómo evoluciona tu preparación para los roles de IT.
            </p>
          </Card>
        ) : (
          <>
            <Card className="home-welcome-card relative mt-5 overflow-hidden border-0 px-5 py-5 text-center text-white sm:mt-6 sm:px-6 sm:py-6">
              <span className="home-welcome-orb home-welcome-orb-one" aria-hidden="true" />
              <span className="home-welcome-orb home-welcome-orb-two" aria-hidden="true" />
              <div className="relative z-10 flex flex-col items-center gap-2.5">
                <Mascot className="h-[6.5rem] w-[6.5rem] sm:h-28 sm:w-28" pose="wave" />
                <h2 className="text-xl font-extrabold tracking-tight text-white sm:text-2xl">¡Descubrí tu potencial!</h2>
                <p className="max-w-xs text-sm leading-relaxed text-sky-50 sm:text-[15px]">
                  Conocé tus habilidades y encontrá tu próximo camino profesional.
                </p>
              </div>
            </Card>

            <section className="mt-5 sm:mt-6" aria-labelledby="home-journey-heading">
              <div className="mb-3">
                <h2 id="home-journey-heading" className="text-base font-extrabold text-[#12304A] sm:text-lg">Tu camino con NextRole</h2>
                <p className="mt-0.5 text-xs text-neutral-600 sm:text-sm">Tu futuro profesional en cuatro pasos.</p>
              </div>
              <div className="grid grid-cols-2 gap-2.5 sm:gap-3">
                <JourneyStep number="01" title="Conocé tus habilidades" description="Descubrí tus fortalezas." color="blue" icon="chart" />
                <JourneyStep number="02" title="Explorá roles IT" description="Encontrá oportunidades." color="purple" icon="compass" />
                <JourneyStep number="03" title="Identificá qué mejorar" description="Conocé tus próximos desafíos." color="amber" icon="target" />
                <JourneyStep number="04" title="Seguí creciendo" description="Prepará tu formación." color="green" icon="cap" />
              </div>
            </section>

            <div className="home-start-action" aria-live="polite">
              <button
                type="button"
                className="home-start-button"
                aria-label={starting ? 'Preparando autoevaluación' : 'Comenzar autoevaluación'}
                disabled={starting}
                onClick={startAssessment}
              >
                <span className="home-play-button" aria-hidden="true">
                  <svg viewBox="0 0 24 24" className="ml-1 h-6 w-6" fill="currentColor">
                    <path d="M7.5 4.9a1.2 1.2 0 0 1 1.8-1.04l11 6.36a2.05 2.05 0 0 1 0 3.56l-11 6.36a1.2 1.2 0 0 1-1.8-1.04V4.9Z" />
                  </svg>
                </span>
                <span className="text-sm font-bold text-[#12304A]">
                  {starting ? 'Preparando autoevaluación…' : 'Comenzar autoevaluación'}
                </span>
              </button>
              {startError && <p role="alert" className="max-w-xs text-center text-sm text-red-700">{startError}</p>}
            </div>
          </>
        )
      )}

      {latestCompleted && (
        <Card className="mt-5 space-y-4">
          <div className="flex items-start gap-3">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-sky-100 text-primary-700" aria-hidden="true">
              <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="1.8">
                <path d="M4 19V5m0 14h16M8 15v-4m4 4V7m4 8V9" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </span>
            <div className="min-w-0 flex-1">
              <h2 className="text-base font-bold text-neutral-950">Tu última evaluación</h2>
              <p className="mt-1 text-xs text-neutral-500">Realizada el {formatCompletionDate(latestCompleted.completedAt)}</p>
            </div>
          </div>
          {topResult ? (
            <div className="flex items-end justify-between gap-3 rounded-xl bg-slate-50 px-4 py-3">
              <div>
                <p className="text-xs font-medium text-neutral-500">Rol con mayor preparación técnica</p>
                <p className="mt-1 font-bold text-[#12304A]">{ASSESSMENT_ROLE_NAME[topResult.roleId] ?? topResult.roleId}</p>
              </div>
              <p className="shrink-0 text-xl font-extrabold tabular-nums text-primary-700">{topResult.displayPercentage}%</p>
            </div>
          ) : (
            <p className="text-sm text-neutral-600">No hay resultados disponibles para esta evaluación.</p>
          )}
          <div className="grid gap-2 sm:grid-cols-2">
            <Link to={`/roles?attemptId=${encodeURIComponent(latestCompleted.attemptId)}`} className="block">
              <Button fullWidth>Ver resultados</Button>
            </Link>
            <Link to="/habilidades/historial" className="block">
              <Button variant="secondary" fullWidth>Ver historial de evaluaciones</Button>
            </Link>
          </div>
        </Card>
      )}
      <ConfirmDiscardDialog
        open={confirmDiscard}
        pending={isDiscarding}
        error={discardError}
        onCancel={() => setConfirmDiscard(false)}
        onConfirm={discardDraft}
      />
    </AppShell>
  )
}

type JourneyStepProps = {
  number: string
  title: string
  description: string
  color: 'blue' | 'purple' | 'amber' | 'green'
  icon: 'chart' | 'compass' | 'target' | 'cap'
}

function JourneyStep({ number, title, description, color, icon }: JourneyStepProps) {
  return (
    <article className={`home-step-card home-step-${color}`}>
      <div className="flex items-start justify-between gap-2">
        <span className="home-step-icon" aria-hidden="true">
          <StepIcon name={icon} />
        </span>
        <span className="pt-0.5 text-xs font-extrabold tracking-wide opacity-60">{number}</span>
      </div>
      <h3 className="mt-2.5 text-[13px] font-bold leading-snug sm:text-sm">{title}</h3>
      <p className="mt-1 text-[11px] leading-snug text-neutral-700/80 sm:text-xs">{description}</p>
    </article>
  )
}

function StepIcon({ name }: { name: JourneyStepProps['icon'] }) {
  const paths: Record<JourneyStepProps['icon'], ReactNode> = {
    chart: <><path d="M4 19V5m0 14h16M8 15v-4m4 4V7m4 8V9" /></>,
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

function HomeGreeting({ name }: { name?: string }) {
  return (
    <div className="pt-2">
      <h1 className="text-xl font-extrabold text-neutral-950">¡Hola, {name || 'Estudiante'}! 👋</h1>
      <p className="mt-1 text-sm text-neutral-500">Tu próximo paso profesional empieza acá.</p>
    </div>
  )
}
