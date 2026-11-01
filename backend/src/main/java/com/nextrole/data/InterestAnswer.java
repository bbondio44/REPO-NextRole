package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="interest_answers")
public class InterestAnswer {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="user_id")
    public UUID userId;
    @Column(name="dimension")
    public String dimension;
    @Column(name="score")
    public Integer score;
    @Column(name="updated_at")
    public Instant updatedAt;
    protected InterestAnswer() {}
    public InterestAnswer(UUID id, UUID userId, String dimension, Integer score, Instant updatedAt) {
        this.id=id;
        this.userId=userId;
        this.dimension=dimension;
        this.score=score;
        this.updatedAt=updatedAt;
    }
}
