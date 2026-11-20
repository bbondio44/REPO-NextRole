package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LearningStepRepository extends JpaRepository<LearningStep,UUID> { List<LearningStep> findByPlanIdOrderByOrderIndexAsc(UUID planId); }
