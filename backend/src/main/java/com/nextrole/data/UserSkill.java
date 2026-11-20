package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="user_skills")
public class UserSkill {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="user_id")
    public UUID userId;
    @Column(name="skill_id")
    public String skillId;
    @Column(name="level")
    public Integer level;
    @Column(name="updated_at")
    public Instant updatedAt;
    protected UserSkill() {}
    public UserSkill(UUID id, UUID userId, String skillId, Integer level, Instant updatedAt) {
        this.id=id;
        this.userId=userId;
        this.skillId=skillId;
        this.level=level;
        this.updatedAt=updatedAt;
    }
}
