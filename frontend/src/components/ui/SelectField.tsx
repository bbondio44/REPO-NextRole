import { forwardRef, type SelectHTMLAttributes } from 'react'
import clsx from 'clsx'

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
}

export const SelectField = forwardRef<HTMLSelectElement, SelectFieldProps>(
  ({ label, error, className, children, ...props }, ref) => (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-neutral-800">{label}</span>
      <select
        ref={ref}
        className={clsx(
          'w-full rounded-xl border bg-white px-3.5 py-3 text-sm text-neutral-900 outline-none transition-colors',
          'focus:border-primary-500 focus:ring-2 focus:ring-primary-100',
          error ? 'border-red-400' : 'border-neutral-200',
          className,
        )}
        {...props}
      >
        {children}
      </select>
      {error && <span className="mt-1 block text-xs text-red-500">{error}</span>}
    </label>
  ),
)
SelectField.displayName = 'SelectField'
