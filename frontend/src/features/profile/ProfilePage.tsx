import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Mascot } from '@/components/ui/Mascot'
import { getProfile } from '@/services/api'

export function ProfilePage() {
  const { account, logout } = useSession()
  const navigate = useNavigate()
  const { data: profile } = useQuery({ queryKey: ['profile'], queryFn: getProfile })

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <AppShell title="Perfil">
      <div className="flex flex-col items-center text-center">
        <Mascot className="h-20 w-20" pose="cool" />
        <h1 className="mt-2 text-lg font-extrabold text-neutral-950">
          {account?.firstName} {account?.lastName}
        </h1>
        <p className="text-sm text-neutral-400">{account?.email}</p>
      </div>

      <Card className="mt-5 space-y-3">
        <h2 className="text-sm font-bold text-neutral-800">Perfil académico</h2>
        <Field label="Carrera o estudios" value={profile?.career} />
        <Field label="Estado actual" value={profile?.stage} />
        <Field label="Objetivo profesional" value={profile?.objective} />
      </Card>

      <div className="mt-4 space-y-2">
        <Button fullWidth variant="secondary" onClick={() => navigate('/onboarding/perfil')}>
          Editar perfil
        </Button>
        <Button fullWidth variant="ghost" onClick={handleLogout}>
          Cerrar sesión
        </Button>
      </div>
    </AppShell>
  )
}

function Field({ label, value }: { label: string; value?: string }) {
  return (
    <div className="flex items-center justify-between gap-3 border-b border-neutral-50 pb-2 text-sm last:border-0 last:pb-0">
      <span className="text-neutral-400">{label}</span>
      <span className="font-medium text-neutral-800">{value ?? '—'}</span>
    </div>
  )
}
