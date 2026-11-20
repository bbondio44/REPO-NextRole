package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="skill_gap_results")
public class GapRun {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="user_id")
    public UUID userId;
    @Column(name="role_id")
    public String roleId;
    @Column(name="created_at")
    public Instant createdAt;
    protected GapRun() {}
    public GapRun(UUID id, UUID userId, String roleId, Instant createdAt) {
        this.id=id;
        this.userId=userId;
        this.roleId=roleId;
        this.createdAt=createdAt;
    }
}
