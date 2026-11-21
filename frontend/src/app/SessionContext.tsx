import { createContext, type ReactNode, useContext, useEffect, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'

import { getCurrentAccount, logout as logoutApi } from '@/services/api'
import type { Account } from '@/services/storage'

interface SessionContextValue {
  account: Account | null
  isLoading: boolean
  setAccount: (account: Account) => void
  logout: () => void
}

const SessionContext = createContext<SessionContextValue | null>(null)

export function SessionProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [account, setAccountState] = useState<Account | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    getCurrentAccount()
      .then((acc) => setAccountState(acc))
      .catch(() => setAccountState(null))
      .finally(() => setIsLoading(false))
  }, [])

  const value: SessionContextValue = {
    account,
    isLoading,
    setAccount: (acc) => {
      queryClient.clear()
      setAccountState(acc)
    },
    logout: () => {
      logoutApi()
      setAccountState(null)
      queryClient.clear()
    },
  }

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}

export function useSession() {
  const ctx = useContext(SessionContext)
  if (!ctx) throw new Error('useSession debe usarse dentro de <SessionProvider>')
  return ctx
}
