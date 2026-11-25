import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { createAssessmentAttempt, createAssessmentReevaluation, getAssessmentHistory, getCurrentAssessmentAttempt } from '@/services/api'
import type { AssessmentAttempt } from '@/types/domain'

export function AssessmentAccessPanel() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [starting, setStarting] = useState(false)
  const [startError, setStartError] = useState<string | null>(null)
  const current = useQuery({ queryKey: ['assessment-attempt'], queryFn: getCurrentAssessmentAttempt })
  const history = useQuery({ queryKey: ['assessment-history'], queryFn: getAssessmentHistory })

  async function startAssessment() {
    setStarting(true)
    setStartError(null)
    try {
      const attempt = latestCompleted
        ? await createAssessmentReevaluation()
        : await createAssessmentAttempt()
      queryClient.setQueryData<AssessmentAttempt | null>(['assessment-attempt'], attempt)
      navigate('/habilidades/evaluacion')
    } catch (error) {
      setStartError(error instanceof Error ? error.message : 'No pudimos comenzar la evaluación.')
    } finally {
      setStarting(false)
    }
  }

  if (current.isLoading || history.isLoading) return <p role="status">Consultando tu autoevaluación…</p>
  if (current.isError || history.isError) return <p role="alert">No pudimos consultar el estado de tu autoevaluación.</p>

  const draft = current.data
  const latestCompleted = history.data?.[0]

  return (
    <Card className="mt-4 space-y-3">
      <div>
        <h2 className="text-base font-bold text-neutral-950">Autoevaluación de habilidades</h2>
        <p className="mt-1 text-sm text-neutral-600">
          {draft
            ? 'Tenés una evaluación en curso. Tus respuestas guardadas siguen disponibles.'
            : latestCompleted
              ? 'Tu evaluación está completada. Podés consultar sus resultados.'
              : 'Completá la autoevaluación para descubrir roles compatibles con tu perfil.'}
        </p>
      </div>

      {draft && (
        <Link to="/habilidades/evaluacion" className="block">
          <Button fullWidth>Continuar evaluación</Button>
        </Link>
      )}

      {latestCompleted && (
        <Link to={`/roles?attemptId=${encodeURIComponent(latestCompleted.attemptId)}`} className="block">
          <Button fullWidth variant={draft ? 'secondary' : 'primary'}>
            {draft ? 'Ver mis resultados anteriores' : 'Ver mis resultados'}
          </Button>
        </Link>
      )}

      {!draft && (
        <Button fullWidth disabled={starting} onClick={startAssessment}>
          {starting ? 'Preparando evaluación…' : latestCompleted ? 'Comenzar nueva evaluación' : 'Comenzar evaluación'}
        </Button>
      )}
      {latestCompleted && (
        <Link to="/habilidades/historial" className="block text-center text-sm font-semibold text-primary-700">
          Ver historial de evaluaciones
        </Link>
      )}
      {startError && <p role="alert" className="text-sm text-red-600">{startError}</p>}
    </Card>
  )
}
