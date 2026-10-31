import { forwardRef, type InputHTMLAttributes, type ReactNode, useState } from 'react'
import clsx from 'clsx'

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  icon?: ReactNode
  isPassword?: boolean
}

export const TextField = forwardRef<HTMLInputElement, TextFieldProps>(
  ({ label, error, icon, isPassword, className, type, ...props }, ref) => {
    const [visible, setVisible] = useState(false)
    const resolvedType = isPassword ? (visible ? 'text' : 'password') : type

    return (
      <label className="block">
        <span className="mb-1.5 block text-sm font-medium text-neutral-800">{label}</span>
        <span className="relative flex items-center">
          {icon && <span className="pointer-events-none absolute left-3.5 text-neutral-400">{icon}</span>}
          <input
            ref={ref}
            type={resolvedType}
            className={clsx(
              'w-full rounded-xl border bg-white px-3.5 py-3 text-sm text-neutral-900 outline-none transition-colors placeholder:text-neutral-400',
              'focus:border-primary-500 focus:ring-2 focus:ring-primary-100',
              icon && 'pl-10',
              isPassword && 'pr-10',
              error ? 'border-red-400' : 'border-neutral-200',
              className,
            )}
            {...props}
          />
          {isPassword && (
            <button
              type="button"
              onClick={() => setVisible((v) => !v)}
              className="absolute right-3.5 text-xs font-medium text-neutral-400 hover:text-neutral-600"
            >
              {visible ? 'Ocultar' : 'Ver'}
            </button>
          )}
        </span>
        {error && <span className="mt-1 block text-xs text-red-500">{error}</span>}
      </label>
    )
  },
)
TextField.displayName = 'TextField'
