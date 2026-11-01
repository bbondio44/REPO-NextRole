package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface InterestAnswerRepository extends JpaRepository<InterestAnswer,UUID> { List<InterestAnswer> findByUserId(UUID userId); Optional<InterestAnswer> findByUserIdAndDimension(UUID userId,String dimension); }
