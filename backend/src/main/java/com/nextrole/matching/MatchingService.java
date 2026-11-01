package com.nextrole.matching;
import com.nextrole.auth.CurrentUser;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import com.nextrole.role.RoleController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Service
public class MatchingService {
    public static final String ALGORITHM_VERSION="matching-v1.0";
    private final RoleRepository roles;
    private final RoleSkillRepository requirements;
    private final SkillRepository skills;
    private final UserSkillRepository levels;
    private final InterestAnswerRepository interests;
    private final MatchingRunRepository runs;
    private final MatchingResultRepository results;
    public MatchingService(RoleRepository roles,RoleSkillRepository requirements,SkillRepository skills,UserSkillRepository levels,InterestAnswerRepository interests,MatchingRunRepository runs,MatchingResultRepository results){
        this.roles=roles;this.requirements=requirements;this.skills=skills;this.levels=levels;this.interests=interests;this.runs=runs;this.results=results;
    }
    public record SuggestedRole(RoleController.RoleView role,Integer affinity,Integer preparation,Integer confidence,Integer coverage,List<String> matchedSkillNames,String explanation){}
    private String interestDimension(String id){
        if(id.contains("frontend"))return "frontend";
        if(id.contains("backend")||id.contains("fullstack"))return "backend";
        if(id.contains("qa"))return "quality";
        if(id.contains("data"))return "data";
        if(id.contains("devops"))return "cloud";
        return "security";
    }
    @Transactional
    public List<SuggestedRole> analyze(){
        UUID userId=CurrentUser.id();
        Map<String,Integer> known=levels.findByUserId(userId).stream().collect(Collectors.toMap(s->s.skillId,s->s.level));
        Map<String,Integer> liked=interests.findByUserId(userId).stream().collect(Collectors.toMap(a->a.dimension,a->a.score));
        Map<String,Skill> skillMap=skills.findAll().stream().collect(Collectors.toMap(s->s.id,Function.identity()));
        MatchingRun run=runs.save(new MatchingRun(UUID.randomUUID(),userId,ALGORITHM_VERSION,Instant.now()));
        List<SuggestedRole> suggested=new ArrayList<>();
        for(Role r:roles.findByActiveTrue()){
            List<RoleSkill> req=requirements.findByRoleId(r.id);
            double totalWeight=0,observedWeight=0,skillPoints=0;
            List<String> matches=new ArrayList<>();
            for(RoleSkill s:req){
                double w=s.importance.equals("esencial")?2:1;
                totalWeight+=w;
                Integer actual=known.get(s.skillId);
                if(actual==null)continue;
                observedWeight+=w;
                double ratio=Math.min(1.0,(double)actual/s.requiredLevel);
                skillPoints+=w*ratio;
                if(ratio>=0.75 && skillMap.containsKey(s.skillId))matches.add(skillMap.get(s.skillId).name);
            }
            Integer coverage=Scoring.percent(observedWeight,totalWeight);
            Integer preparation=Scoring.percent(skillPoints,observedWeight);
            Integer interest=liked.containsKey(interestDimension(r.id))?liked.get(interestDimension(r.id))*25:null;
            // Unavailable evidence receives no score and no penalty. Weights are renormalized.
            Integer affinity=Scoring.affinity(interest,preparation);
            // Confidence reflects observed requirements and whether interests were answered.
            int confidence=Scoring.confidence(coverage,interest!=null);
            String explanation=affinity==null?"Todavía no hay información suficiente para estimar afinidad":
              "Estimación basada en "+(preparation!=null?"competencias evaluadas":"")+(preparation!=null&&interest!=null?" e intereses":"")+(preparation==null?"intereses declarados":"")+". Cobertura técnica: "+(coverage==null?0:coverage)+"%. No representa probabilidad de contratación.";
            MatchingResult saved=results.save(new MatchingResult(UUID.randomUUID(),run.id,r.id,affinity,preparation,coverage,String.join("|",matches),confidence,explanation));
            suggested.add(new SuggestedRole(RoleController.view(r),saved.affinity,saved.readiness,saved.confidence,coverage,matches,saved.explanation));
        }
        suggested.sort(Comparator.comparing((SuggestedRole s)->s.affinity()==null?-1:s.affinity()).reversed());
        return suggested;
    }
    @Transactional(readOnly=true)
    public List<SuggestedRole> latest(){
        MatchingRun run=runs.findFirstByUserIdOrderByCreatedAtDesc(CurrentUser.id()).orElseThrow(()->ApiProblem.notFound("Análisis de afinidad"));
        Map<String,Role> roleMap=roles.findAll().stream().collect(Collectors.toMap(r->r.id,Function.identity()));
        List<SuggestedRole> data=new ArrayList<>();
        for(MatchingResult result:results.findByRunId(run.id)){
            Role role=roleMap.get(result.roleId);if(role==null)continue;
            data.add(new SuggestedRole(RoleController.view(role),result.affinity,result.readiness,result.confidence,result.coverage,result.matchedSkillNames==null?List.of():Arrays.stream(result.matchedSkillNames.split("\\|",-1)).filter(s->!s.isBlank()).toList(),result.explanation));
        }
        data.sort(Comparator.comparing((SuggestedRole s)->s.affinity()==null?-1:s.affinity()).reversed());
        return data;
    }
}
