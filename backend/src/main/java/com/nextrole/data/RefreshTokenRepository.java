package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID> { @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from RefreshToken t where t.tokenHash = :hash and t.revokedAt is null") Optional<RefreshToken> lockActiveByHash(@Param("hash") String hash); Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash); List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId); }
