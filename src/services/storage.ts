import type { AcademicProfile, KnowledgeLevel } from '@/types/domain'

/**
 * No hay backend todavía: esta capa simula la persistencia que normalmente
 * haría la API REST (ver doc "Guía de Desarrollo NextRole", capa de
 * persistencia). El resto de la app solo conoce estas funciones, así que
 * reemplazarlas por llamadas reales a Spring Boot no requiere tocar pantallas.
 */

export interface Account {
  firstName: string
  lastName: string
  email: string
}

interface SessionData {
  account: Account | null
  profile: AcademicProfile | null
  skillLevels: Partial<Record<string, KnowledgeLevel>>
  selectedRoleId: string | null
}

const STORAGE_KEY = 'nextrole.session.v1'

function readRaw(): SessionData {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return emptySession()
    return { ...emptySession(), ...JSON.parse(raw) }
  } catch {
    return emptySession()
  }
}

function writeRaw(data: SessionData) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

function emptySession(): SessionData {
  return { account: null, profile: null, skillLevels: {}, selectedRoleId: null }
}

export const storage = {
  getSession(): SessionData {
    return readRaw()
  },
  setAccount(account: Account) {
    writeRaw({ ...readRaw(), account })
  },
  setProfile(profile: AcademicProfile) {
    writeRaw({ ...readRaw(), profile })
  },
  setSkillLevel(skillId: string, level: KnowledgeLevel) {
    const session = readRaw()
    writeRaw({ ...session, skillLevels: { ...session.skillLevels, [skillId]: level } })
  },
  setSelectedRole(roleId: string | null) {
    writeRaw({ ...readRaw(), selectedRoleId: roleId })
  },
  clear() {
    localStorage.removeItem(STORAGE_KEY)
  },
}
