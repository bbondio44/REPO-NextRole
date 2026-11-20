package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserSkillRepository extends JpaRepository<UserSkill,UUID> { List<UserSkill> findByUserId(UUID userId); Optional<UserSkill> findByUserIdAndSkillId(UUID userId,String skillId); }
