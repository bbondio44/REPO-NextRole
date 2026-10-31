import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Card } from '@/components/ui/Card'
import { getRoleById, getRecommendedResources, getSkillGap } from '@/services/api'

export function LearningPage() {
  const { roleId } = useParams<{ roleId: string }>()
  const { data: role } = useQuery({ queryKey: ['role', roleId], queryFn: () => getRoleById(roleId!), enabled: !!roleId })
  const { data: gap } = useQuery({ queryKey: ['skill-gap', roleId], queryFn: () => getSkillGap(roleId!), enabled: !!roleId })
  const { data: resources } = useQuery({
    queryKey: ['resources', roleId],
    queryFn: () => getRecommendedResources(roleId!),
    enabled: !!roleId,
  })

  if (!roleId) return null

  const pendingSkills = gap?.filter((g) => g.status === 'faltante' || g.status === 'parcial') ?? []
  const grouped = pendingSkills.map((gapItem) => ({
    skill: gapItem.skill,
    priority: gapItem.priority,
    resources: resources?.filter((r) => r.skillId === gapItem.skill.id) ?? [],
  }))

  return (
    <AppShell title="Formación" subtitle={role?.name} showBack>
      <Badge tone="success">Ruta IT · Etapa 6 completada</Badge>

      <h1 className="mt-3 text-lg font-extrabold text-neutral-950">Cursos recomendados</h1>
      <p className="mt-1 text-sm text-neutral-500">
        Para cerrar tus {pendingSkills.length} brecha{pendingSkills.length === 1 ? '' : 's'} detectada
        {pendingSkills.length === 1 ? '' : 's'}.
      </p>

      <div className="mt-4 space-y-4">
        {grouped.map(({ skill, priority, resources: skillResources }) => (
          <div key={skill.id}>
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-bold text-neutral-800">{skill.name}</h2>
              <Badge tone={priority === 'alta' ? 'danger' : 'warning'}>
                Prioridad {priority === 'alta' ? 'alta' : 'media'}
              </Badge>
            </div>
            <div className="mt-2 space-y-2">
              {skillResources.length === 0 && (
                <p className="text-xs text-neutral-400">Todavía no hay recursos verificados para esta competencia.</p>
              )}
              {skillResources.map((resource) => (
                <Card key={resource.id} className="flex items-center justify-between gap-3">
                  <div>
                    <p className="flex items-center gap-2 text-xs text-neutral-400">
                      {resource.provider} · <span>{resource.isFree ? 'Gratuito' : 'Pago'}</span>
                    </p>
                    <p className="text-sm font-semibold text-neutral-900">{resource.name}</p>
                  </div>
                  <a
                    href={resource.url}
                    target="_blank"
                    rel="noreferrer"
                    className="shrink-0 rounded-lg bg-primary-500 px-3 py-2 text-xs font-semibold text-white hover:bg-primary-600"
                  >
                    Ver curso ↗
                  </a>
                </Card>
              ))}
            </div>
          </div>
        ))}

        {grouped.length === 0 && (
          <Card className="text-center text-sm text-neutral-500">
            ¡Felicitaciones! No detectamos brechas pendientes para este rol con tu evaluación actual.
          </Card>
        )}
      </div>

      <Card className="mt-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-neutral-800">¿Querés explorar más?</p>
          <p className="text-xs text-neutral-400">Consultá el catálogo completo.</p>
        </div>
        <Link to="/roles" className="shrink-0 rounded-lg border border-primary-200 px-3 py-2 text-xs font-semibold text-primary-600">
          Explorar roles →
        </Link>
      </Card>
    </AppShell>
  )
}
