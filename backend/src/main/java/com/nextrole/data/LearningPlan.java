package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="learning_plans")
public class LearningPlan {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="user_id")
    public UUID userId;
    @Column(name="role_id")
    public String roleId;
    @Column(name="gap_run_id")
    public UUID gapRunId;
    @Column(name="created_at")
    public Instant createdAt;
    protected LearningPlan() {}
    public LearningPlan(UUID id, UUID userId, String roleId, UUID gapRunId, Instant createdAt) {
        this.id=id;
        this.userId=userId;
        this.roleId=roleId;
        this.gapRunId=gapRunId;
        this.createdAt=createdAt;
    }
}
