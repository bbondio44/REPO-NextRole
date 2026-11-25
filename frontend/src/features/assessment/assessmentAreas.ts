import type { AssessmentAttempt, AssessmentQuestion } from '@/types/domain'

export const ASSESSMENT_AREAS = [
  { name: 'Desarrollo', firstPosition: 1, lastPosition: 16 },
  { name: 'QA', firstPosition: 17, lastPosition: 21 },
  { name: 'Datos', firstPosition: 22, lastPosition: 26 },
  { name: 'Cloud', firstPosition: 27, lastPosition: 31 },
] as const

export function countSavedAnswers(
  questions: AssessmentQuestion[],
  answers: AssessmentAttempt['answers'],
) {
  return questions.reduce(
    (count, question) => count + (Object.hasOwn(answers, question.competencyCode) ? 1 : 0),
    0,
  )
}

export function getAssessmentAreaIndex(position: number) {
  const index = ASSESSMENT_AREAS.findIndex(
    (area) => position >= area.firstPosition && position <= area.lastPosition,
  )
  return index < 0 ? 0 : index
}
