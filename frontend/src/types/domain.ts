export type KnowledgeLevel = 0 | 1 | 2 | 3 | 4

export interface AssessmentAnswerOption {
  level: KnowledgeLevel
  description: string
}

export interface AssessmentQuestion {
  competencyCode: string
  position: number
  title: string
  question: string
  options: AssessmentAnswerOption[]
}

export interface AssessmentCatalog {
  questionnaireVersion: string
  questions: AssessmentQuestion[]
}

export interface AssessmentAttempt {
  attemptId: string
  status: 'DRAFT' | 'COMPLETED' | 'DISCARDED'
  createdAt: string
  completedAt: string | null
  questionnaireVersion: string
  matrixVersion: string
  answeredCount: number
  totalQuestions: number
  answers: Record<string, KnowledgeLevel>
  questions: AssessmentQuestion[]
  results?: AssessmentRoleResult[]
}

export interface AssessmentRoleResult {
  roleId: string
  scoreNumerator: string
  scoreDenominator: string
  displayPercentage: string
  rankPosition: number
}

export interface AssessmentHistoryItem {
  attemptId: string
  completedAt: string
  questionnaireVersion: string
  matrixVersion: string
  results: AssessmentRoleResult[]
}

export const ASSESSMENT_ROLE_NAME: Record<string, string> = {
  'backend-dev-jr': 'Backend',
  'frontend-dev-jr': 'Frontend',
  'fullstack-dev-jr': 'Full Stack',
  'qa-jr': 'QA',
  'data-analyst-jr': 'Data Analyst',
  'cloud-jr': 'Cloud',
}

export interface AssessmentSavedAnswer {
  competencyCode: string
  level: KnowledgeLevel
  answeredCount: number
  totalQuestions: number
}

export const KNOWLEDGE_LEVEL_LABEL: Record<KnowledgeLevel, string> = {
  0: 'Sin conocimientos',
  1: 'Inicial',
  2: 'Básico',
  3: 'Intermedio',
  4: 'Avanzado',
}

export interface SkillCategory {
  id: string
  name: string
  icon: string
}

export interface Skill {
  id: string
  categoryId: string
  name: string
  description: string
}

export interface Role {
  id: string
  name: string
  area: string
  icon: string
  description: string
  responsibilities: string[]
  demandTag: string
}

export interface RoleSkillRequirement {
  roleId: string
  skillId: string
  requiredLevel: KnowledgeLevel
  importance: 'esencial' | 'complementaria'
}

export interface LearningResource {
  id: string
  skillId: string
  name: string
  provider: string
  url: string
  type: 'curso' | 'video' | 'articulo' | 'documentacion'
  isFree: boolean
  priority: 'alta' | 'media'
}

export interface AcademicProfile {
  career: string
  stage: string
  objective: string
}

export interface MatchingResult {
  role: Role
  affinity: number
  matchedSkillNames: string[]
}

export type SkillGapStatus = 'cubierta' | 'parcial' | 'faltante' | 'no_evaluada'

export interface SkillGapItem {
  skill: Skill
  requiredLevel: KnowledgeLevel
  currentLevel: KnowledgeLevel | null
  status: SkillGapStatus
  priority: 'alta' | 'media' | 'baja'
}
