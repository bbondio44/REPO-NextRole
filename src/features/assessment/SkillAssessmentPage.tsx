import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { LevelPicker } from '@/components/ui/LevelPicker'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { getSkillCatalog, getSkillLevels, setSkillLevel } from '@/services/api'
import type { KnowledgeLevel } from '@/types/domain'

export function SkillAssessmentPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [activeCategory, setActiveCategory] = useState<string>('all')
  const [pending, setPending] = useState(false)

  const { data: catalog } = useQuery({ queryKey: ['skill-catalog'], queryFn: getSkillCatalog })
  const { data: levels } = useQuery({ queryKey: ['skill-levels'], queryFn: getSkillLevels })

  const totalSkills = catalog?.skills.length ?? 0
  const evaluatedCount = Object.keys(levels ?? {}).length
  const pct = totalSkills ? Math.round((evaluatedCount / totalSkills) * 100) : 0

  const visibleSkills = useMemo(() => {
    if (!catalog) return []
    if (activeCategory === 'all') return catalog.skills
    return catalog.skills.filter((s) => s.categoryId === activeCategory)
  }, [catalog, activeCategory])

  async function handleSelect(skillId: string, level: KnowledgeLevel) {
    setPending(true)
    await setSkillLevel(skillId, level)
    await queryClient.invalidateQueries({ queryKey: ['skill-levels'] })
    setPending(false)
  }

  return (
    <AppShell title="Habilidades" subtitle="Autoevaluación de habilidades" showBack>
      <div className="flex items-center justify-between text-xs font-semibold text-primary-600">
        <span>ETAPA 3 DE 6</span>
        <span className="text-neutral-400">
          {evaluatedCount} de {totalSkills} evaluadas ({pct}%)
        </span>
      </div>
      <div className="mt-2">
        <ProgressBar value={pct} />
      </div>
      <div className="mt-2">
        <Badge tone="success">Perfil base listo</Badge>
      </div>

      <h1 className="mt-4 text-lg font-extrabold text-neutral-950">Autoevaluación de habilidades</h1>
      <p className="mt-1 text-sm text-neutral-500">
        Calificá tu nivel con honestidad para detectar brechas reales. Si no evaluás una competencia, no la vamos a
        contar como "sin conocimientos" — simplemente la dejamos pendiente.
      </p>

      <div className="mt-4 flex gap-2 overflow-x-auto pb-1">
        <CategoryChip label="Todas" active={activeCategory === 'all'} onClick={() => setActiveCategory('all')} />
        {catalog?.categories.map((cat) => (
          <CategoryChip
            key={cat.id}
            label={cat.name}
            active={activeCategory === cat.id}
            onClick={() => setActiveCategory(cat.id)}
          />
        ))}
      </div>

      <div className="mt-4 space-y-3">
        {visibleSkills.map((skill) => {
          const current = levels?.[skill.id] ?? null
          return (
            <Card key={skill.id}>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <p className="text-sm font-semibold text-neutral-900">{skill.name}</p>
                  <p className="text-xs text-neutral-400">{skill.description}</p>
                </div>
                {current !== null && <Badge tone="primary">Evaluado</Badge>}
              </div>
              <div className="mt-3">
                <LevelPicker value={current} onChange={(level) => handleSelect(skill.id, level)} />
              </div>
            </Card>
          )
        })}
      </div>

      <div className="mt-6">
        <Button fullWidth disabled={pending || evaluatedCount === 0} onClick={() => navigate('/habilidades/resumen')}>
          Ver mi diagnóstico →
        </Button>
      </div>
    </AppShell>
  )
}

function CategoryChip({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`shrink-0 rounded-full border px-3.5 py-1.5 text-xs font-semibold transition-colors ${
        active ? 'border-primary-500 bg-primary-500 text-white' : 'border-neutral-200 bg-white text-neutral-500'
      }`}
    >
      {label}
    </button>
  )
}
