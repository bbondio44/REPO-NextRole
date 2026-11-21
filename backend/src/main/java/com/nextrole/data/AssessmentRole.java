package com.nextrole.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_roles")
public class AssessmentRole {
    @Id
    @Column(name = "id", length = 100)
    public String id;

    @Column(name = "name", nullable = false, unique = true, length = 180)
    public String name;

    protected AssessmentRole() {}
}
