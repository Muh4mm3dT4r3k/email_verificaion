package com.mohamed.emailVerification.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeTokenRepository extends JpaRepository<CodeToken, Integer> {

    Optional<CodeToken> findByCodeToken(String codeToken);
}
