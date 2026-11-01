package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RoleSkillRepository extends JpaRepository<RoleSkill,UUID> { List<RoleSkill> findByRoleId(String roleId); }
