package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="skill_categories")
public class SkillCategory {
    @Id
    @Column(name="id")
    public String id;
    @Column(name="name")
    public String name;
    @Column(name="icon")
    public String icon;
    protected SkillCategory() {}
    public SkillCategory(String id, String name, String icon) {
        this.id=id;
        this.name=name;
        this.icon=icon;
    }
}
