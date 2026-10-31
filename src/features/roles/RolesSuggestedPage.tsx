import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getRoleMatching, getSelectedRoleId } from '@/services/api'

function affinityTone(affinity: number): 'success' | 'primary' | 'warning' {
  if (affinity >= 80) return 'success'
  if (affinity >= 50) return 'primary'
  return 'warning'
}

export function RolesSuggestedPage() {
  const { data: results } = useQuery({ queryKey: ['role-matching'], queryFn: getRoleMatching })
  const { data: selectedRoleId } = useQuery({ queryKey: ['selected-role'], queryFn: getSelectedRoleId })

  return (
    <AppShell title="Roles" subtitle="Roles IT sugeridos" showBack>
      <div className="flex items-center gap-1.5 text-xs font-semibold text-primary-600">
        <Step done />
        <Dash />
        <Step done />
        <Dash />
        <Step active label="4" />
        <Dash />
        <Step label="5" />
      </div>

      <h1 className="mt-4 text-lg font-extrabold text-neutral-950">Roles sugeridos para vos</h1>
      <p className="mt-1 text-sm text-neutral-500">Ordenados por afinidad con tus habilidades evaluadas.</p>

      <div className="mt-4 space-y-3">
        {results?.map((result) => (
          <Card key={result.role.id}>
            <div className="flex items-start justify-between gap-2">
              <div>
                <p className="text-sm font-bold text-neutral-950">{result.role.name}</p>
                <p className="text-xs text-neutral-400">{result.role.description}</p>
              </div>
              <Badge tone={affinityTone(result.affinity)}>{result.affinity}% afinidad</Badge>
            </div>
            <div className="mt-3 flex items-center justify-between">
              <span className="text-xs font-medium text-neutral-400">📈 {result.role.demandTag}</span>
              <Link to={`/roles/${result.role.id}`}>
                <Button variant="secondary">Explorar rol →</Button>
              </Link>
            </div>
          </Card>
        ))}
      </div>

      <Link to={selectedRoleId ? `/roles/${selectedRoleId}/brechas` : '#'} className="mt-6 block">
        <Button fullWidth disabled={!selectedRoleId}>
          Continuar con mi rol elegido →
        </Button>
      </Link>
      {!selectedRoleId && (
        <p className="mt-2 text-center text-xs text-neutral-400">
          Elegí "Explorar rol" en algún candidato y confirmalo para continuar.
        </p>
      )}
    </AppShell>
  )
}

function Step({ done, active, label }: { done?: boolean; active?: boolean; label?: string }) {
  if (done) {
    return (
      <span className="flex h-6 w-6 items-center justify-center rounded-full bg-emerald-500 text-white">✓</span>
    )
  }
  return (
    <span
      className={`flex h-6 w-6 items-center justify-center rounded-full text-[11px] ${
        active ? 'bg-primary-500 text-white' : 'bg-neutral-200 text-neutral-500'
      }`}
    >
      {label}
    </span>
  )
}

function Dash() {
  return <span className="h-px w-4 bg-neutral-200" />
}
