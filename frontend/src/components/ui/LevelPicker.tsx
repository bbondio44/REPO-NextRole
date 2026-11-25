import clsx from 'clsx'
import type { AssessmentAnswerOption, KnowledgeLevel } from '@/types/domain'

interface LevelPickerProps {
  value: KnowledgeLevel | null
  options: AssessmentAnswerOption[]
  onChange: (level: KnowledgeLevel) => void
  disabled?: boolean
}

const LEVEL_LABELS: Record<KnowledgeLevel, string> = {
  0: 'Sin conocimientos',
  1: 'Inicial',
  2: 'Básico',
  3: 'Práctico',
  4: 'Avanzado',
}

export function LevelPicker({ value, options, onChange, disabled = false }: LevelPickerProps) {
  const selectedOption = options.find((option) => option.level === value)

  return (
    <div className="@container w-full min-w-0 max-w-full space-y-4">
      <div
        className="grid auto-rows-fr grid-cols-2 gap-2 @min-[360px]:grid-cols-6 @min-[640px]:grid-cols-5"
        role="group"
        aria-label="Nivel declarado"
      >
        {options.map((option, index) => {
          const selected = value === option.level
          return (
            <button
              key={option.level}
              type="button"
              aria-pressed={selected}
              aria-label={LEVEL_LABELS[option.level]}
              disabled={disabled}
              onClick={() => onChange(option.level)}
              className={clsx(
                'flex min-h-11 w-full min-w-0 items-center justify-center rounded-lg border px-2 py-2 text-center text-[15px] font-semibold leading-tight transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 @min-[360px]:col-span-2 @min-[640px]:col-span-1 @min-[640px]:col-start-auto',
                index === 3 && '@min-[360px]:col-start-2',
                index === 4 && 'col-start-2 @min-[360px]:col-start-4',
                selected
                  ? 'border-primary-700 bg-primary-50 text-primary-800 shadow-sm'
                  : 'border-neutral-200 bg-white text-neutral-700 hover:border-primary-300 hover:bg-primary-50',
                disabled && 'cursor-wait opacity-60',
              )}
            >
              <span className="w-full min-w-0 whitespace-normal">{LEVEL_LABELS[option.level]}</span>
            </button>
          )
        })}
      </div>
      {selectedOption && (
        <p aria-live="polite" className="min-h-12 min-w-0 break-words border-l-2 border-primary-400 pl-3 text-[15px] leading-relaxed text-neutral-600">
          {selectedOption.description}
        </p>
      )}
    </div>
  )
}
