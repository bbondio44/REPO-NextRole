package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MatchingResultRepository extends JpaRepository<MatchingResult,UUID> { List<MatchingResult> findByRunId(UUID runId); }
