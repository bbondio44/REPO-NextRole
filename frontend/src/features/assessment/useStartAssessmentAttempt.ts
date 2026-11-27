import { useQueryClient } from '@tanstack/react-query'
import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { createAssessmentAttempt, createAssessmentReevaluation } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

export function useStartAssessmentAttempt(hasCompletedHistory: boolean) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const startingRef = useRef(false)
  const [starting, setStarting] = useState(false)
  const [startError, setStartError] = useState<string | null>(null)

  async function startAssessment() {
    if (startingRef.current) return
    startingRef.current = true
    setStarting(true)
    setStartError(null)
    try {
      const attempt = hasCompletedHistory
        ? await createAssessmentReevaluation()
        : await createAssessmentAttempt()
      queryClient.setQueryData<AssessmentAttempt | null>(['assessment-attempt'], attempt)
      navigate('/habilidades/evaluacion')
    } catch (error) {
      setStartError(error instanceof Error ? error.message : 'No pudimos comenzar la evaluación.')
    } finally {
      startingRef.current = false
      setStarting(false)
    }
  }

  return { startAssessment, starting, startError, clearStartError: () => setStartError(null) }
}
