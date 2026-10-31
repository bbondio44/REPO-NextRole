import clsx from 'clsx'

export function LogoMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 48 48" fill="none" className={clsx('h-8 w-8', className)} aria-hidden>
      <path
        d="M24 4C15 4 8 11 8 20c0 6 3 10 7 13v9l9-8c13-1 16-11 16-14 0-9-7-16-16-16Z"
        className="fill-primary-500"
      />
      <path d="M18 23l6 6 10-12" stroke="white" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round" fill="none" />
    </svg>
  )
}

export function LogoWithName({ dark }: { dark?: boolean }) {
  return (
    <div className="flex items-center gap-2">
      <LogoMark />
      <span className={clsx('text-lg font-extrabold', dark ? 'text-white' : 'text-neutral-950')}>NextRole</span>
    </div>
  )
}
