package com.nextrole.learning;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import com.nextrole.skillgap.SkillGapService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class LearningService {
    private final SkillGapService gaps;
    private final LearningResourceRepository resources;
    private final LearningPlanRepository plans;
    private final LearningStepRepository steps;
    private final SkillRepository skills;
    public LearningService(SkillGapService gaps,LearningResourceRepository resources,LearningPlanRepository plans,LearningStepRepository steps,SkillRepository skills){
       this.gaps=gaps;this.resources=resources;this.plans=plans;this.steps=steps;this.skills=skills;
    }
    public record ResourceView(String id,String skillId,String name,String provider,String url,String type,boolean isFree,String priority){}
    public record StepView(int order,String skillId,String skillName,String priority,String explanation,List<ResourceView> resources){}
    public record PlanView(UUID id,String roleId,Instant createdAt,List<StepView> steps){}
    public static ResourceView view(LearningResource r){return new ResourceView(r.id,r.skillId,r.name,r.provider,r.url,r.type,Boolean.TRUE.equals(r.free),r.priority);}
    private List<ResourceView> forSkill(String skillId){
        return resources.findBySkillIdAndActiveTrue(skillId).stream().filter(r->r.verifiedAt!=null)
          .sorted(Comparator.comparing((LearningResource r)->r.priority.equals("alta")?0:1))
          .limit(3).map(LearningService::view).toList();
    }
    @Transactional(readOnly=true)
    public List<StepView> recommend(String roleId){
        SkillGapService.Analysis gap=gaps.latest(roleId);
        List<StepView> output=new ArrayList<>();
        for(SkillGapService.GapView item:gap.gaps()){
            if(item.status().equals("cubierta")||item.status().equals("no_evaluada"))continue;
            output.add(new StepView(output.size()+1,item.skill().id(),item.skill().name(),item.priority(),
               item.explanation(),forSkill(item.skill().id())));
        }
        return output;
    }
    @Transactional
    public PlanView create(String roleId){
        SkillGapService.Analysis gap=gaps.analyze(roleId);
        List<StepView> ordered=recommend(roleId);
        LearningPlan p=plans.save(new LearningPlan(UUID.randomUUID(),CurrentUser.id(),roleId,gap.analysisId(),Instant.now()));
        for(StepView s:ordered){
            String primary=s.resources().isEmpty()?null:s.resources().get(0).id();
            steps.save(new LearningStep(UUID.randomUUID(),p.id,s.skillId(),s.order(),primary,s.priority(),s.explanation()));
        }
        return new PlanView(p.id,roleId,p.createdAt,ordered);
    }
    @Transactional(readOnly=true)
    public PlanView current(){
        LearningPlan p=plans.findFirstByUserIdOrderByCreatedAtDesc(CurrentUser.id()).orElseThrow(()->ApiProblem.notFound("Plan de formación"));
        Map<String,Skill> skillMap=skills.findAll().stream().collect(Collectors.toMap(s->s.id,Function.identity()));
        List<StepView> data=steps.findByPlanIdOrderByOrderIndexAsc(p.id).stream().map(s->new StepView(s.orderIndex,s.skillId,
          skillMap.containsKey(s.skillId)?skillMap.get(s.skillId).name:s.skillId,s.priority,s.explanation,forSkill(s.skillId))).toList();
        return new PlanView(p.id,p.roleId,p.createdAt,data);
    }
}
