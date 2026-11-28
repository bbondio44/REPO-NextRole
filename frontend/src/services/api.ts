import {
  learningResources,
  roleSkillRequirements,
  roles,
  skillCategories,
  skills,
} from '@/services/mockData'
import { type Account, storage } from '@/services/storage'
import type {
  AssessmentAttempt,
  AssessmentHistoryItem,
  AssessmentCatalog,
  AssessmentSavedAnswer,
  ExplainabilityDiagnosisResponse,
  ExplainabilityRoleCatalogEntry,
  AcademicProfile,
  KnowledgeLevel,
  LearningResource,
  MatchingResult,
  Role,
  SkillGapItem,
  SkillGapStatus,
} from '@/types/domain'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''
let accessToken: string | null = null
let refreshInFlight: Promise<Account | null> | null = null

interface BackendAccount {
  firstName: string
  lastName: string
  email: string
}

interface BackendAuthResponse {
  accessToken: string
  user: BackendAccount
}

async function responseError(response: Response): Promise<Error> {
  const body = await response.json().catch(() => null) as { message?: string; code?: string } | null
  return new Error(body?.message ?? body?.code ?? `La API respondió ${response.status}`)
}

async function acceptAuthResponse(response: Response): Promise<Account> {
  if (!response.ok) throw await responseError(response)
  const auth = await response.json() as BackendAuthResponse
  accessToken = auth.accessToken
  const account = { firstName: auth.user.firstName, lastName: auth.user.lastName, email: auth.user.email }
  storage.setAccount(account)
  return account
}

async function refreshSession(): Promise<Account | null> {
  if (refreshInFlight) return refreshInFlight
  refreshInFlight = (async () => {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
      method: 'POST',
      credentials: 'include',
    })
    if (response.status === 401) {
      accessToken = null
      return null
    }
    return acceptAuthResponse(response)
  })().finally(() => { refreshInFlight = null })
  return refreshInFlight
}

async function authenticatedFetch(path: string, init: RequestInit = {}): Promise<Response> {
  if (!accessToken) await refreshSession()
  if (!accessToken) throw new Error('Iniciá sesión para continuar')

  const send = () => {
    const headers = new Headers(init.headers)
    headers.set('Authorization', `Bearer ${accessToken}`)
    return fetch(`${API_BASE_URL}${path}`, { ...init, headers, credentials: 'include' })
  }

  let response = await send()
  if (response.status === 401) {
    accessToken = null
    await refreshSession()
    if (!accessToken) throw await responseError(response)
    response = await send()
  }
  if (!response.ok) throw await responseError(response)
  return response
}

/**
 * Esta capa imita los endpoints REST descriptos en la "Guía de Desarrollo
 * NextRole" (autenticación, perfil, autoevaluación, matching, skill gap y
 * formación). No existe backend todavía: todo se resuelve en el cliente,
 * pero las firmas de las funciones son las que usará TanStack Query, así
 * que conectar el backend real más adelante es reemplazar el cuerpo de cada
 * función, no las pantallas que las consumen.
 */

function delay<T>(value: T, ms = 250): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), ms))
}

// ---------- Auth & cuenta ----------

export interface RegisterInput {
  firstName: string
  lastName: string
  email: string
  password: string
}

export async function registerAccount(input: RegisterInput): Promise<Account> {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({
      firstName: input.firstName,
      lastName: input.lastName,
      email: input.email,
      password: input.password,
      confirmPassword: input.password,
      acceptTerms: true,
    }),
  })
  return acceptAuthResponse(response)
}

export async function login(email: string, password: string): Promise<Account> {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ email, password }),
  })
  return acceptAuthResponse(response)
}

export async function getCurrentAccount(): Promise<Account | null> {
  if (accessToken) return storage.getSession().account
  return refreshSession()
}

export async function logout() {
  try {
    await fetch(`${API_BASE_URL}/api/v1/auth/logout`, { method: 'POST', credentials: 'include' })
  } catch {
    // Clear local state even if the backend is temporarily unavailable.
  } finally {
    accessToken = null
    storage.clear()
  }
}

export async function getAssessmentCatalog(): Promise<AssessmentCatalog> {
  const response = await authenticatedFetch('/api/v2/assessment-catalog/current')
  return response.json() as Promise<AssessmentCatalog>
}

export async function getCurrentAssessmentAttempt(): Promise<AssessmentAttempt | null> {
  const response = await authenticatedFetch('/api/v2/assessment-attempts/current')
  if (response.status === 204) return null
  return response.json() as Promise<AssessmentAttempt>
}

export async function getAssessmentHistory(): Promise<AssessmentHistoryItem[]> {
  const response = await authenticatedFetch('/api/v2/assessment-history')
  return response.json() as Promise<AssessmentHistoryItem[]>
}

export async function createAssessmentAttempt(): Promise<AssessmentAttempt> {
  const response = await authenticatedFetch('/api/v2/assessment-attempts', { method: 'POST' })
  return response.json() as Promise<AssessmentAttempt>
}

export async function createAssessmentReevaluation(): Promise<AssessmentAttempt> {
  const response = await authenticatedFetch('/api/v2/assessment-attempts/reevaluations', { method: 'POST' })
  return response.json() as Promise<AssessmentAttempt>
}

export async function discardAssessmentAttempt(attemptId: string): Promise<void> {
  await authenticatedFetch(`/api/v2/assessment-attempts/${encodeURIComponent(attemptId)}/discard`, { method: 'POST' })
}

export async function getOrStartAssessment(): Promise<AssessmentAttempt> {
  const existing = await getCurrentAssessmentAttempt()
  if (existing) return existing
  await getAssessmentCatalog()
  return createAssessmentAttempt()
}

export async function getAssessmentAttempt(attemptId: string): Promise<AssessmentAttempt> {
  const response = await authenticatedFetch(`/api/v2/assessment-attempts/${encodeURIComponent(attemptId)}`)
  return response.json() as Promise<AssessmentAttempt>
}

export async function getRoleCatalogEntry(roleId: string): Promise<ExplainabilityRoleCatalogEntry> {
  const response = await authenticatedFetch(`/api/v2/roles/${encodeURIComponent(roleId)}`)
  return response.json() as Promise<ExplainabilityRoleCatalogEntry>
}

function diagnosisPath(attemptId: string, roleId: string): string {
  return `/api/v2/assessment-attempts/${encodeURIComponent(attemptId)}/diagnoses/${encodeURIComponent(roleId)}`
}

export async function getDiagnosis(attemptId: string, roleId: string): Promise<ExplainabilityDiagnosisResponse> {
  const response = await authenticatedFetch(diagnosisPath(attemptId, roleId))
  return response.json() as Promise<ExplainabilityDiagnosisResponse>
}

export async function generateDiagnosis(attemptId: string, roleId: string): Promise<ExplainabilityDiagnosisResponse> {
  const response = await authenticatedFetch(`${diagnosisPath(attemptId, roleId)}/generate`, { method: 'POST' })
  return response.json() as Promise<ExplainabilityDiagnosisResponse>
}

export async function retryDiagnosis(attemptId: string, roleId: string): Promise<ExplainabilityDiagnosisResponse> {
  const response = await authenticatedFetch(`${diagnosisPath(attemptId, roleId)}/retry`, { method: 'POST' })
  return response.json() as Promise<ExplainabilityDiagnosisResponse>
}

export async function saveAssessmentAnswer(
  attemptId: string,
  competencyCode: string,
  level: KnowledgeLevel,
): Promise<AssessmentSavedAnswer> {
  const response = await authenticatedFetch(
    `/api/v2/assessment-attempts/${encodeURIComponent(attemptId)}/answers/${encodeURIComponent(competencyCode)}`,
    { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ level }) },
  )
  return response.json() as Promise<AssessmentSavedAnswer>
}

export async function completeAssessmentAttempt(attemptId: string): Promise<AssessmentAttempt> {
  const response = await authenticatedFetch(
    `/api/v2/assessment-attempts/${encodeURIComponent(attemptId)}/complete`,
    { method: 'POST' },
  )
  return response.json() as Promise<AssessmentAttempt>
}

// ---------- Perfil académico ----------

export async function getProfile(): Promise<AcademicProfile | null> {
  return delay(storage.getSession().profile)
}

export async function saveProfile(profile: AcademicProfile): Promise<AcademicProfile> {
  storage.setProfile(profile)
  return delay(profile)
}

// ---------- Catálogo de competencias ----------

export async function getSkillCatalog() {
  return delay({ categories: skillCategories, skills })
}

export async function getSkillLevels(): Promise<Partial<Record<string, KnowledgeLevel>>> {
  return delay(storage.getSession().skillLevels)
}

export async function setSkillLevel(skillId: string, level: KnowledgeLevel) {
  storage.setSkillLevel(skillId, level)
  return delay({ skillId, level })
}

// ---------- Matching de roles ----------

const IMPORTANCE_WEIGHT = { esencial: 2, complementaria: 1 } as const

export async function getRoleMatching(): Promise<MatchingResult[]> {
  const levels = storage.getSession().skillLevels

  const results = roles.map((role) => {
    const requirements = roleSkillRequirements.filter((r) => r.roleId === role.id)
    let weightedScore = 0
    let weightTotal = 0
    const matchedSkillNames: string[] = []

    for (const req of requirements) {
      const current = levels[req.skillId]
      if (current === undefined) continue // no evaluado: no penaliza, reduce confianza
      const weight = IMPORTANCE_WEIGHT[req.importance]
      const ratio = Math.min(1, current / req.requiredLevel)
      weightedScore += weight * ratio
      weightTotal += weight
      if (ratio >= 0.75) {
        const skill = skills.find((s) => s.id === req.skillId)
        if (skill) matchedSkillNames.push(skill.name)
      }
    }

    const affinity = weightTotal > 0 ? Math.round((weightedScore / weightTotal) * 100) : 0
    return { role, affinity, matchedSkillNames }
  })

  return delay(results.sort((a, b) => b.affinity - a.affinity))
}

export async function getRoleById(roleId: string): Promise<Role | undefined> {
  return delay(roles.find((r) => r.id === roleId))
}

export async function selectRole(roleId: string) {
  storage.setSelectedRole(roleId)
  return delay({ roleId })
}

export async function getSelectedRoleId(): Promise<string | null> {
  return delay(storage.getSession().selectedRoleId)
}

// ---------- Skill Gap ----------

function computeStatus(current: KnowledgeLevel | undefined, required: KnowledgeLevel): SkillGapStatus {
  if (current === undefined) return 'no_evaluada'
  if (current >= required) return 'cubierta'
  if (current > 0) return 'parcial'
  return 'faltante'
}

function computePriority(
  status: SkillGapStatus,
  importance: 'esencial' | 'complementaria',
  gap: number,
): 'alta' | 'media' | 'baja' {
  if (status === 'cubierta') return 'baja'
  if (importance === 'esencial' && (status === 'faltante' || status === 'no_evaluada' || gap >= 2)) return 'alta'
  if (importance === 'esencial' || status === 'faltante') return 'media'
  return 'baja'
}

export async function getSkillGap(roleId: string): Promise<SkillGapItem[]> {
  const levels = storage.getSession().skillLevels
  const requirements = roleSkillRequirements.filter((r) => r.roleId === roleId)

  const items: SkillGapItem[] = requirements
    .map((req) => {
      const skill = skills.find((s) => s.id === req.skillId)
      if (!skill) return null
      const current = levels[req.skillId]
      const status = computeStatus(current, req.requiredLevel)
      const gap = Math.max(0, req.requiredLevel - (current ?? 0))
      return {
        skill,
        requiredLevel: req.requiredLevel,
        currentLevel: current ?? null,
        status,
        priority: computePriority(status, req.importance, gap),
      }
    })
    .filter((item): item is SkillGapItem => item !== null)

  const priorityOrder = { alta: 0, media: 1, baja: 2 }
  items.sort((a, b) => priorityOrder[a.priority] - priorityOrder[b.priority])

  return delay(items)
}

// ---------- Formación recomendada ----------

export async function getRecommendedResources(roleId: string): Promise<LearningResource[]> {
  const gap = await getSkillGap(roleId)
  const pendingSkillIds = new Set(
    gap.filter((g) => g.status === 'faltante' || g.status === 'parcial').map((g) => g.skill.id),
  )
  const resources = learningResources.filter((r) => pendingSkillIds.has(r.skillId))
  return delay(resources)
}
