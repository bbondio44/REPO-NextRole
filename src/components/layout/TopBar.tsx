import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

import { LogoMark } from '@/components/ui/Logo'

interface TopBarProps {
  title?: string
  subtitle?: string
  showBack?: boolean
  right?: ReactNode
}

export function TopBar({ title, subtitle, showBack, right }: TopBarProps) {
  const navigate = useNavigate()

  return (
    <header className="sticky top-0 z-10 border-b border-neutral-100 bg-white/95 px-4 py-3 backdrop-blur">
      <div className="mx-auto flex max-w-md items-center justify-between">
        <div className="flex items-center gap-2.5">
          {showBack ? (
            <button
              onClick={() => navigate(-1)}
              aria-label="Volver"
              className="flex h-8 w-8 items-center justify-center rounded-full text-neutral-500 hover:bg-neutral-100"
            >
              <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth={2}>
                <path d="M15 19l-7-7 7-7" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          ) : (
            <LogoMark />
          )}
          <div>
            <p className="text-sm font-bold leading-none text-neutral-950">{title ?? 'NextRole'}</p>
            {subtitle && <p className="mt-0.5 text-xs text-neutral-400">{subtitle}</p>}
          </div>
        </div>
        {right}
      </div>
    </header>
  )
}
