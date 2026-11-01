package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="profiles")
public class Profile {
    @Id
    @Column(name="user_id")
    public UUID userId;
    @Column(name="career")
    public String career;
    @Column(name="stage")
    public String stage;
    @Column(name="objective")
    public String objective;
    @Column(name="university")
    public String university;
    @Column(name="graduation_year")
    public Integer graduationYear;
    @Column(name="selected_role_id")
    public String selectedRoleId;
    @Column(name="updated_at")
    public Instant updatedAt;
    protected Profile() {}
    public Profile(UUID userId, String career, String stage, String objective, String university, Integer graduationYear, String selectedRoleId, Instant updatedAt) {
        this.userId=userId;
        this.career=career;
        this.stage=stage;
        this.objective=objective;
        this.university=university;
        this.graduationYear=graduationYear;
        this.selectedRoleId=selectedRoleId;
        this.updatedAt=updatedAt;
    }
}
