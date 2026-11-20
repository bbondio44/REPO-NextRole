package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="matching_results")
public class MatchingResult {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="run_id")
    public UUID runId;
    @Column(name="role_id")
    public String roleId;
    @Column(name="affinity")
    public Integer affinity;
    @Column(name="readiness")
    public Integer readiness;
    @Column(name="coverage")
    public Integer coverage;
    @Column(name="matched_skill_names",columnDefinition="text")
    public String matchedSkillNames;
    @Column(name="confidence")
    public Integer confidence;
    @Column(name="explanation",columnDefinition="text")
    public String explanation;
    protected MatchingResult() {}
    public MatchingResult(UUID id, UUID runId, String roleId, Integer affinity, Integer readiness, Integer coverage, String matchedSkillNames, Integer confidence, String explanation) {
        this.id=id;
        this.runId=runId;
        this.roleId=roleId;
        this.affinity=affinity;
        this.readiness=readiness;
        this.coverage=coverage;
        this.matchedSkillNames=matchedSkillNames;
        this.confidence=confidence;
        this.explanation=explanation;
    }
}
