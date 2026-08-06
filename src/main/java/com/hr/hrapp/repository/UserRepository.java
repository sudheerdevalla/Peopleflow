package com.hr.hrapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hr.hrapp.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH r.permissions
        WHERE u.username = :username
        """)
    Optional<User> findByUsername(@Param("username") String username);
}