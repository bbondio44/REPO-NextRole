import { AppShell } from '@/components/layout/AppShell'
import { AssessmentAccessPanel } from '@/features/assessment/AssessmentAccessPanel'

export function SkillsIndexPage() {
  return (
    <AppShell title="Habilidades" subtitle="Tu autoevaluación técnica">
      <AssessmentAccessPanel />
    </AppShell>
  )
}
