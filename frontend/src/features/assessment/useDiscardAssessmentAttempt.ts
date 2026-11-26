import { useRef } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'

import { discardAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

export function useDiscardAssessmentAttempt() {
  const queryClient = useQueryClient()
  const discardRequest = useRef<Promise<void> | null>(null)
  const mutation = useMutation({
    mutationFn: discardAssessmentAttempt,
    onSuccess: () => {
      queryClient.setQueryData<AssessmentAttempt | null>(['assessment-attempt'], null)
    },
  })

  function discard(attemptId: string) {
    if (discardRequest.current) return discardRequest.current
    const request = mutation.mutateAsync(attemptId).then(() => undefined).finally(() => {
      if (discardRequest.current === request) discardRequest.current = null
    })
    discardRequest.current = request
    return request
  }

  return {
    discard,
    isDiscarding: mutation.isPending,
    discardError: mutation.error instanceof Error ? mutation.error.message : null,
    clearDiscardError: mutation.reset,
  }
}
