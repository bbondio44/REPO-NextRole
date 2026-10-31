import clsx from 'clsx'
import type { KnowledgeLevel } from '@/types/domain'

// El doc define 5 niveles (0-4), pero la autoevaluación rápida del wireframe
// expone 4 botones. Se omite el nivel 1 ("Inicial") en este selector rápido.
const OPTIONS: { level: KnowledgeLevel; label: string }[] = [
  { level: 0, label: 'Nada' },
  { level: 2, label: 'Básico' },
  { level: 3, label: 'Intermedio' },
  { level: 4, label: 'Avanzado' },
]

interface LevelPickerProps {
  value: KnowledgeLevel | null
  onChange: (level: KnowledgeLevel) => void
}

export function LevelPicker({ value, onChange }: LevelPickerProps) {
  return (
    <div className="grid grid-cols-4 gap-2">
      {OPTIONS.map((opt) => {
        const selected = value === opt.level
        return (
          <button
            key={opt.level}
            type="button"
            onClick={() => onChange(opt.level)}
            className={clsx(
              'rounded-lg border px-2 py-2 text-xs font-medium transition-colors',
              selected
                ? 'border-primary-500 bg-primary-50 text-primary-700'
                : 'border-neutral-200 bg-white text-neutral-500 hover:border-neutral-300',
            )}
          >
            {opt.label}
          </button>
        )
      })}
    </div>
  )
}
