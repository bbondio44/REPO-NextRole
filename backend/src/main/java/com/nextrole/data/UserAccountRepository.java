package com.nextrole.data;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserAccountRepository extends JpaRepository<UserAccount,UUID> { Optional<UserAccount> findByEmailIgnoreCase(String email); }
