package com.example.repository;

import com.example.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author ishrat.jahan
 * @since 03/28,2024
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

}