package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="profile_projects")
public class ProfileProject {
    @Id public UUID id;
    @Column(name="user_id") public UUID userId;
    @Column(name="name") public String name;
    @Column(name="description",columnDefinition="text") public String description;
    @Column(name="url") public String url;
    @Column(name="project_type") public String projectType;
    @Column(name="created_at") public Instant createdAt;
    protected ProfileProject() {}
    public ProfileProject(UUID id,UUID userId,String name,String description,String url,String projectType,Instant createdAt){
        this.id=id;this.userId=userId;this.name=name;this.description=description;this.url=url;this.projectType=projectType;this.createdAt=createdAt;
    }
}
