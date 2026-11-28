import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('Explainability API client', () => {
  let api: typeof import('@/services/api')
  let fetchMock: ReturnType<typeof vi.fn>

  beforeEach(async () => {
    vi.resetModules()
    localStorage.clear()
    fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    api = await import('@/services/api')
  })

  afterEach(() => vi.unstubAllGlobals())

  it('uses authenticated V2 read, generate and manual retry contracts with encoded IDs', async () => {
    fetchMock.mockResolvedValueOnce(new Response(JSON.stringify({
      accessToken: 'test-token', user: { firstName: 'Ada', lastName: 'Prueba', email: 'ada@example.test' },
    }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    for (let index = 0; index < 5; index += 1) {
      fetchMock.mockResolvedValueOnce(new Response(JSON.stringify({ status: index === 3 ? 'NOT_STARTED' : 'GENERATING' }), {
        status: index === 3 ? 202 : 200,
        headers: { 'Content-Type': 'application/json' },
      }))
    }

    await api.login('ada@example.test', 'password')
    await api.getRoleCatalogEntry('qa-jr')
    await api.getAssessmentAttempt('old/attempt')
    await api.getDiagnosis('old/attempt', 'qa-jr')
    await api.generateDiagnosis('old/attempt', 'qa-jr')
    await api.retryDiagnosis('old/attempt', 'qa-jr')

    expect(fetchMock.mock.calls.slice(1).map(([url, init]) => [url, (init as RequestInit).method ?? 'GET'])).toEqual([
      ['/api/v2/roles/qa-jr', 'GET'],
      ['/api/v2/assessment-attempts/old%2Fattempt', 'GET'],
      ['/api/v2/assessment-attempts/old%2Fattempt/diagnoses/qa-jr', 'GET'],
      ['/api/v2/assessment-attempts/old%2Fattempt/diagnoses/qa-jr/generate', 'POST'],
      ['/api/v2/assessment-attempts/old%2Fattempt/diagnoses/qa-jr/retry', 'POST'],
    ])
    for (const [, init] of fetchMock.mock.calls.slice(1)) {
      expect(new Headers((init as RequestInit).headers).get('Authorization')).toBe('Bearer test-token')
    }
  })
})
