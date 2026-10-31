import {
  learningResources,
  roleSkillRequirements,
  roles,
  skillCategories,
  skills,
} from '@/services/mockData'
import { type Account, storage } from '@/services/storage'
import type {
  AcademicProfile,
  KnowledgeLevel,
  LearningResource,
  MatchingResult,
  Role,
  SkillGapItem,
  SkillGapStatus,
} from '@/types/domain'

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
  const account: Account = { firstName: input.firstName, lastName: input.lastName, email: input.email }
  storage.setAccount(account)
  return delay(account)
}

export async function login(email: string, _password: string): Promise<Account> {
  const existing = storage.getSession().account
  const account = existing ?? { firstName: 'Estudiante', lastName: '', email }
  storage.setAccount(account)
  return delay(account)
}

export async function getCurrentAccount(): Promise<Account | null> {
  return delay(storage.getSession().account)
}

export function logout() {
  storage.clear()
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
