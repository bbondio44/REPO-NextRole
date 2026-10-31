import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Mascot } from '@/components/ui/Mascot'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { getSelectedRoleId, getSkillCatalog, getSkillLevels } from '@/services/api'

const TODAY = new Date().toLocaleDateString('es-AR', { day: 'numeric', month: 'short' })

export function HomePage() {
  const { account } = useSession()

  const { data: catalog } = useQuery({ queryKey: ['skill-catalog'], queryFn: getSkillCatalog })
  const { data: levels } = useQuery({ queryKey: ['skill-levels'], queryFn: getSkillLevels })
  const { data: selectedRoleId } = useQuery({ queryKey: ['selected-role'], queryFn: getSelectedRoleId })

  const totalSkills = catalog?.skills.length ?? 0
  const evaluatedSkills = Object.keys(levels ?? {}).length
  const assessmentDone = totalSkills > 0 && evaluatedSkills >= totalSkills
  const roleSelected = Boolean(selectedRoleId)

  const steps = [
    { label: 'Completar perfil', done: true },
    { label: 'Autoevaluación de habilidades', done: assessmentDone },
    { label: 'Selección de rol objetivo', done: roleSelected },
    { label: 'Diagnóstico de brechas', done: roleSelected },
    { label: 'Formación recomendada', done: false },
  ]
  const currentStepIndex = steps.findIndex((s) => !s.done)
  const stepNumber = currentStepIndex === -1 ? steps.length : currentStepIndex + 1
  const overallPct = Math.round(((stepNumber - 1) / steps.length) * 100)

  let suggestion: { title: string; description: string; to: string; cta: string }
  if (!assessmentDone) {
    suggestion = {
      title: '¡Listo para tu próximo paso!',
      description: 'Completá tu autoevaluación guiada y descubrí las mejores opciones para tu perfil profesional.',
      to: '/habilidades',
      cta: 'Continuar con mi autoevaluación →',
    }
  } else if (!roleSelected) {
    suggestion = {
      title: 'Tu diagnóstico está listo',
      description: 'Ya evaluamos tus competencias. Mirá los roles IT con mayor afinidad a tu perfil.',
      to: '/roles',
      cta: 'Ver roles sugeridos →',
    }
  } else {
    suggestion = {
      title: 'Seguí preparándote',
      description: 'Revisá tus brechas de competencias y la formación recomendada para tu rol elegido.',
      to: `/roles/${selectedRoleId}/brechas`,
      cta: 'Ver mi diagnóstico de brechas →',
    }
  }

  return (
    <AppShell title="Inicio" subtitle={account?.firstName ? `Hola, ${account.firstName}` : undefined}>
      <div className="flex items-center justify-between">
        <Badge tone="primary">En progreso</Badge>
        <span className="text-xs text-neutral-400">{TODAY}</span>
      </div>

      <h1 className="mt-3 text-xl font-extrabold text-neutral-950">
        ¡Hola, {account?.firstName || 'Estudiante'}! 👋
      </h1>
      <p className="mt-1 text-sm text-neutral-500">Estás a solo unos pasos de descubrir tu ruta laboral en tecnología.</p>

      <Card className="mt-5 flex flex-col items-center gap-3 bg-gradient-to-b from-primary-50 to-white text-center">
        <Mascot className="h-24 w-24" />
        <Badge tone="success">Paso sugerido</Badge>
        <h2 className="text-base font-bold text-neutral-950">{suggestion.title}</h2>
        <p className="text-sm text-neutral-500">{suggestion.description}</p>
      </Card>

      <Card className="mt-4 space-y-2">
        <div className="flex items-center justify-between text-sm">
          <span className="font-semibold text-neutral-800">Ruta de orientación</span>
          <span className="text-xs text-neutral-400">Paso {stepNumber} de {steps.length}</span>
        </div>
        <ProgressBar value={overallPct} />
        <div className="flex items-center justify-between text-xs text-neutral-400">
          <span>Siguiente: {steps[currentStepIndex === -1 ? steps.length - 1 : currentStepIndex].label}</span>
          <span>{overallPct}% completado</span>
        </div>
      </Card>

      <Link to={suggestion.to} className="mt-5 block">
        <Button fullWidth>{suggestion.cta}</Button>
      </Link>
    </AppShell>
  )
}
