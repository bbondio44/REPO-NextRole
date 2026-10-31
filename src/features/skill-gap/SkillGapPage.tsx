import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { getRoleById, getSkillGap } from '@/services/api'
import { KNOWLEDGE_LEVEL_LABEL } from '@/types/domain'

const STATUS_STYLE: Record<string, { tone: 'success' | 'warning' | 'danger' | 'neutral'; label: string; color: string }> = {
  cubierta: { tone: 'success', label: 'Cubierta', color: 'bg-emerald-500' },
  parcial: { tone: 'warning', label: 'Parcialmente cubierta', color: 'bg-amber-500' },
  faltante: { tone: 'danger', label: 'Faltante', color: 'bg-red-500' },
  no_evaluada: { tone: 'neutral', label: 'No evaluada', color: 'bg-neutral-300' },
}

export function SkillGapPage() {
  const { roleId } = useParams<{ roleId: string }>()
  const { data: role } = useQuery({ queryKey: ['role', roleId], queryFn: () => getRoleById(roleId!), enabled: !!roleId })
  const { data: gap } = useQuery({ queryKey: ['skill-gap', roleId], queryFn: () => getSkillGap(roleId!), enabled: !!roleId })

  if (!roleId) return null

  const priorityCount = gap?.filter((g) => g.priority === 'alta').length ?? 0

  return (
    <AppShell title="Diagnóstico de Brechas" subtitle={role?.name} showBack>
      <div className="flex items-center justify-between text-xs font-semibold text-primary-600">
        <span>PASO 5 DE 6 · DIAGNÓSTICO DE CARRERA</span>
      </div>
      <div className="mt-2 flex gap-1">
        {Array.from({ length: 6 }).map((_, i) => (
          <span key={i} className={`h-1.5 flex-1 rounded-full ${i < 4 ? 'bg-emerald-500' : i === 4 ? 'bg-primary-500' : 'bg-neutral-200'}`} />
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between">
        <h1 className="text-lg font-extrabold text-neutral-950">Comparativa de brechas</h1>
        <Badge tone="danger">{priorityCount} prioritarias</Badge>
      </div>
      <p className="mt-1 text-sm text-neutral-500">Contrastamos tu nivel con los requisitos del mercado.</p>

      <div className="mt-4 space-y-3">
        {gap?.map((item) => {
          const style = STATUS_STYLE[item.status]
          const currentPct = item.currentLevel !== null ? (item.currentLevel / 4) * 100 : 0
          return (
            <Card key={item.skill.id}>
              <div className="flex items-start justify-between gap-2">
                <p className="text-sm font-semibold text-neutral-900">{item.skill.name}</p>
                <Badge tone={style.tone}>{style.label}</Badge>
              </div>
              <div className="mt-2">
                <ProgressBar value={currentPct} colorClassName={style.color} />
              </div>
              <div className="mt-1 flex justify-between text-xs text-neutral-400">
                <span>Requerido: {KNOWLEDGE_LEVEL_LABEL[item.requiredLevel]}</span>
                <span>
                  Tu nivel: {item.currentLevel !== null ? KNOWLEDGE_LEVEL_LABEL[item.currentLevel] : 'Sin evaluar'}
                </span>
              </div>
            </Card>
          )
        })}
      </div>

      <Link to={`/roles/${roleId}/formacion`} className="mt-6 block">
        <Button fullWidth>Ver cursos recomendados →</Button>
      </Link>
    </AppShell>
  )
}
