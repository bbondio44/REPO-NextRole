package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LearningPlanRepository extends JpaRepository<LearningPlan,UUID> { Optional<LearningPlan> findFirstByUserIdOrderByCreatedAtDesc(UUID userId); }
