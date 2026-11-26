import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'

interface EmptyStateCardProps {
  icon: ReactNode
  title: string
  description: string
  actionLabel: string
  to: string
  secondary?: ReactNode
}

export function EmptyStateCard({ icon, title, description, actionLabel, to, secondary }: EmptyStateCardProps) {
  return (
    <Card className="mt-4 border-sky-100 bg-gradient-to-br from-sky-50 to-white text-center">
      <span className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-sky-100 text-primary-700" aria-hidden="true">
        {icon}
      </span>
      <h2 className="mt-3 text-lg font-extrabold text-[#12304A]">{title}</h2>
      <p className="mx-auto mt-1 max-w-sm text-sm leading-relaxed text-neutral-600">{description}</p>
      <Link to={to} className="mt-4 block"><Button fullWidth>{actionLabel}</Button></Link>
      {secondary && <div className="mt-3">{secondary}</div>}
    </Card>
  )
}
