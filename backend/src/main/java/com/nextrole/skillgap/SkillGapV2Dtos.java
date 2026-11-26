package com.nextrole.skillgap;

import com.nextrole.data.AssessmentRequirementImportance;

import java.util.List;
import java.util.UUID;

public final class SkillGapV2Dtos {
    private SkillGapV2Dtos() {}

    public record DiagnosticDto(UUID attemptId, String roleId, String questionnaireVersion,
                                String matrixVersion, List<CompetencyGapDto> competencies) {}

    public record CompetencyGapDto(String competencyCode, String competencyName, int declaredLevel,
                                   int requiredLevel, AssessmentRequirementImportance importance, int gap) {}
}
