import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useMemo, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Mascot } from '@/components/ui/Mascot'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { getRoleVisual } from '@/features/roles/roleVisuals'
import { generateDiagnosis, getAssessmentAttempt, getDiagnosis, getRoleCatalogEntry, retryDiagnosis } from '@/services/api'
import type { ExplainabilityDiagnosisResponse } from '@/types/domain'

const POLL_INTERVAL_MS = 2_000
const VISUAL_WAIT_LIMIT_MS = 120_000
const DATE_FORMAT = new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' })

function diagnosisKey(attemptId: string | null, roleId: string | undefined) {
  return ['explainability-diagnosis', attemptId, roleId] as const
}

function PlanCard() {
  return (
    <section aria-labelledby="training-plan-title" className="rounded-2xl border border-neutral-100 bg-white p-4 shadow-sm shadow-neutral-900/5">
      <div className="flex items-center justify-between gap-3">
        <h2 id="training-plan-title" className="text-lg font-bold text-neutral-950">Tu plan de formación</h2>
        <span className="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-700">Próximamente</span>
      </div>
    </section>
  )
}

function DiagnosisSections({ response }: { response: ExplainabilityDiagnosisResponse }) {
  const diagnosis = response.diagnosis
  if (!diagnosis) return null

  const sections = [
    ['Tu situación actual', diagnosis.currentSituation],
    ['En qué deberías enfocarte', diagnosis.recommendedFocus],
    ['Por qué es importante', diagnosis.importanceExplanation],
    ['Cómo te acerca al mundo laboral', diagnosis.professionalRelevance],
  ] as const

  return (
    <div className="space-y-0 rounded-xl bg-white p-4 text-neutral-800 sm:p-5">
      {sections.map(([title, text], index) => (
        <section key={title} aria-labelledby={`diagnosis-section-${index}`} className={index ? 'border-t border-neutral-100 pt-4 mt-4' : ''}>
          <h3 id={`diagnosis-section-${index}`} className="text-base font-bold" style={{ color: 'var(--role-diagnosis-color)' }}>{title}</h3>
          <p className="mt-2 whitespace-pre-wrap text-sm leading-[1.65] text-neutral-800">{text}</p>
        </section>
      ))}
    </div>
  )
}

export function RoleFormationPage() {
  const { roleId } = useParams()
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const queryClient = useQueryClient()
  const visual = roleId ? getRoleVisual(roleId) : undefined
  const pairKey = `${attemptId ?? ''}:${roleId ?? ''}`
  const [waitLimitReachedFor, setWaitLimitReachedFor] = useState<string | null>(null)
  const [initialRequestFailedFor, setInitialRequestFailedFor] = useState<string | null>(null)
  const waitLimitReached = waitLimitReachedFor === pairKey
  const initialRequestFailed = initialRequestFailedFor === pairKey

  const roleQuery = useQuery({
    queryKey: ['role-catalog-entry', roleId],
    queryFn: () => getRoleCatalogEntry(roleId!),
    enabled: Boolean(roleId && visual),
    retry: false,
    refetchOnWindowFocus: false,
  })
  const attemptQuery = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
    retry: false,
    refetchOnWindowFocus: false,
  })
  const attemptIsValid = attemptQuery.data?.status === 'COMPLETED'
    && attemptQuery.data.results?.some((result) => result.roleId === roleId) === true
  const statusKey = useMemo(() => diagnosisKey(attemptId, roleId), [attemptId, roleId])

  const generateMutation = useMutation({
    mutationKey: ['explainability-generate', attemptId, roleId],
    mutationFn: () => generateDiagnosis(attemptId!, roleId!),
    retry: false,
    onSuccess: (result) => { setInitialRequestFailedFor(null); queryClient.setQueryData(statusKey, result) },
    onError: () => { setInitialRequestFailedFor(pairKey); void queryClient.invalidateQueries({ queryKey: statusKey }) },
  })
  const retryMutation = useMutation({
    mutationKey: ['explainability-retry', attemptId, roleId],
    mutationFn: () => retryDiagnosis(attemptId!, roleId!),
    retry: false,
    onSuccess: (result) => queryClient.setQueryData(statusKey, result),
    onError: () => { void queryClient.invalidateQueries({ queryKey: statusKey }) },
  })

  const diagnosisQuery = useQuery({
    queryKey: statusKey,
    queryFn: () => getDiagnosis(attemptId!, roleId!),
    enabled: Boolean(attemptId && roleId && visual && attemptIsValid),
    retry: false,
    refetchOnWindowFocus: false,
    refetchInterval: (query) => {
      const backendGenerating = query.state.data?.status === 'GENERATING'
      const requestInFlight = generateMutation.isPending || retryMutation.isPending
      return (backendGenerating || requestInFlight) && !waitLimitReached ? POLL_INTERVAL_MS : false
    },
  })

  useEffect(() => {
    if (!attemptId || !roleId || !visual || !attemptIsValid || diagnosisQuery.data?.status !== 'NOT_STARTED') return
    const guardKey = ['explainability-initial-generation-started', attemptId, roleId] as const
    if (queryClient.getQueryData(guardKey)) return
    queryClient.setQueryDefaults(guardKey, { gcTime: Number.POSITIVE_INFINITY })
    queryClient.setQueryData(guardKey, true)
    generateMutation.mutate()
  }, [attemptId, roleId, visual, attemptIsValid, diagnosisQuery.data?.status, queryClient, generateMutation])

  const backendGenerating = diagnosisQuery.data?.status === 'GENERATING'
  const requestInFlight = generateMutation.isPending || retryMutation.isPending
  const isGenerating = backendGenerating || requestInFlight

  useEffect(() => {
    if (!isGenerating || waitLimitReached) return
    const timer = window.setTimeout(() => setWaitLimitReachedFor(pairKey), VISUAL_WAIT_LIMIT_MS)
    return () => window.clearTimeout(timer)
  }, [isGenerating, waitLimitReached, pairKey])

  const diagnosisResponse = diagnosisQuery.data
  const hasFailed = diagnosisResponse?.status === 'FAILED' || initialRequestFailed || generateMutation.isError || retryMutation.isError
  const roleName = (diagnosisResponse?.status === 'SUCCEEDED' ? diagnosisResponse.roleName : null) ?? roleQuery.data?.name
  const roleDescription = (diagnosisResponse?.status === 'SUCCEEDED' ? diagnosisResponse.roleDescription : null) ?? roleQuery.data?.description
  const score = attemptQuery.data?.results?.find((result) => result.roleId === roleId)?.displayPercentage

  if (!roleId || !visual) {
    return <AppShell title="Formación" showBack><p role="alert">No encontramos el rol solicitado.</p><PlanCard /></AppShell>
  }

  const backTo = `/roles?attemptId=${encodeURIComponent(attemptId ?? '')}`

  return (
    <AppShell title="Formación" subtitle={roleName ?? undefined} showBack backTo={backTo}>
      <div className="space-y-4" style={{ '--role-diagnosis-color': visual.diagnosisColor } as React.CSSProperties}>
        <section aria-labelledby="role-heading" className="rounded-2xl p-4 sm:p-5" style={{ backgroundColor: visual.backgroundColor }}>
          <div className="flex items-start gap-4">
            <img src={visual.emblem} alt={visual.emblemAlt} className="h-20 w-20 shrink-0 object-contain" />
            <div className="min-w-0 flex-1">
              <h1 id="role-heading" className="text-[22px] font-extrabold leading-tight text-neutral-950">{roleName ?? (roleQuery.isLoading ? 'Cargando rol…' : visual.label)}</h1>
              <p className="mt-1 text-[13px] font-medium text-neutral-700">Junior</p>
              {score !== undefined ? (
                <div className="mt-3">
                  <p className="text-[30px] font-extrabold leading-none tabular-nums text-primary-700">{score}%</p>
                  <div className="mt-2" role="progressbar" aria-label="Porcentaje de preparación" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Number(score)}>
                    <ProgressBar value={Number(score)} colorClassName="bg-primary-500" />
                  </div>
                </div>
              ) : <p className="mt-3 text-sm text-neutral-600">{attemptQuery.isLoading ? 'Cargando resultados…' : 'No hay resultados para este rol en la evaluación.'}</p>}
            </div>
          </div>
          {roleDescription && <p className="mt-4 text-[13px] leading-relaxed text-neutral-800">{roleDescription}</p>}
          {attemptQuery.data && <p className="mt-3 text-xs text-neutral-600">Evaluación realizada el {DATE_FORMAT.format(new Date(attemptQuery.data.completedAt ?? attemptQuery.data.createdAt))}</p>}
          {attemptQuery.data && <p className="mt-1 text-xs text-neutral-600">Cuestionario {attemptQuery.data.questionnaireVersion} · Matriz {attemptQuery.data.matrixVersion}</p>}
          <p className="mt-1 text-xs text-neutral-600">Autoevaluación declarada; no es una certificación.</p>
          {attemptQuery.data && attemptQuery.data.status !== 'COMPLETED' && <p role="alert" className="mt-3 text-sm font-semibold text-red-800">La evaluación seleccionada no está completa.</p>}
          {attemptQuery.data?.status === 'COMPLETED' && !attemptQuery.data.results?.some((result) => result.roleId === roleId) && <p role="alert" className="mt-3 text-sm font-semibold text-red-800">No hay resultados guardados para este rol en la evaluación seleccionada.</p>}
          {attemptQuery.isError && <p role="alert" className="mt-3 text-sm text-red-800">No pudimos cargar esta evaluación.</p>}
          {!attemptId && <p role="alert" className="mt-3 text-sm text-red-800">No se indicó qué evaluación querés consultar.</p>}
          {roleQuery.isError && <p role="alert" className="mt-3 text-sm text-red-800">No pudimos cargar la descripción del rol.</p>}
        </section>

        <section aria-labelledby="diagnosis-title" className="rounded-2xl p-4 sm:p-5" style={{ backgroundColor: visual.diagnosisColor }}>
          <div className="mb-3 flex items-center justify-between gap-2">
            <h2 id="diagnosis-title" className="text-[22px] font-extrabold text-white">Tu diagnóstico.</h2>
            <Mascot pose={diagnosisResponse?.status === 'SUCCEEDED' ? 'pointing' : 'thinking'} className={`h-14 w-14 shrink-0 ${isGenerating ? 'motion-safe:animate-pulse motion-reduce:animate-none' : ''}`} />
          </div>

          {diagnosisResponse?.status === 'SUCCEEDED' && <DiagnosisSections response={diagnosisResponse} />}

          {isGenerating && diagnosisResponse?.status !== 'SUCCEEDED' && (
            <div className="rounded-xl bg-white p-5 text-center" role="status" aria-live="polite">
              <p className="font-bold text-neutral-950">{waitLimitReached ? 'Tu diagnóstico continúa en preparación' : 'Estamos preparando tu diagnóstico'}</p>
              <p className="mt-2 text-sm leading-relaxed text-neutral-700">Analizamos cómo tus habilidades se relacionan con este rol profesional.</p>
            </div>
          )}

          {hasFailed && diagnosisResponse?.status !== 'SUCCEEDED' && !isGenerating && (
            <div className="rounded-xl bg-white p-5 text-center" role="alert">
              <p className="font-bold text-neutral-950">No pudimos preparar tu diagnóstico</p>
              <p className="mt-2 text-sm text-neutral-700">Tus resultados están guardados. Podés volver a intentarlo.</p>
              <button
                type="button"
                className="mt-4 rounded-xl bg-primary-600 px-5 py-3 text-sm font-semibold text-white hover:bg-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-700 disabled:cursor-not-allowed disabled:opacity-60 motion-reduce:transition-none"
                disabled={retryMutation.isPending || generateMutation.isPending || (diagnosisResponse?.status === 'FAILED' && diagnosisResponse.canRetry === false)}
                onClick={() => {
                  setWaitLimitReachedFor(null)
                  setInitialRequestFailedFor(null)
                  if (diagnosisResponse?.status === 'FAILED') retryMutation.mutate()
                  else generateMutation.mutate()
                }}
              >
                Volver a intentar
              </button>
            </div>
          )}

          {diagnosisQuery.isLoading && !isGenerating && <div className="rounded-xl bg-white p-5 text-sm text-neutral-600" role="status">Cargando diagnóstico…</div>}
          {diagnosisQuery.isError && !hasFailed && <div className="rounded-xl bg-white p-5 text-sm text-red-800" role="alert">No pudimos consultar el estado del diagnóstico. Volvé a intentarlo más tarde.</div>}
          {diagnosisResponse?.status === 'NOT_STARTED' && !generateMutation.isPending && !initialRequestFailed && <div className="rounded-xl bg-white p-5 text-sm text-neutral-600" role="status">Preparando la solicitud…</div>}
        </section>

        <PlanCard />
      </div>
    </AppShell>
  )
}
