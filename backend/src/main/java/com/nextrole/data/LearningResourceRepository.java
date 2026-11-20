package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LearningResourceRepository extends JpaRepository<LearningResource,String> { List<LearningResource> findBySkillIdAndActiveTrue(String skillId); }
