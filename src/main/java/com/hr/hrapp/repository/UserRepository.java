package com.hr.hrapp.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.hr.hrapp.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);
}