import clsx from 'clsx'

import logoUrl from '@/assets/brand/logo.png'

export function LogoMark({ className }: { className?: string }) {
  return <img src={logoUrl} alt="" className={clsx('h-8 w-8 object-contain', className)} aria-hidden />
}

export function LogoWithName({ dark }: { dark?: boolean }) {
  return (
    <div className="flex items-center gap-2">
      <LogoMark />
      <span className={clsx('text-lg font-extrabold', dark ? 'text-white' : 'text-neutral-950')}>NextRole</span>
    </div>
  )
}
