package com.brentcodes.springboot.librarysystem.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    // or you can do @Query("SELECT s FROM Student s WHERE s.email = ?1")

    boolean existsByEmail(String email);
}
