import type { ReactNode } from 'react'

import { LogoWithName } from '@/components/ui/Logo'

export function AuthShell({ children, eyebrow }: { children: ReactNode; eyebrow?: string }) {
  return (
    <div className="min-h-dvh bg-gradient-to-b from-neutral-950 via-neutral-900 to-primary-900 px-6 py-8 text-white">
      <div className="mx-auto flex max-w-md flex-col">
        <div className="flex items-center justify-between">
          <LogoWithName dark />
          {eyebrow && (
            <span className="flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-xs font-medium text-tertiary-400">
              <span className="h-1.5 w-1.5 rounded-full bg-tertiary-400" />
              {eyebrow}
            </span>
          )}
        </div>
        <div className="mt-10">{children}</div>
      </div>
    </div>
  )
}
