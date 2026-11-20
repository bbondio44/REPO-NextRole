package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProfileProjectRepository extends JpaRepository<ProfileProject,UUID> {
    List<ProfileProject> findByUserId(UUID userId);
}
