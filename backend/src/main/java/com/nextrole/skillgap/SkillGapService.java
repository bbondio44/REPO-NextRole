package com.nextrole.skillgap;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import com.nextrole.skill.SkillController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class SkillGapService {
    private final RoleRepository roles;private final SkillRepository skills;private final RoleSkillRepository requirements;
    private final UserSkillRepository levels;private final GapRunRepository runs;private final GapItemRepository items;
    public SkillGapService(RoleRepository roles,SkillRepository skills,RoleSkillRepository requirements,UserSkillRepository levels,GapRunRepository runs,GapItemRepository items){
       this.roles=roles;this.skills=skills;this.requirements=requirements;this.levels=levels;this.runs=runs;this.items=items;
    }
    public record GapView(SkillController.SkillView skill,int requiredLevel,Integer currentLevel,Integer gap,String status,String priority,String explanation){}
    public record Analysis(UUID analysisId,String roleId,List<GapView> gaps){}
    private List<GapView> compute(String roleId){
        roles.findById(roleId).filter(r->Boolean.TRUE.equals(r.active)).orElseThrow(()->ApiProblem.notFound("Rol"));
        Map<String,Integer> known=levels.findByUserId(CurrentUser.id()).stream().collect(Collectors.toMap(v->v.skillId,v->v.level));
        Map<String,Skill> skillMap=skills.findAll().stream().collect(Collectors.toMap(s->s.id,Function.identity()));
        List<GapView> results=new ArrayList<>();
        for(RoleSkill req:requirements.findByRoleId(roleId)){
            Skill skill=skillMap.get(req.skillId);if(skill==null)continue;
            Integer level=known.get(req.skillId);
            Integer gap=GapPolicy.amount(level,req.requiredLevel);
            String status=GapPolicy.status(level,req.requiredLevel);
            String priority=GapPolicy.priority(level,req.requiredLevel,req.importance.equals("esencial"));
            String explanation=level==null?"Evaluá esta competencia antes de determinar una brecha":
             gap==0?"Alcanzás el nivel esperado para este rol":
             "Necesitás mejorar "+gap+" nivel(es); competencia "+req.importance+" para el rol";
            results.add(new GapView(SkillController.view(skill),req.requiredLevel,level,gap,status,priority,explanation));
        }
        Map<String,Integer> ordering=Map.of("alta",0,"media",1,"baja",2,"pendiente",3);
        results.sort(Comparator.comparingInt(v->ordering.get(v.priority())));
        return results;
    }
    @Transactional
    public Analysis analyze(String roleId){
        List<GapView> computed=compute(roleId);
        GapRun run=runs.save(new GapRun(UUID.randomUUID(),CurrentUser.id(),roleId,Instant.now()));
        for(GapView g:computed)items.save(new GapItem(UUID.randomUUID(),run.id,g.skill().id(),g.requiredLevel(),g.currentLevel(),g.status(),g.priority(),g.explanation()));
        return new Analysis(run.id,roleId,computed);
    }
    @Transactional(readOnly=true)
    public Analysis latest(String roleId){
        UUID uid=CurrentUser.id();
        GapRun run=roleId==null?runs.findFirstByUserIdOrderByCreatedAtDesc(uid).orElseThrow(()->ApiProblem.notFound("Diagnóstico"))
          :runs.findFirstByUserIdAndRoleIdOrderByCreatedAtDesc(uid,roleId).orElseThrow(()->ApiProblem.notFound("Diagnóstico"));
        Map<String,Skill> known=skills.findAll().stream().collect(Collectors.toMap(s->s.id,Function.identity()));
        List<GapView> gap=items.findByRunId(run.id).stream().filter(g->known.containsKey(g.skillId))
          .map(g->new GapView(SkillController.view(known.get(g.skillId)),g.requiredLevel,g.currentLevel,g.currentLevel==null?null:Math.max(0,g.requiredLevel-g.currentLevel),g.status,g.priority,g.explanation)).toList();
        return new Analysis(run.id,run.roleId,gap);
    }
}
