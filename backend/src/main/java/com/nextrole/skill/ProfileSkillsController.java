package com.nextrole.skill;
import com.nextrole.common.ApiProblem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Rutas alternativas del documento de API para las competencias del usuario. */
@RestController
@RequestMapping("/api/v1/profile/skills")
public class ProfileSkillsController {
    private final SkillController skills;
    public ProfileSkillsController(SkillController skills){this.skills=skills;}
    public record SkillsBulkRequest(@NotNull Map<String,Integer> levels){}
    @GetMapping public Map<String,Integer> list(){return skills.levels();}
    @PutMapping @Transactional
    public Map<String,Integer> save(@Valid @RequestBody SkillsBulkRequest request){
        for(var entry:request.levels().entrySet()){
            Integer level=entry.getValue();
            if(level==null || level<0 || level>4)throw ApiProblem.invalid("Nivel inválido para competencia "+entry.getKey());
            skills.setLevel(entry.getKey(),new SkillController.LevelRequest(level));
        }
        return skills.levels();
    }
}
