import clsx from 'clsx'
import type { AssessmentAnswerOption, KnowledgeLevel } from '@/types/domain'

interface LevelPickerProps {
  value: KnowledgeLevel | null
  options: AssessmentAnswerOption[]
  onChange: (level: KnowledgeLevel) => void
}

export function LevelPicker({ value, options, onChange }: LevelPickerProps) {
  return (
    <div className="grid grid-cols-1 gap-2 sm:grid-cols-5" role="group" aria-label="Nivel declarado">
      {options.map((option) => {
        const selected = value === option.level
        return (
          <button
            key={option.level}
            type="button"
            aria-pressed={selected}
            aria-label={`Nivel ${option.level}: ${option.description}`}
            onClick={() => onChange(option.level)}
            className={clsx(
              'rounded-lg border px-3 py-2 text-left text-xs transition-colors',
              selected
                ? 'border-primary-500 bg-primary-50 text-primary-700'
                : 'border-neutral-200 bg-white text-neutral-600 hover:border-neutral-300',
            )}
          >
            <span className="block font-semibold">Nivel {option.level}</span>
            <span className="mt-1 block leading-relaxed">{option.description}</span>
          </button>
        )
      })}
    </div>
  )
}
