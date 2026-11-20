/**
 * Ejemplo para que Patricio reemplace frontend/src/services/api.ts.
 * REQUIERE AJUSTAR Domain Types y UX de null/pending antes de integrarlo.
 * Este archivo es una guía; NO forma parte del build del backend.
 */
import type {
  AcademicProfile, KnowledgeLevel, LearningResource, MatchingResult,
  Role, SkillGapItem,
} from '@/types/domain'
import type { Account } from '@/services/storage'

const BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'
let token: string | null = null
let current: Account | null = null

interface AuthResponse {
  accessToken: string
  user: Account
}

function remember(result: AuthResponse): Account {
  token = result.accessToken
  current = result.user
  return result.user
}

async function refresh(): Promise<boolean> {
  const response = await fetch(`${BASE}/auth/refresh`, {
    method: 'POST', credentials: 'include',
  })
  if (!response.ok) {
    token = null
    current = null
    return false
  }
  remember(await response.json() as AuthResponse)
  return true
}

async function request<T>(path: string, init: RequestInit = {}, authenticated = true): Promise<T> {
  if (authenticated && !token && !(await refresh())) throw new Error('Sesión expirada')
  async function attempt() {
    return fetch(`${BASE}${path}`, {
      ...init,
      credentials: 'include',
      headers: {
        ...(init.body ? { 'Content-Type': 'application/json' } : {}),
        ...(authenticated && token ? { Authorization: `Bearer ${token}` } : {}),
        ...(init.headers ?? {}),
      },
    })
  }
  let response = await attempt()
  if (response.status === 401 && authenticated && await refresh()) response = await attempt()
  if (!response.ok) {
    const body: { message?: string } = await response.json().catch(() => ({}))
    throw new Error(body.message ?? `Error HTTP ${response.status}`)
  }
  return response.status === 204 ? null as T : await response.json() as T
}

export interface RegisterInput {
  firstName: string; lastName: string; email: string; password: string
  confirmPassword: string; acceptTerms: boolean
}

export async function registerAccount(data: RegisterInput): Promise<Account> {
  return remember(await request<AuthResponse>('/auth/register', {
    method: 'POST', body: JSON.stringify(data),
  }, false))
}

export async function login(email: string, password: string): Promise<Account> {
  return remember(await request<AuthResponse>('/auth/login', {
    method: 'POST', body: JSON.stringify({ email, password }),
  }, false))
}

export async function getCurrentAccount(): Promise<Account | null> {
  return (token || await refresh()) ? current : null
}

export function logout(): void {
  // El SessionContext actual espera void. En producción conviene convertirlo en async.
  void request('/auth/logout', { method: 'POST' }, false).catch(() => {})
  token = null
  current = null
}

export async function getProfile(): Promise<AcademicProfile | null> {
  return request<AcademicProfile | null>('/profile')
}

export async function saveProfile(profile: AcademicProfile): Promise<AcademicProfile> {
  return request('/profile', { method: 'PUT', body: JSON.stringify(profile) })
}

export async function getSkillCatalog() {
  return request<{ categories: Array<{id: string; name: string; icon: string}>;
    skills: Array<{id: string; categoryId: string; name: string; description: string}> }>('/skills')
}

export async function getSkillLevels(): Promise<Partial<Record<string, KnowledgeLevel>>> {
  return request('/skills/levels')
}

export async function setSkillLevel(skillId: string, level: KnowledgeLevel) {
  return request<{skillId: string; level: KnowledgeLevel}>(`/skills/${encodeURIComponent(skillId)}/level`, {
    method: 'PUT', body: JSON.stringify({level}),
  })
}

export async function getRoleMatching(): Promise<MatchingResult[]> {
  const results = await request<Array<MatchingResult & {affinity: number | null}>>('/matching/analyze', {method:'POST'})
  // TODO frontend: agregar estado 'sin datos' cuando affinity sea null, no 0%.
  return results.map(r => ({...r, affinity: r.affinity ?? 0}))
}

export async function getRoleById(id: string): Promise<Role | undefined> {
  const result = await request<{role: Role}>(`/roles/${encodeURIComponent(id)}`)
  return result.role
}

export async function selectRole(roleId: string) {
  return request<{roleId: string}>('/profile/target-role', {
    method: 'PUT', body: JSON.stringify({roleId}),
  })
}

export async function getSelectedRoleId(): Promise<string | null> {
  const result = await request<{roleId?: string}>('/profile/target-role')
  return result.roleId ?? null
}

export async function getSkillGap(roleId: string): Promise<SkillGapItem[]> {
  const result = await request<{gaps: Array<SkillGapItem & {priority: 'alta'|'media'|'baja'|'pendiente'}>}>(
    '/skill-gap/analyze', {method: 'POST', body: JSON.stringify({roleId})})
  // TODO UI: representar la prioridad 'pendiente' sin sugerir que la competencia falta.
  return result.gaps.map(g => ({...g, priority: g.priority === 'pendiente' ? 'baja' : g.priority}))
}

export async function getRecommendedResources(roleId: string): Promise<LearningResource[]> {
  const steps = await request<Array<{resources: LearningResource[]}>>(
    `/learning/resources?roleId=${encodeURIComponent(roleId)}`)
  return steps.flatMap(s => s.resources)
}
