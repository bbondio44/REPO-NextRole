package com.nextrole.skillgap;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/skill-gap")
public class SkillGapController {
    private final SkillGapService service;
    public SkillGapController(SkillGapService service){this.service=service;}
    public record AnalyzeRequest(@NotBlank String roleId){}
    @PostMapping("/analyze") public SkillGapService.Analysis analyze(@Valid @RequestBody AnalyzeRequest req){return service.analyze(req.roleId());}
    @GetMapping("/latest") public SkillGapService.Analysis latest(@RequestParam(required=false) String roleId){return service.latest(roleId);}
}
