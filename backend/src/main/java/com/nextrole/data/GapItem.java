package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="skill_gap_items")
public class GapItem {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="run_id")
    public UUID runId;
    @Column(name="skill_id")
    public String skillId;
    @Column(name="required_level")
    public Integer requiredLevel;
    @Column(name="current_level")
    public Integer currentLevel;
    @Column(name="status")
    public String status;
    @Column(name="priority")
    public String priority;
    @Column(name="explanation",columnDefinition="text")
    public String explanation;
    protected GapItem() {}
    public GapItem(UUID id, UUID runId, String skillId, Integer requiredLevel, Integer currentLevel, String status, String priority, String explanation) {
        this.id=id;
        this.runId=runId;
        this.skillId=skillId;
        this.requiredLevel=requiredLevel;
        this.currentLevel=currentLevel;
        this.status=status;
        this.priority=priority;
        this.explanation=explanation;
    }
}
