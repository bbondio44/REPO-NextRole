import { NavLink } from 'react-router-dom'
import clsx from 'clsx'

const ICONS: Record<string, string> = {
  home: 'm3 10.5 9-7.5 9 7.5M5.5 9v10h13V9M9.5 19v-5h5v5',
  skills: 'M4 19V10m5 9V6m5 13v-6m5 6V4M2.5 19.5h19',
  roles: 'M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18Zm3.8 5.2-2.3 5.3-5.3 2.3 2.3-5.3 5.3-2.3Z',
  learning: 'm2.5 9 9.5-5 9.5 5-9.5 5-9.5-5Zm3.5 2v4c3.7 2.6 8.3 2.6 12 0v-4m3.5-2v5',
  profile: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8c.7-3.3 3.3-5 7-5s6.3 1.7 7 5',
}

const ITEMS = [
  { to: '/', label: 'Inicio', icon: 'home', end: true },
  { to: '/habilidades', label: 'Habilidades', icon: 'skills' },
  { to: '/roles', label: 'Roles', icon: 'roles' },
  { to: '/formacion', label: 'Formación', icon: 'learning' },
  { to: '/perfil', label: 'Perfil', icon: 'profile' },
]

export function BottomNav() {
  return (
    <nav aria-label="Navegación principal" className="fixed inset-x-0 bottom-0 z-20 border-t border-primary-100 bg-white/95 pb-[env(safe-area-inset-bottom)] backdrop-blur">
      <div className="mx-auto flex max-w-md items-stretch justify-between px-1.5">
        {ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className="flex min-h-[68px] min-w-0 flex-1 flex-col items-center justify-center gap-1 py-1.5 text-[10px] leading-tight sm:text-[11px]"
          >
            {({ isActive }) => (
              <>
                <span className={clsx(
                  'flex h-9 w-12 shrink-0 items-center justify-center rounded-xl transition-colors duration-200 ease-out motion-reduce:transition-none',
                  isActive ? 'bg-primary-600 text-white' : 'bg-transparent text-slate-500',
                )}>
                  <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d={ICONS[item.icon]} />
                  </svg>
                </span>
                <span className={clsx(
                  'transition-colors duration-200 ease-out motion-reduce:transition-none',
                  isActive ? 'font-semibold text-primary-900' : 'font-medium text-slate-500',
                )}>
                  {item.label}
                </span>
              </>
            )}
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
