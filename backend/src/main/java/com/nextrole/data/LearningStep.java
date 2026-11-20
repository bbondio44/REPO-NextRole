package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="learning_steps")
public class LearningStep {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="plan_id")
    public UUID planId;
    @Column(name="skill_id")
    public String skillId;
    @Column(name="order_index")
    public Integer orderIndex;
    @Column(name="resource_id")
    public String resourceId;
    @Column(name="priority")
    public String priority;
    @Column(name="explanation",columnDefinition="text")
    public String explanation;
    protected LearningStep() {}
    public LearningStep(UUID id, UUID planId, String skillId, Integer orderIndex, String resourceId, String priority, String explanation) {
        this.id=id;
        this.planId=planId;
        this.skillId=skillId;
        this.orderIndex=orderIndex;
        this.resourceId=resourceId;
        this.priority=priority;
        this.explanation=explanation;
    }
}
