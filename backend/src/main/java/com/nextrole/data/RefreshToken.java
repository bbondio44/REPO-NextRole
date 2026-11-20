package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="refresh_tokens")
public class RefreshToken {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="user_id")
    public UUID userId;
    @Column(name="token_hash")
    public String tokenHash;
    @Column(name="expires_at")
    public Instant expiresAt;
    @Column(name="revoked_at")
    public Instant revokedAt;
    protected RefreshToken() {}
    public RefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant revokedAt) {
        this.id=id;
        this.userId=userId;
        this.tokenHash=tokenHash;
        this.expiresAt=expiresAt;
        this.revokedAt=revokedAt;
    }
}
