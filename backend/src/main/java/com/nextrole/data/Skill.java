package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="skills")
public class Skill {
    @Id
    @Column(name="id")
    public String id;
    @Column(name="category_id")
    public String categoryId;
    @Column(name="name")
    public String name;
    @Column(name="description",columnDefinition="text")
    public String description;
    @Column(name="active")
    public Boolean active;
    protected Skill() {}
    public Skill(String id, String categoryId, String name, String description, Boolean active) {
        this.id=id;
        this.categoryId=categoryId;
        this.name=name;
        this.description=description;
        this.active=active;
    }
}
