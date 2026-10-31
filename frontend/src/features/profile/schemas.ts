import { z } from 'zod'

export const profileSchema = z.object({
  career: z.string().min(1, 'Seleccioná tu carrera o formación.'),
  stage: z.string().min(1, 'Seleccioná tu estado actual.'),
  objective: z.string().min(1, 'Seleccioná tu meta inmediata.'),
})

export type ProfileFormValues = z.infer<typeof profileSchema>

export const CAREER_OPTIONS = [
  'Ingeniería en Informática',
  'Licenciatura en Sistemas',
  'Analista de Sistemas',
  'Ingeniería en Software',
  'Otra carrera tecnológica',
]

export const STAGE_OPTIONS = ['Cursando materias intermedias', 'Cursando últimas materias', 'Recién graduado/a', 'Egresado/a hace tiempo']

export const OBJECTIVE_OPTIONS = [
  'Conseguir mi primera experiencia IT',
  'Definir en qué especializarme',
  'Cambiar de área dentro de IT',
  'Prepararme para una entrevista puntual',
]
