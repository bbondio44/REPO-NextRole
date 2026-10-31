import { z } from 'zod'

export const registerSchema = z
  .object({
    firstName: z.string().min(2, 'Ingresá tu nombre.'),
    lastName: z.string().min(2, 'Ingresá tu apellido.'),
    email: z.email('Ingresá un correo válido.'),
    password: z.string().min(8, 'Mínimo 8 caracteres.'),
    confirmPassword: z.string(),
    acceptTerms: z.boolean(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Las contraseñas no coinciden.',
    path: ['confirmPassword'],
  })
  .refine((data) => data.acceptTerms === true, {
    message: 'Tenés que aceptar los términos y la privacidad.',
    path: ['acceptTerms'],
  })

export type RegisterFormValues = z.infer<typeof registerSchema>

export const loginSchema = z.object({
  email: z.email('Ingresá un correo válido.'),
  password: z.string().min(1, 'Ingresá tu contraseña.'),
})

export type LoginFormValues = z.infer<typeof loginSchema>
