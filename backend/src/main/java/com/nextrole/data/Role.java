package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="roles")
public class Role {
    @Id
    @Column(name="id")
    public String id;
    @Column(name="name")
    public String name;
    @Column(name="area")
    public String area;
    @Column(name="icon")
    public String icon;
    @Column(name="description",columnDefinition="text")
    public String description;
    @Column(name="responsibilities",columnDefinition="text")
    public String responsibilities;
    @Column(name="demand_tag")
    public String demandTag;
    @Column(name="active")
    public Boolean active;
    protected Role() {}
    public Role(String id, String name, String area, String icon, String description, String responsibilities, String demandTag, Boolean active) {
        this.id=id;
        this.name=name;
        this.area=area;
        this.icon=icon;
        this.description=description;
        this.responsibilities=responsibilities;
        this.demandTag=demandTag;
        this.active=active;
    }
}
