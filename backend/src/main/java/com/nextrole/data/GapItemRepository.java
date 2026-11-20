package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GapItemRepository extends JpaRepository<GapItem,UUID> { List<GapItem> findByRunId(UUID runId); }
