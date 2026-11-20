import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'

import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Mascot } from '@/components/ui/Mascot'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { TopBar } from '@/components/layout/TopBar'
import { getRoleById, getRoleMatching, getSkillGap, selectRole } from '@/services/api'

const STATUS_LABEL: Record<string, string> = {
  cubierta: 'Coincide',
  parcial: 'Parcial',
  faltante: 'Falta',
  no_evaluada: 'Sin evaluar',
}

export function RoleDetailPage() {
  const { roleId } = useParams<{ roleId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: role } = useQuery({ queryKey: ['role', roleId], queryFn: () => getRoleById(roleId!), enabled: !!roleId })
  const { data: matching } = useQuery({ queryKey: ['role-matching'], queryFn: getRoleMatching })
  const { data: gap } = useQuery({ queryKey: ['skill-gap', roleId], queryFn: () => getSkillGap(roleId!), enabled: !!roleId })

  if (!role || !roleId) return null

  const affinity = matching?.find((m) => m.role.id === roleId)?.affinity ?? 0

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
          <p className="text-xs font-semibold uppercase tracking-wide text-primary-500">Área de {role.area}</p>
          <h1 className="mt-1 text-lg font-extrabold text-neutral-950">{role.name}</h1>
          <p className="mt-1 text-sm text-neutral-500">{role.description}</p>

          <div className="mt-4 flex items-center gap-3 rounded-xl bg-primary-50 p-3">
            <span className="text-sm font-bold text-primary-700">{affinity}% afinidad con tu perfil</span>
            <span className="ml-auto text-xs font-semibold text-emerald-600">
              {affinity >= 80 ? 'Excelente' : affinity >= 50 ? 'Buena' : 'Inicial'}
            </span>
          </div>
          <div className="mt-2">
            <ProgressBar value={affinity} />
          </div>
        </Card>

        <h2 className="mt-5 text-sm font-bold text-neutral-800">Responsabilidades habituales</h2>
        <ul className="mt-2 space-y-1.5 text-sm text-neutral-600">
          {role.responsibilities.map((r) => (
            <li key={r} className="flex gap-2">
              <span className="text-primary-500">•</span>
              {r}
            </li>
          ))}
        </ul>

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

        <Card className="mt-5 flex items-center gap-3 bg-primary-50">
          <Mascot className="h-10 w-10 shrink-0" pose="thinking" />
          <div>
            <p className="text-xs font-bold uppercase tracking-wide text-primary-600">Tip motivacional</p>
            <p className="text-sm text-neutral-600">
              {affinity >= 70
                ? '¡Estás muy cerca! Cumplís con la mayoría de las habilidades base para postularte.'
                : 'Todavía tenés margen de mejora — mirá qué brechas priorizar en el próximo paso.'}
            </p>
          </div>
        </Card>

        <Button fullWidth className="mt-5" onClick={handleContinue}>
          Ver diferencias con mi perfil →
        </Button>
      </main>
    </div>
  )
}
