package com.nextrole.skill;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.*;
@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {
    private final SkillRepository skills;
    private final SkillCategoryRepository categories;
    private final UserSkillRepository userSkills;
    public SkillController(SkillRepository skills,SkillCategoryRepository categories,UserSkillRepository userSkills){this.skills=skills;this.categories=categories;this.userSkills=userSkills;}
    public record SkillView(String id,String categoryId,String name,String description){}
    public record CategoryView(String id,String name,String icon){}
    public record Catalog(List<CategoryView> categories,List<SkillView> skills){}
    public record LevelRequest(@NotNull @Min(0) @Max(4) Integer level){}
    public record AreaDiagnosis(String categoryId,String categoryName,Integer percent,int evaluatedCount,int total){}
    public record Diagnosis(int evaluatedCount,int total,Double generalPercent,List<AreaDiagnosis> byCategory){}
    public static SkillView view(Skill s){return new SkillView(s.id,s.categoryId,s.name,s.description);}
    @GetMapping
    @Transactional(readOnly=true)
    public Catalog catalog(){return new Catalog(categories.findAll().stream().map(c->new CategoryView(c.id,c.name,c.icon)).toList(),skills.findByActiveTrue().stream().map(SkillController::view).toList());}
    @GetMapping("/levels")
    @Transactional(readOnly=true)
    public Map<String,Integer> levels(){return userSkills.findByUserId(CurrentUser.id()).stream().collect(Collectors.toMap(s->s.skillId,s->s.level));}
    @PutMapping("/{skillId}/level")
    @Transactional
    public Map<String,Object> setLevel(@PathVariable String skillId,@Valid @RequestBody LevelRequest r){
        skills.findById(skillId).filter(x->Boolean.TRUE.equals(x.active)).orElseThrow(()->ApiProblem.notFound("Competencia"));
        UUID uid=CurrentUser.id();
        UserSkill entry=userSkills.findByUserIdAndSkillId(uid,skillId).orElseGet(()->new UserSkill(UUID.randomUUID(),uid,skillId,0,Instant.now()));
        entry.level=r.level();entry.updatedAt=Instant.now();userSkills.save(entry);
        return Map.of("skillId",skillId,"level",entry.level);
    }
    @GetMapping("/diagnosis")
    @Transactional(readOnly=true)
    public Diagnosis diagnosis(){
        Map<String,Integer> known=userSkills.findByUserId(CurrentUser.id()).stream().collect(Collectors.toMap(s->s.skillId,s->s.level));
        List<Skill> active=skills.findByActiveTrue();
        List<AreaDiagnosis> area=new ArrayList<>();
        for(SkillCategory cat:categories.findAll()){
            List<Skill> inCat=active.stream().filter(s->s.categoryId.equals(cat.id)).toList();
            if(inCat.isEmpty())continue;
            List<Skill> done=inCat.stream().filter(s->known.containsKey(s.id)).toList();
            Integer pct=done.isEmpty()?null:(int)Math.round(done.stream().mapToInt(s->known.get(s.id)).average().orElse(0)*25);
            area.add(new AreaDiagnosis(cat.id,cat.name,pct,done.size(),inCat.size()));
        }
        Double mean=known.isEmpty()?null:known.values().stream().mapToInt(i->i).average().orElse(0)*25;
        return new Diagnosis(known.size(),active.size(),mean,area);
    }
}
