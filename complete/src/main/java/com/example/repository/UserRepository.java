package com.example.repository;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author ishrat.jahan
 * @since 03/28,2024
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}
