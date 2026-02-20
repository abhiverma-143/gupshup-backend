package com.gupshup.gupshup_backend.repository;

import com.gupshup.gupshup_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Phone number se user dhundne ke liye method
    Optional<User> findByPhoneNumber(String phoneNumber);
}