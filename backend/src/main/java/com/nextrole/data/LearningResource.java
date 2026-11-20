package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="learning_resources")
public class LearningResource {
    @Id
    @Column(name="id")
    public String id;
    @Column(name="skill_id")
    public String skillId;
    @Column(name="name")
    public String name;
    @Column(name="provider")
    public String provider;
    @Column(name="url")
    public String url;
    @Column(name="type")
    public String type;
    @Column(name="free")
    public Boolean free;
    @Column(name="priority")
    public String priority;
    @Column(name="active")
    public Boolean active;
    @Column(name="verified_at")
    public Instant verifiedAt;
    protected LearningResource() {}
    public LearningResource(String id, String skillId, String name, String provider, String url, String type, Boolean free, String priority, Boolean active, Instant verifiedAt) {
        this.id=id;
        this.skillId=skillId;
        this.name=name;
        this.provider=provider;
        this.url=url;
        this.type=type;
        this.free=free;
        this.priority=priority;
        this.active=active;
        this.verifiedAt=verifiedAt;
    }
}
