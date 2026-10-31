import type { HTMLAttributes } from 'react'
import clsx from 'clsx'

export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={clsx('rounded-card border border-neutral-100 bg-white p-4 shadow-sm shadow-neutral-900/5', className)}
      {...props}
    />
  )
}
