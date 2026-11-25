import { ASSESSMENT_AREAS, countSavedAnswers, getAssessmentAreaIndex } from '@/features/assessment/assessmentAreas'
import type { AssessmentAttempt, AssessmentQuestion } from '@/types/domain'

interface AssessmentProgressProps {
  currentPosition: number
  questions: AssessmentQuestion[]
  answers: AssessmentAttempt['answers']
}

export function AssessmentProgress({ currentPosition, questions, answers }: AssessmentProgressProps) {
  const area = ASSESSMENT_AREAS[getAssessmentAreaIndex(currentPosition)]
  const areaQuestions = questions.filter(
    (question) => question.position >= area.firstPosition && question.position <= area.lastPosition,
  )
  const areaAnsweredCount = countSavedAnswers(areaQuestions, answers)
  const areaQuestionCount = areaQuestions.length
  const progress = areaQuestionCount > 0 ? Math.min(100, (areaAnsweredCount / areaQuestionCount) * 100) : 0

  return (
    <section aria-label="Progreso de la evaluación" className="space-y-3">
      <div className="flex items-center justify-between">
        <h1 className="text-sm font-semibold text-neutral-700 sm:text-base">{area.name}</h1>
      </div>
      <div
        role="progressbar"
        aria-label="Progreso de la evaluación"
        aria-valuemin={0}
        aria-valuemax={areaQuestionCount}
        aria-valuenow={areaAnsweredCount}
        aria-valuetext={area.name}
        className="h-1.5 w-full overflow-hidden rounded-full bg-neutral-200"
      >
        <div
          className="h-full rounded-full bg-primary-600 transition-[width] duration-300 ease-out"
          style={{ width: `${progress}%` }}
        />
      </div>
    </section>
  )
}
