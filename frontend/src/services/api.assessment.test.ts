import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

describe('assessment API client', () => {
  let api: typeof import('@/services/api')
  let fetchMock: ReturnType<typeof vi.fn>

  beforeEach(async () => {
    vi.resetModules()
    localStorage.clear()
    fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    api = await import('@/services/api')
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('uses the JWT from the existing backend login for V2 requests and sends no user ID', async () => {
    fetchMock
      .mockResolvedValueOnce(new Response(JSON.stringify({
        accessToken: 'test-access-token',
        user: { firstName: 'Ada', lastName: 'Prueba', email: 'ada@example.test' },
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        questionnaireVersion: 'Q1',
        questions: [],
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        competencyCode: 'programming-fundamentals',
        level: 0,
        answeredCount: 1,
        totalQuestions: 31,
      }), { status: 200, headers: { 'Content-Type': 'application/json' } }))

    await api.login('ada@example.test', 'password')
    await api.getAssessmentCatalog()
    await api.saveAssessmentAnswer('attempt-1', 'programming-fundamentals', 0)

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/v1/auth/login', expect.objectContaining({ credentials: 'include' }))
    const assessmentRequest = fetchMock.mock.calls[2]
    expect(assessmentRequest[0]).toBe('/api/v2/assessment-attempts/attempt-1/answers/programming-fundamentals')
    expect(new Headers((assessmentRequest[1] as RequestInit).headers).get('Authorization')).toBe('Bearer test-access-token')
    expect(JSON.parse((assessmentRequest[1] as RequestInit).body as string)).toEqual({ level: 0 })
  })

  it('retrieves persisted completion results from the V2 endpoint without changing server rank order', async () => {
    const completed = {
      attemptId: 'attempt-2',
      status: 'COMPLETED',
      results: [
        { roleId: 'qa-jr', scoreNumerator: '50', scoreDenominator: '1', displayPercentage: '50.0', rankPosition: 1 },
        { roleId: 'backend-dev-jr', scoreNumerator: '95', scoreDenominator: '1', displayPercentage: '95.0', rankPosition: 2 },
      ],
    }
    fetchMock.mockResolvedValueOnce(new Response(JSON.stringify({
      accessToken: 'test-access-token',
      user: { firstName: 'Ada', lastName: 'Prueba', email: 'ada@example.test' },
    }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify(completed), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }))

    await api.login('ada@example.test', 'password')
    const result = await api.completeAssessmentAttempt('attempt-2')

    expect(fetchMock.mock.calls[1][0]).toBe('/api/v2/assessment-attempts/attempt-2/complete')
    expect((fetchMock.mock.calls[1][1] as RequestInit).method).toBe('POST')
    expect(result.results?.map((role) => role.rankPosition)).toEqual([1, 2])
    expect(result.results?.[0].roleId).toBe('qa-jr')
  })
})
