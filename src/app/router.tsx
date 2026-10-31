import type { ReactNode } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'

import { RequireAuth, RequireProfile } from '@/app/RequireAuth'
import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'
import { SkillAssessmentPage } from '@/features/assessment/SkillAssessmentPage'
import { SkillsIndexPage } from '@/features/assessment/SkillsIndexPage'
import { SkillSummaryPage } from '@/features/assessment/SkillSummaryPage'
import { HomePage } from '@/features/home/HomePage'
import { LearningIndexPage } from '@/features/learning/LearningIndexPage'
import { LearningPage } from '@/features/learning/LearningPage'
import { ProfileOnboardingPage } from '@/features/profile/ProfileOnboardingPage'
import { ProfilePage } from '@/features/profile/ProfilePage'
import { RoleDetailPage } from '@/features/roles/RoleDetailPage'
import { RolesSuggestedPage } from '@/features/roles/RolesSuggestedPage'
import { SkillGapPage } from '@/features/skill-gap/SkillGapPage'

function Protected({ children }: { children: ReactNode }) {
  return (
    <RequireAuth>
      <RequireProfile>{children}</RequireProfile>
    </RequireAuth>
  )
}

export function AppRouter() {
  return (
    <Routes>
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/onboarding/perfil"
        element={
          <RequireAuth>
            <ProfileOnboardingPage />
          </RequireAuth>
        }
      />

      <Route path="/" element={<Protected><HomePage /></Protected>} />
      <Route path="/perfil" element={<Protected><ProfilePage /></Protected>} />

      <Route path="/habilidades" element={<Protected><SkillsIndexPage /></Protected>} />
      <Route path="/habilidades/evaluacion" element={<Protected><SkillAssessmentPage /></Protected>} />
      <Route path="/habilidades/resumen" element={<Protected><SkillSummaryPage /></Protected>} />

      <Route path="/roles" element={<Protected><RolesSuggestedPage /></Protected>} />
      <Route path="/roles/:roleId" element={<Protected><RoleDetailPage /></Protected>} />
      <Route path="/roles/:roleId/brechas" element={<Protected><SkillGapPage /></Protected>} />
      <Route path="/roles/:roleId/formacion" element={<Protected><LearningPage /></Protected>} />

      <Route path="/formacion" element={<Protected><LearningIndexPage /></Protected>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
