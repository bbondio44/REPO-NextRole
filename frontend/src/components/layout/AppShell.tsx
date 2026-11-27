import type { ReactNode } from 'react'

import { BottomNav } from '@/components/layout/BottomNav'
import { TopBar } from '@/components/layout/TopBar'

interface AppShellProps {
  children: ReactNode
  title?: string
  subtitle?: string
  showBack?: boolean
  right?: ReactNode
  compactBottomNavSpacing?: boolean
}

export function AppShell({ children, title, subtitle, showBack, right, compactBottomNavSpacing = false }: AppShellProps) {
  return (
    <div className="min-h-dvh bg-app-background">
      <TopBar title={title} subtitle={subtitle} showBack={showBack} right={right} />
      <main className={`mx-auto max-w-md px-4 ${compactBottomNavSpacing ? 'pb-[calc(4.5rem_+_env(safe-area-inset-bottom))]' : 'pb-[calc(6rem_+_env(safe-area-inset-bottom))]'} pt-4`}>
        {children}
      </main>
      <BottomNav />
    </div>
  )
}
