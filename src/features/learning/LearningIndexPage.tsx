import { useQuery } from '@tanstack/react-query'
import { Navigate, Link } from 'react-router-dom'

import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { getSelectedRoleId } from '@/services/api'

export function LearningIndexPage() {
  const { data: selectedRoleId, isLoading } = useQuery({ queryKey: ['selected-role'], queryFn: getSelectedRoleId })

  if (isLoading) return null
  if (selectedRoleId) return <Navigate to={`/roles/${selectedRoleId}/formacion`} replace />

  return (
    <AppShell title="Formación">
      <Card className="mt-4 text-center">
        <p className="text-sm font-semibold text-neutral-800">Todavía no elegiste un rol objetivo</p>
        <p className="mt-1 text-sm text-neutral-500">
          Elegí un rol para que podamos recomendarte la formación que cierra tus brechas.
        </p>
        <Link to="/roles" className="mt-4 block">
          <Button fullWidth>Ver roles sugeridos →</Button>
        </Link>
      </Card>
    </AppShell>
  )
}
