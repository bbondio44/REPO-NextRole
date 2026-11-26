package com.nextrole.skillgap;

import com.nextrole.auth.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.nextrole.skillgap.SkillGapV2Dtos.DiagnosticDto;

@RestController
@RequestMapping("/api/v2/assessment-attempts/{attemptId}/skill-gaps")
public class SkillGapV2Controller {
    private final SkillGapV2Service service;

    public SkillGapV2Controller(SkillGapV2Service service) {
        this.service = service;
    }

    @GetMapping("/{roleId}")
    public DiagnosticDto calculate(@PathVariable UUID attemptId, @PathVariable String roleId) {
        return service.calculate(CurrentUser.id(), attemptId, roleId);
    }
}
