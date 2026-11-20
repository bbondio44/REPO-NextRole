package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GapRunRepository extends JpaRepository<GapRun,UUID> { Optional<GapRun> findFirstByUserIdOrderByCreatedAtDesc(UUID userId); Optional<GapRun> findFirstByUserIdAndRoleIdOrderByCreatedAtDesc(UUID userId,String roleId); }
