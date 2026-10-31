import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'

import { useSession } from '@/app/SessionContext'
import { AuthShell } from '@/components/layout/AuthShell'
import { Button } from '@/components/ui/Button'
import { Mascot } from '@/components/ui/Mascot'
import { TextField } from '@/components/ui/TextField'
import { type LoginFormValues, loginSchema } from '@/features/auth/schemas'
import { getProfile, login } from '@/services/api'

export function LoginPage() {
  const navigate = useNavigate()
  const { setAccount } = useSession()
  const [serverError, setServerError] = useState<string | null>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) })

  async function onSubmit(values: LoginFormValues) {
    setServerError(null)
    try {
      const account = await login(values.email, values.password)
      setAccount(account)
      const profile = await getProfile()
      navigate(profile ? '/' : '/onboarding/perfil')
    } catch {
      setServerError('No pudimos iniciar sesión. Revisá tus credenciales.')
    }
  }

  return (
    <AuthShell eyebrow="Comunidad IT">
      <div className="flex flex-col items-center text-center">
        <Mascot />
        <h1 className="mt-4 text-2xl font-extrabold">¡Hola de nuevo!</h1>
        <p className="mt-1 text-sm text-white/60">Ingresá para seguir construyendo tu camino en IT con tu plan personalizado.</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-4 rounded-2xl bg-white p-5 text-neutral-900">
        <TextField
          label="Correo electrónico"
          type="email"
          placeholder="tunombre@ejemplo.com"
          error={errors.email?.message}
          {...register('email')}
        />
        <div>
          <TextField
            label="Contraseña"
            isPassword
            placeholder="••••••••••"
            error={errors.password?.message}
            {...register('password')}
          />
          <div className="mt-1 text-right text-xs font-medium text-primary-600">¿Olvidaste tu contraseña?</div>
        </div>
        {serverError && <p className="text-xs text-red-500">{serverError}</p>}
        <Button type="submit" fullWidth disabled={isSubmitting}>
          {isSubmitting ? 'Ingresando…' : 'Ingresar a mi cuenta →'}
        </Button>
      </form>

      <div className="mt-5 text-center text-sm text-white/70">
        ¿Todavía no tenés cuenta?{' '}
        <Link to="/register" className="font-semibold text-white underline">
          Registrate gratis
        </Link>
        <p className="mt-2 text-xs text-white/40">🛡 Tus datos de progreso y habilidades están seguros</p>
      </div>
    </AuthShell>
  )
}
