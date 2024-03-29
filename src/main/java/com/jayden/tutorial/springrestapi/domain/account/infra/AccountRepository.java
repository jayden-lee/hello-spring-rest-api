package com.jayden.tutorial.springrestapi.domain.account.infra;

import com.jayden.tutorial.springrestapi.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String username);
}
