package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MatchingRunRepository extends JpaRepository<MatchingRun,UUID> { Optional<MatchingRun> findFirstByUserIdOrderByCreatedAtDesc(UUID userId); }
