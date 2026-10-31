import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'

import { Button } from '@/components/ui/Button'
import { Mascot } from '@/components/ui/Mascot'
import { SelectField } from '@/components/ui/SelectField'
import {
  CAREER_OPTIONS,
  OBJECTIVE_OPTIONS,
  type ProfileFormValues,
  profileSchema,
  STAGE_OPTIONS,
} from '@/features/profile/schemas'
import { saveProfile } from '@/services/api'

export function ProfileOnboardingPage() {
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormValues>({ resolver: zodResolver(profileSchema) })

  async function onSubmit(values: ProfileFormValues) {
    await saveProfile(values)
    navigate('/')
  }

  return (
    <div className="min-h-dvh bg-slate-50 px-4 py-6">
      <div className="mx-auto max-w-md">
        <div className="rounded-2xl bg-primary-50 p-4">
          <p className="text-xs font-bold uppercase tracking-wide text-primary-600">¡Hola!</p>
          <h1 className="mt-1 text-lg font-extrabold text-neutral-950">Armemos tu perfil profesional</h1>
          <p className="mt-1 text-sm text-neutral-600">
            Completá estos 3 datos para que podamos recomendarte tus roles ideales.
          </p>
          <div className="mt-2 flex justify-end">
            <Mascot className="h-16 w-16" />
          </div>
        </div>

        <div className="mt-5">
          <h2 className="text-base font-bold text-neutral-950">Completá tu perfil</h2>
          <p className="text-sm text-neutral-500">Paso 1 de 2: Formación y rumbo</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-4 space-y-4">
          <SelectField label="Carrera o estudios" defaultValue="" error={errors.career?.message} {...register('career')}>
            <option value="" disabled>
              Seleccioná tu carrera o formación
            </option>
            {CAREER_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </SelectField>

          <SelectField label="Año de cursada o egreso" defaultValue="" error={errors.stage?.message} {...register('stage')}>
            <option value="" disabled>
              Seleccioná tu estado actual
            </option>
            {STAGE_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </SelectField>

          <SelectField label="Objetivo profesional" defaultValue="" error={errors.objective?.message} {...register('objective')}>
            <option value="" disabled>
              Seleccioná tu meta inmediata
            </option>
            {OBJECTIVE_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </SelectField>

          <Button type="submit" fullWidth disabled={isSubmitting}>
            {isSubmitting ? 'Guardando…' : 'Continuar →'}
          </Button>
        </form>
      </div>
    </div>
  )
}
