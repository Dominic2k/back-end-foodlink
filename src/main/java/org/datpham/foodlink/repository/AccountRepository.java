package org.datpham.foodlink.repository;

import org.datpham.foodlink.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    // TODO: Add query methods needed for auth.
    Account findByUsername(String username);
    boolean existsByUsername(String username);
}
