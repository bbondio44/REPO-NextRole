import { Link } from 'react-router-dom'

import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { ASSESSMENT_ROLE_NAME, type AssessmentRoleResult } from '@/types/domain'

function scoreTone(score: number): 'success' | 'primary' | 'warning' {
  if (score >= 80) return 'success'
  if (score >= 50) return 'primary'
  return 'warning'
}

interface AssessmentResultsRankingProps {
  attemptId: string
  results: AssessmentRoleResult[]
}

export function AssessmentResultsRanking({ attemptId, results }: AssessmentResultsRankingProps) {
  const topResult = results.find((result) => result.rankPosition === 1) ?? results[0]
  const secondaryResults = results.filter((result) => result.rankPosition === 2 || result.rankPosition === 3)
  const otherResults = results.filter((result) => result.rankPosition >= 4)

  return (
    <div className="space-y-3">
      {topResult && (
        <Card className="space-y-4" key={topResult.roleId}>
          <div className="flex justify-end">
            <span className="text-xs font-semibold text-primary-700">Puesto {topResult.rankPosition}</span>
          </div>
          <div>
            <h2 className="text-xl font-bold leading-snug text-neutral-950">
              {ASSESSMENT_ROLE_NAME[topResult.roleId] ?? topResult.roleId}
            </h2>
            <p className="mt-1 text-sm text-neutral-500">Porcentaje de preparación técnica</p>
          </div>
          <div className="flex flex-wrap items-end justify-between gap-3">
            <p className="text-4xl font-extrabold leading-none text-primary-700">{topResult.displayPercentage}%</p>
            <Link to={`/roles/${encodeURIComponent(topResult.roleId)}?attemptId=${encodeURIComponent(attemptId)}`}>
              <Button variant="secondary">Explorar rol →</Button>
            </Link>
          </div>
        </Card>
      )}

      <div className="grid grid-cols-1 gap-3 min-[360px]:grid-cols-2">
        {secondaryResults.map((result) => (
          <Card key={result.roleId} className="min-w-0">
            <p className="text-xs font-semibold text-primary-700">Puesto {result.rankPosition}</p>
            <h3 className="mt-1 break-words text-sm font-bold leading-snug text-neutral-950">
              {ASSESSMENT_ROLE_NAME[result.roleId] ?? result.roleId}
            </h3>
            <p className="mt-3 text-2xl font-extrabold leading-none text-neutral-950">{result.displayPercentage}%</p>
            <p className="mt-1 text-xs text-neutral-500">Preparación técnica</p>
            <Link className="mt-3 block" to={`/roles/${encodeURIComponent(result.roleId)}?attemptId=${encodeURIComponent(attemptId)}`}>
              <Button fullWidth variant="secondary" className="px-2 py-2 text-xs">Explorar rol →</Button>
            </Link>
          </Card>
        ))}
      </div>

      {otherResults.length > 0 && (
        <details>
          <summary className="cursor-pointer text-sm font-semibold text-primary-700">Ver otros roles</summary>
          <div className="mt-3 space-y-3">
            {otherResults.map((result) => (
              <Card key={result.roleId} className="flex min-w-0 items-center justify-between gap-3">
                <div className="min-w-0">
                  <p className="text-xs font-semibold text-primary-700">Puesto {result.rankPosition}</p>
                  <h3 className="mt-1 break-words text-sm font-bold text-neutral-950">
                    {ASSESSMENT_ROLE_NAME[result.roleId] ?? result.roleId}
                  </h3>
                </div>
                <div className="flex shrink-0 items-center gap-2">
                  <Badge tone={scoreTone(Number(result.displayPercentage))}>{result.displayPercentage}%</Badge>
                  <Link to={`/roles/${encodeURIComponent(result.roleId)}?attemptId=${encodeURIComponent(attemptId)}`}>
                    <Button variant="secondary" className="px-3 py-2 text-xs">Explorar rol →</Button>
                  </Link>
                </div>
              </Card>
            ))}
          </div>
        </details>
      )}
    </div>
  )
}
