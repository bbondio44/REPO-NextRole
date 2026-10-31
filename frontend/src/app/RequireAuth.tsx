import { useQuery } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { getProfile } from '@/services/api'

/** Protege rutas que requieren una cuenta creada (simulada, vía localStorage). */
export function RequireAuth({ children }: { children: ReactNode }) {
  const { account, isLoading } = useSession()
  if (isLoading) return null
  if (!account) return <Navigate to="/login" replace />
  return <>{children}</>
}

/** Protege rutas que además necesitan el perfil académico ya completado. */
export function RequireProfile({ children }: { children: ReactNode }) {
  const { data: profile, isLoading } = useQuery({ queryKey: ['profile'], queryFn: getProfile })
  if (isLoading) return null
  if (!profile) return <Navigate to="/onboarding/perfil" replace />
  return <>{children}</>
}
