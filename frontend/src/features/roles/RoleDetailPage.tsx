import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'

import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Mascot } from '@/components/ui/Mascot'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { TopBar } from '@/components/layout/TopBar'
import { getAssessmentAttempt, getRoleById, getRoleMatching, getSkillGap, selectRole } from '@/services/api'
import { ASSESSMENT_ROLE_NAME } from '@/types/domain'

const STATUS_LABEL: Record<string, string> = {
  cubierta: 'Coincide',
  parcial: 'Parcial',
  faltante: 'Falta',
  no_evaluada: 'Sin evaluar',
}

export function RoleDetailPage() {
  const { roleId } = useParams<{ roleId: string }>()
  const [searchParams] = useSearchParams()
  const attemptId = searchParams.get('attemptId')
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: role } = useQuery({ queryKey: ['role', roleId], queryFn: () => getRoleById(roleId!), enabled: !!roleId })
  const { data: matching } = useQuery({ queryKey: ['role-matching'], queryFn: getRoleMatching, enabled: !attemptId })
  const { data: assessment, isLoading: isAssessmentLoading, isError: isAssessmentError } = useQuery({
    queryKey: ['assessment-attempt', attemptId],
    queryFn: () => getAssessmentAttempt(attemptId!),
    enabled: Boolean(attemptId),
  })
  const { data: gap } = useQuery({ queryKey: ['skill-gap', roleId], queryFn: () => getSkillGap(roleId!), enabled: !!roleId && !!role && !attemptId })

  if (!roleId) return null
  if (attemptId && isAssessmentLoading) {
    return <div className="min-h-dvh bg-slate-50"><TopBar title="Detalle del rol" showBack /><main className="mx-auto max-w-md px-4 pt-4"><p role="status">Cargando el resultado guardado…</p></main></div>
  }
  if (attemptId && (isAssessmentError || !assessment || assessment.status !== 'COMPLETED')) {
    return <div className="min-h-dvh bg-slate-50"><TopBar title="Detalle del rol" showBack /><main className="mx-auto max-w-md px-4 pt-4"><p role="alert">No pudimos cargar el resultado guardado.</p></main></div>
  }
  const roleName = role?.name ?? ASSESSMENT_ROLE_NAME[roleId]
  if (!roleName) return null

  const persistedScore = assessment?.results?.find((result) => result.roleId === roleId)
  const affinity = persistedScore ? Number(persistedScore.displayPercentage) : attemptId
    ? null
    : matching?.find((m) => m.role.id === roleId)?.affinity ?? 0

  async function handleContinue() {
    await selectRole(roleId!)
    await queryClient.invalidateQueries({ queryKey: ['selected-role'] })
    navigate(`/roles/${roleId}/brechas`)
  }

  return (
    <div className="min-h-dvh bg-slate-50 pb-10">
      <TopBar title="Detalle del rol" showBack />
      <main className="mx-auto max-w-md px-4 pt-4">
        <Card>
          <p className="text-xs font-semibold uppercase tracking-wide text-primary-500">{role ? `Área de ${role.area}` : 'Rol IT'}</p>
          <h1 className="mt-1 text-lg font-extrabold text-neutral-950">{roleName}</h1>
          {role && <p className="mt-1 text-sm text-neutral-500">{role.description}</p>}

          <div className="mt-4 flex items-center gap-3 rounded-xl bg-primary-50 p-3">
            <span className="text-sm font-bold text-primary-700">
              {persistedScore
                ? `${persistedScore.displayPercentage}% preparación técnica`
                : attemptId ? 'Resultado no disponible' : `${affinity ?? 0}% afinidad con tu perfil`}
            </span>
            {affinity !== null && <span className="ml-auto text-xs font-semibold text-emerald-600">
              {affinity >= 80 ? 'Excelente' : affinity >= 50 ? 'Buena' : 'Inicial'}
            </span>}
          </div>
          <div className="mt-2">
            {affinity !== null && <ProgressBar value={affinity} />}
          </div>
          {persistedScore && <p className="mt-2 text-xs text-neutral-500">Puesto {persistedScore.rankPosition} · Autoevaluación declarada; no es una certificación.</p>}
          {assessment && <p className="mt-2 text-xs text-neutral-500">Evaluación del {new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium' }).format(new Date(assessment.completedAt ?? assessment.createdAt))} · Cuestionario {assessment.questionnaireVersion} · Matriz {assessment.matrixVersion}</p>}
        </Card>

        {role ? <>
          <h2 className="mt-5 text-sm font-bold text-neutral-800">Responsabilidades habituales</h2>
          <ul className="mt-2 space-y-1.5 text-sm text-neutral-600">
            {role.responsibilities.map((r) => (
              <li key={r} className="flex gap-2">
                <span className="text-primary-500">•</span>
                {r}
              </li>
            ))}
          </ul>

          {!attemptId && <>
            <div className="mt-5 flex items-center justify-between">
              <h2 className="text-sm font-bold text-neutral-800">Requisitos y habilidades requeridas</h2>
              <span className="text-xs text-neutral-400">{gap?.length ?? 0} requeridas</span>
            </div>
            <div className="mt-2 space-y-2">
              {gap?.map((item) => (
                <Card key={item.skill.id} className="flex items-center justify-between py-2.5">
                  <p className="text-sm font-medium text-neutral-800">{item.skill.name}</p>
                  <Badge tone={item.status === 'cubierta' ? 'success' : item.status === 'no_evaluada' ? 'neutral' : 'warning'}>
                    {STATUS_LABEL[item.status]}
                  </Badge>
                </Card>
              ))}
            </div>
          </>}

        {affinity !== null && <Card className="mt-5 flex items-center gap-3 bg-primary-50">
          <Mascot className="h-10 w-10 shrink-0" pose="thinking" />
          <div>
            <p className="text-xs font-bold uppercase tracking-wide text-primary-600">Tip motivacional</p>
            <p className="text-sm text-neutral-600">
              {affinity >= 70
                ? '¡Estás muy cerca! Cumplís con la mayoría de las habilidades base para postularte.'
                : 'Todavía tenés margen de mejora — mirá qué brechas priorizar en el próximo paso.'}
            </p>
          </div>
        </Card>}

        {!attemptId && <Button fullWidth className="mt-5" onClick={handleContinue}>
          Ver diferencias con mi perfil →
        </Button>}
        </> : <p className="mt-4 text-sm text-neutral-500">El detalle de responsabilidades y requisitos no está disponible para este rol en el catálogo existente.</p>}
      </main>
    </div>
  )
}
