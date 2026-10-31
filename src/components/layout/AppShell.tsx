import type { ReactNode } from 'react'

import { BottomNav } from '@/components/layout/BottomNav'
import { TopBar } from '@/components/layout/TopBar'

interface AppShellProps {
  children: ReactNode
  title?: string
  subtitle?: string
  showBack?: boolean
  right?: ReactNode
}

export function AppShell({ children, title, subtitle, showBack, right }: AppShellProps) {
  return (
    <div className="min-h-dvh bg-slate-50">
      <TopBar title={title} subtitle={subtitle} showBack={showBack} right={right} />
      <main className="mx-auto max-w-md px-4 pb-24 pt-4">{children}</main>
      <BottomNav />
    </div>
  )
}
