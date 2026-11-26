import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Link } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { ConfirmDiscardDialog } from '@/components/ui/ConfirmDiscardDialog'
import { Mascot } from '@/components/ui/Mascot'
import { AssessmentJourney } from '@/features/assessment/AssessmentJourney'
import { useDiscardAssessmentAttempt } from '@/features/assessment/useDiscardAssessmentAttempt'
import { getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import { ASSESSMENT_ROLE_NAME } from '@/types/domain'

function formatCompletionDate(value: string) {
  return new Date(value).toLocaleDateString('es-AR', { day: 'numeric', month: 'short', year: 'numeric' })
}

export function HomePage() {
  const { account } = useSession()
  const [confirmDiscard, setConfirmDiscard] = useState(false)
  const current = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt })
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })
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
        <Card className="mt-6 flex flex-col items-center gap-3 border-sky-100 bg-gradient-to-br from-sky-50 to-white py-6 text-center">
          <Mascot className="h-24 w-24" pose="wave" />
          <h2 className="text-lg font-extrabold text-[#12304A]">
            {latestCompleted ? '¡Seguí creciendo!' : '¡Descubrí tu potencial!'}
          </h2>
          <p className="max-w-sm text-sm leading-relaxed text-neutral-600">
            {latestCompleted
              ? 'Actualizá tus habilidades y descubrí cómo evoluciona tu preparación para los roles de IT.'
              : 'Conocé tus habilidades y descubrí las oportunidades profesionales que mejor se adaptan a tu perfil.'}
          </p>
        </Card>
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

function HomeGreeting({ name }: { name?: string }) {
  return (
    <div className="pt-2">
      <h1 className="text-xl font-extrabold text-neutral-950">¡Hola, {name || 'Estudiante'}! 👋</h1>
      <p className="mt-1 text-sm text-neutral-500">Tu próximo paso profesional empieza acá.</p>
    </div>
  )
}
