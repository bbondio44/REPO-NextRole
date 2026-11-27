export type NextRoleStep = {
  number: string
  title: string
  homeDescription: string
  introDescription: string
  tone: 'blue' | 'violet' | 'amber' | 'mint'
  icon: 'chart' | 'compass' | 'target' | 'cap'
}

export const NEXTROLE_STEPS: NextRoleStep[] = [
  {
    number: '01',
    title: 'Conocé tus habilidades',
    homeDescription: 'Descubrí tus fortalezas.',
    introDescription: 'Identificá qué conocimientos tenés y cuál es tu nivel actual.',
    tone: 'blue',
    icon: 'chart',
  },
  {
    number: '02',
    title: 'Explorá roles IT',
    homeDescription: 'Encontrá oportunidades.',
    introDescription: 'Descubrí qué perfiles profesionales se ajustan mejor a tus habilidades.',
    tone: 'violet',
    icon: 'compass',
  },
  {
    number: '03',
    title: 'Identificá qué mejorar',
    homeDescription: 'Conocé tus próximos desafíos.',
    introDescription: 'Conocé qué competencias necesitás fortalecer para acercarte a cada rol.',
    tone: 'amber',
    icon: 'target',
  },
  {
    number: '04',
    title: 'Seguí creciendo',
    homeDescription: 'Prepará tu formación.',
    introDescription: 'Accedé a recomendaciones de formación orientadas a tu desarrollo profesional.',
    tone: 'mint',
    icon: 'cap',
  },
]
