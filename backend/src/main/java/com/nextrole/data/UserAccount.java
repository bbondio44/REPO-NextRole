package com.nextrole.data;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
@Table(name="users")
public class UserAccount {
    @Id
    @Column(name="id")
    public UUID id;
    @Column(name="email")
    public String email;
    @Column(name="first_name")
    public String firstName;
    @Column(name="last_name")
    public String lastName;
    @Column(name="password_hash")
    public String passwordHash;
    @Column(name="role")
    public String role;
    @Column(name="created_at")
    public Instant createdAt;
    protected UserAccount() {}
    public UserAccount(UUID id, String email, String firstName, String lastName, String passwordHash, String role, Instant createdAt) {
        this.id=id;
        this.email=email;
        this.firstName=firstName;
        this.lastName=lastName;
        this.passwordHash=passwordHash;
        this.role=role;
        this.createdAt=createdAt;
    }
}
