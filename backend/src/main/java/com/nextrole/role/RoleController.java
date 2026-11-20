package com.nextrole.role;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleRepository roles;private final RoleSkillRepository requirements;private final SkillRepository skills;
    public RoleController(RoleRepository roles,RoleSkillRepository requirements,SkillRepository skills){this.roles=roles;this.requirements=requirements;this.skills=skills;}
    public record RoleView(String id,String name,String area,String icon,String description,List<String> responsibilities,String demandTag){}
    public record RequirementView(String skillId,String skillName,int requiredLevel,String importance){}
    public record RoleDetail(RoleView role,List<RequirementView> requirements){}
    public static RoleView view(Role r){return new RoleView(r.id,r.name,r.area,r.icon,r.description,Arrays.stream(r.responsibilities.split("\\|",-1)).filter(s->!s.isBlank()).toList(),r.demandTag);}
    @GetMapping
    @Transactional(readOnly=true)
    public List<RoleView> all(){return roles.findByActiveTrue().stream().map(RoleController::view).toList();}
    @GetMapping("/{roleId}")
    @Transactional(readOnly=true)
    public RoleDetail one(@PathVariable String roleId){
        Role r=roles.findById(roleId).filter(x->Boolean.TRUE.equals(x.active)).orElseThrow(()->ApiProblem.notFound("Rol"));
        List<RequirementView> req=requirements.findByRoleId(r.id).stream().map(s->new RequirementView(s.skillId,skills.findById(s.skillId).map(t->t.name).orElse(s.skillId),s.requiredLevel,s.importance)).toList();
        return new RoleDetail(view(r),req);
    }
}
