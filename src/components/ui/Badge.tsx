import type { ReactNode } from 'react'
import clsx from 'clsx'

type Tone = 'primary' | 'success' | 'warning' | 'danger' | 'neutral'

const toneClasses: Record<Tone, string> = {
  primary: 'bg-primary-50 text-primary-700',
  success: 'bg-emerald-50 text-emerald-700',
  warning: 'bg-amber-50 text-amber-700',
  danger: 'bg-red-50 text-red-600',
  neutral: 'bg-neutral-100 text-neutral-600',
}

export function Badge({ tone = 'neutral', children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span className={clsx('inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold', toneClasses[tone])}>
      {children}
    </span>
  )
}
