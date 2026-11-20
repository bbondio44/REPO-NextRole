package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SkillRepository extends JpaRepository<Skill,String> { List<Skill> findByActiveTrue(); }
