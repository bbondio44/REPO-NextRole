package com.nextrole.learning;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {
    private final LearningService service;
    public LearningController(LearningService service){this.service=service;}
    public record PlanRequest(@NotBlank String roleId){}
    @GetMapping("/resources") public List<LearningService.StepView> resources(@RequestParam String roleId){return service.recommend(roleId);}
    @PostMapping("/plans") public LearningService.PlanView create(@Valid @RequestBody PlanRequest request){return service.create(request.roleId());}
    @GetMapping("/plans/current") public LearningService.PlanView current(){return service.current();}
}
