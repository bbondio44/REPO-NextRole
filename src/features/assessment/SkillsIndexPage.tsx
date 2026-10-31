import { useQuery } from '@tanstack/react-query'
import { Navigate } from 'react-router-dom'

import { getSkillCatalog, getSkillLevels } from '@/services/api'

/** Decide si el usuario debe ver la autoevaluación o ya puede ver el resumen. */
export function SkillsIndexPage() {
  const { data: catalog } = useQuery({ queryKey: ['skill-catalog'], queryFn: getSkillCatalog })
  const { data: levels } = useQuery({ queryKey: ['skill-levels'], queryFn: getSkillLevels })

  if (!catalog || !levels) return null

  const done = Object.keys(levels).length >= catalog.skills.length
  return <Navigate to={done ? '/habilidades/resumen' : '/habilidades/evaluacion'} replace />
}
