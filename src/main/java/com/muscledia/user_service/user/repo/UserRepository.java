package com.muscledia.user_service.user.repo;

import com.muscledia.user_service.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


    // New UUID-based methods
    Optional<User> findByUuidString(String uuidString);

    boolean existsByUuidString(String uuidString);

    void deleteByUuidString(String uuidString);

    // Custom query methods for better performance
    @Query("SELECT u.userId FROM User u WHERE u.uuidString = :uuidString")
    Optional<Long> findUserIdByUuidString(@Param("uuidString") String uuidString);

    @Query("SELECT u.uuidString FROM User u WHERE u.userId = :userId")
    Optional<String> findUuidStringByUserId(@Param("userId") Long userId);
}
