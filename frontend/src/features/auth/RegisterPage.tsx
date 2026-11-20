import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { Button } from '@/components/ui/Button'
import { TextField } from '@/components/ui/TextField'
import { AuthShell } from '@/components/layout/AuthShell'
import { Mascot } from '@/components/ui/Mascot'
import { registerAccount } from '@/services/api'
import { type RegisterFormValues, registerSchema } from '@/features/auth/schemas'

export function RegisterPage() {
  const navigate = useNavigate()
  const { setAccount } = useSession()
  const [serverError, setServerError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) })

  async function onSubmit(values: RegisterFormValues) {
    setServerError(null)
    try {
      const account = await registerAccount(values)
      setAccount(account)
      navigate('/onboarding/perfil')
    } catch {
      setServerError('No pudimos crear tu cuenta. Probá de nuevo.')
    }
  }

  return (
    <AuthShell eyebrow="Paso 1 de 6">
      <div className="flex flex-col items-center text-center">
        <Mascot pose="thumbsUp" />
        <h1 className="mt-4 text-2xl font-extrabold">Creá tu cuenta en NextRole</h1>
        <p className="mt-1 text-sm text-white/60">Descubrí tu próximo rol y acelerá tu carrera tecnológica.</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-4 rounded-2xl bg-white p-5 text-neutral-900">
        <div className="grid grid-cols-2 gap-3">
          <TextField label="Nombre" placeholder="Martín" error={errors.firstName?.message} {...register('firstName')} />
          <TextField label="Apellido" placeholder="Gómez" error={errors.lastName?.message} {...register('lastName')} />
        </div>
        <TextField
          label="Correo electrónico"
          type="email"
          placeholder="ejemplo@correo.com"
          error={errors.email?.message}
          {...register('email')}
        />
        <TextField
          label="Contraseña"
          isPassword
          placeholder="Mínimo 8 caracteres"
          error={errors.password?.message}
          {...register('password')}
        />
        <TextField
          label="Confirmar contraseña"
          isPassword
          placeholder="Repetí tu contraseña"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />
        <label className="flex items-start gap-2 text-xs text-neutral-600">
          <input type="checkbox" className="mt-0.5" {...register('acceptTerms')} />
          Acepto los <span className="font-medium text-primary-600">Términos y Condiciones</span> y la{' '}
          <span className="font-medium text-primary-600">Privacidad</span>.
        </label>
        {errors.acceptTerms && <p className="text-xs text-red-500">{errors.acceptTerms.message}</p>}
        {serverError && <p className="text-xs text-red-500">{serverError}</p>}

        <Button type="submit" fullWidth disabled={isSubmitting}>
          {isSubmitting ? 'Creando cuenta…' : 'Crear cuenta →'}
        </Button>
      </form>

      <p className="mt-5 text-center text-sm text-white/70">
        ¿Ya tenés una cuenta?{' '}
        <Link to="/login" className="font-semibold text-white underline">
          Iniciar sesión
        </Link>
      </p>
    </AuthShell>
  )
}
