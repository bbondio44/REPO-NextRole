import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Mascot } from '@/components/ui/Mascot'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { getSkillCatalog, getSkillLevels } from '@/services/api'

export function SkillSummaryPage() {
  const { data: catalog } = useQuery({ queryKey: ['skill-catalog'], queryFn: getSkillCatalog })
  const { data: levels } = useQuery({ queryKey: ['skill-levels'], queryFn: getSkillLevels })

  if (!catalog || !levels) return null

  const totalSkills = catalog.skills.length
  const evaluatedSkills = catalog.skills.filter((s) => levels[s.id] !== undefined)
  const generalPct = evaluatedSkills.length
    ? Math.round((evaluatedSkills.reduce((acc, s) => acc + (levels[s.id] ?? 0), 0) / (evaluatedSkills.length * 4)) * 100)
    : 0

  const byCategory = catalog.categories
    .map((cat) => {
      const skillsInCat = catalog.skills.filter((s) => s.categoryId === cat.id)
      const evaluated = skillsInCat.filter((s) => levels[s.id] !== undefined)
      const pct = evaluated.length
        ? Math.round((evaluated.reduce((acc, s) => acc + (levels[s.id] ?? 0), 0) / (evaluated.length * 4)) * 100)
        : null
      return { category: cat, pct, evaluatedCount: evaluated.length, total: skillsInCat.length }
    })
    .filter((c) => c.total > 0)

  const allEvaluated = evaluatedSkills.length >= totalSkills

  return (
    <AppShell title="Habilidades" subtitle="Resumen de habilidades" showBack>
      {allEvaluated ? (
        <Badge tone="success">Etapa 3 completada</Badge>
      ) : (
        <Badge tone="warning">{evaluatedSkills.length} de {totalSkills} evaluadas</Badge>
      )}

      <Card className="mt-3 flex flex-col items-center gap-2 text-center">
        <Mascot className="h-20 w-20" />
        <p className="text-xs font-bold uppercase tracking-wide text-primary-600">¡Diagnóstico completado!</p>
        <h1 className="text-lg font-extrabold text-neutral-950">Resumen de habilidades</h1>
        <p className="text-sm text-neutral-500">
          Evaluaste {evaluatedSkills.length} de {totalSkills} competencias clave para tu perfil.
        </p>
      </Card>

      <div className="mt-4 flex items-center justify-between">
        <h2 className="text-sm font-bold text-neutral-800">Habilidades por área</h2>
        <span className="text-xs font-semibold text-primary-600">General {generalPct}%</span>
      </div>

      <div className="mt-2 space-y-3">
        {byCategory.map(({ category, pct, evaluatedCount, total }) => (
          <Card key={category.id}>
            <div className="flex items-center justify-between text-sm">
              <span className="font-semibold text-neutral-800">{category.name}</span>
              <span className="text-xs text-neutral-400">
                {pct === null ? 'Sin evaluar' : `${pct}%`}
              </span>
            </div>
            <div className="mt-2">
              <ProgressBar
                value={pct ?? 0}
                colorClassName={pct === null ? 'bg-neutral-200' : 'bg-primary-500'}
              />
            </div>
            <p className="mt-1 text-xs text-neutral-400">
              {evaluatedCount} de {total} competencias evaluadas
            </p>
          </Card>
        ))}
      </div>

      <Link to="/roles" className="mt-6 block">
        <Button fullWidth disabled={evaluatedSkills.length === 0}>
          Ver roles sugeridos →
        </Button>
      </Link>
      <p className="mt-2 text-center text-xs text-neutral-400">🔒 Tus resultados están guardados en tu perfil</p>
    </AppShell>
  )
}
