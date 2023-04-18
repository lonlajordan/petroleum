package com.petroleum.repositories;

import com.petroleum.enums.Role;
import com.petroleum.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    Optional<User> findFirstByRole(Role role);
    List<User> findAllByOrderByLastLoginDesc();
}
