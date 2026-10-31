import clsx from 'clsx'

interface ProgressBarProps {
  value: number
  max?: number
  colorClassName?: string
  trackClassName?: string
}

export function ProgressBar({
  value,
  max = 100,
  colorClassName = 'bg-primary-500',
  trackClassName = 'bg-neutral-100',
}: ProgressBarProps) {
  const pct = Math.min(100, Math.max(0, (value / max) * 100))
  return (
    <div className={clsx('h-2 w-full overflow-hidden rounded-full', trackClassName)}>
      <div className={clsx('h-full rounded-full transition-all', colorClassName)} style={{ width: `${pct}%` }} />
    </div>
  )
}
