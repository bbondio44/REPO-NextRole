import { useQuery } from '@tanstack/react-query'
import { Navigate } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { getOrStartAssessment } from '@/services/api'

export function SkillsIndexPage() {
  const { data: attempt, isLoading, isError } = useQuery({
    queryKey: ['assessment-attempt'],
    queryFn: getOrStartAssessment,
  })

  if (isLoading) return null
  if (isError || !attempt) {
    return <AppShell title="Autoevaluación técnica"><p role="alert">No pudimos recuperar tu evaluación.</p></AppShell>
  }
  return <Navigate to="/habilidades/evaluacion" replace />
}
