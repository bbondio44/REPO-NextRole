import backendEmblem from '@/assets/logos-roles/backend.svg'
import cloudEmblem from '@/assets/logos-roles/cloud.svg'
import dataAnalystEmblem from '@/assets/logos-roles/data-analyst.svg'
import frontendEmblem from '@/assets/logos-roles/frontend.svg'
import fullstackEmblem from '@/assets/logos-roles/fullstack.svg'
import qaEmblem from '@/assets/logos-roles/qa.svg'

export interface RoleVisual {
  label: string
  emblem: string
  emblemAlt: string
  backgroundColor: string
  diagnosisColor: string
}

export const ROLE_VISUALS = {
  'backend-dev-jr': {
    label: 'Backend Developer', emblem: backendEmblem, emblemAlt: 'Emblema de Backend Developer',
    backgroundColor: '#E8F4FC', diagnosisColor: '#0065BE',
  },
  'frontend-dev-jr': {
    label: 'Frontend Developer', emblem: frontendEmblem, emblemAlt: 'Emblema de Frontend Developer',
    backgroundColor: '#F5EBFC', diagnosisColor: '#6B209E',
  },
  'fullstack-dev-jr': {
    label: 'Full Stack Developer', emblem: fullstackEmblem, emblemAlt: 'Emblema de Full Stack Developer',
    backgroundColor: '#E5F8F8', diagnosisColor: '#007B89',
  },
  'qa-jr': {
    label: 'QA', emblem: qaEmblem, emblemAlt: 'Emblema de QA',
    backgroundColor: '#FFF3E1', diagnosisColor: '#B95408',
  },
  'data-analyst-jr': {
    label: 'Data Analyst', emblem: dataAnalystEmblem, emblemAlt: 'Emblema de Data Analyst',
    backgroundColor: '#F0F8E5', diagnosisColor: '#397A20',
  },
  'cloud-jr': {
    label: 'Cloud', emblem: cloudEmblem, emblemAlt: 'Emblema de Cloud',
    backgroundColor: '#EAF0F5', diagnosisColor: '#3C5978',
  },
} satisfies Record<string, RoleVisual>

export type RoleId = keyof typeof ROLE_VISUALS

export function getRoleVisual(roleId: string): RoleVisual | undefined {
  return ROLE_VISUALS[roleId as RoleId]
}
