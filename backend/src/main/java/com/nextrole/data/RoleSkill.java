package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="role_skills")
public class RoleSkill {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="role_id")
    public String roleId;
    @Column(name="skill_id")
    public String skillId;
    @Column(name="required_level")
    public Integer requiredLevel;
    @Column(name="importance")
    public String importance;
    protected RoleSkill() {}
    public RoleSkill(UUID id, String roleId, String skillId, Integer requiredLevel, String importance) {
        this.id=id;
        this.roleId=roleId;
        this.skillId=skillId;
        this.requiredLevel=requiredLevel;
        this.importance=importance;
    }
}
