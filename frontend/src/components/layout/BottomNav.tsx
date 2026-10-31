import { NavLink } from 'react-router-dom'
import clsx from 'clsx'

const ICONS: Record<string, string> = {
  home: 'M3 11l9-8 9 8M5 10v10h14V10',
  skills: 'M4 19V9m7 10V5m7 14v-7',
  roles: 'M12 3a9 9 0 100 18 9 9 0 000-18zM12 12l4-4m-10 4a6 6 0 1012 0 6 6 0 00-12 0z',
  learning: 'M3 9l9-5 9 5-9 5-9-5zm0 0v6c0 1.5 4 3 9 3s9-1.5 9-3V9',
  profile: 'M12 12a4 4 0 100-8 4 4 0 000 8zm-7 8a7 7 0 0114 0',
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
    <nav className="fixed inset-x-0 bottom-0 z-20 border-t border-neutral-100 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-md items-stretch justify-between px-2">
        {ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              clsx(
                'flex flex-1 flex-col items-center gap-1 py-2.5 text-[11px] font-medium',
                isActive ? 'text-primary-600' : 'text-neutral-400',
              )
            }
          >
            <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
              <path d={ICONS[item.icon]} />
            </svg>
            {item.label}
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
