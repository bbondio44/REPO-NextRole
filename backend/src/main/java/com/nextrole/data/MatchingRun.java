package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="matching_runs")
public class MatchingRun {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="user_id")
    public UUID userId;
    @Column(name="algorithm_version")
    public String algorithmVersion;
    @Column(name="created_at")
    public Instant createdAt;
    protected MatchingRun() {}
    public MatchingRun(UUID id, UUID userId, String algorithmVersion, Instant createdAt) {
        this.id=id;
        this.userId=userId;
        this.algorithmVersion=algorithmVersion;
        this.createdAt=createdAt;
    }
}
