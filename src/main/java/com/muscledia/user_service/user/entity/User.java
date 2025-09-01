package com.muscledia.user_service.user.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.muscledia.user_service.avatar.entity.Avatar;
import com.muscledia.user_service.avatar.entity.AvatarType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @Column(name = "user_id")
    @JsonSerialize(using = ToStringSerializer.class) // This fixes the precision issue
    private Long userId;

    @Column(name = "uuid_string", unique = true, nullable = false, length = 36)
    private String uuidString; // Store the original UUID as string for reference

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Hide password in responses
    private String passwordHash;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "height", precision = 5)
    private Double height;

    @Column(name = "initial_weight", precision = 5)
    private Double initialWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type")
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "initial_avatar_type")
    private AvatarType initialAvatarType;

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Avatar avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserChampion> userChampions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserBadge> userBadges = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "current_streak", columnDefinition = "INT DEFAULT 0")
    private Integer currentStreak;

    @Column(name = "total_exp", columnDefinition = "BIGINT DEFAULT 0")
    private Long totalExp;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        // Initialize default values
        if (this.currentStreak == null) {
            this.currentStreak = 0;
        }
        if (this.totalExp == null) {
            this.totalExp = 0L;
        }
    }

    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public Object getInitialAvatarType() {
        return initialAvatarType != null ? initialAvatarType.name() : null;
    }


    /**
     * STEP 1: Add simple validation to your existing setters
     */
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be 3-50 characters");
        }
        this.username = username.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.toLowerCase().trim();
    }

    /**
     * STEP 1: Simple email validation helper
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
        // This is basic - you can improve it later
    }

    // ========== STEP 1: Add some simple business logic methods ==========

    /**
     * Business logic: Check if user has a specific role
     */
    public boolean hasRole(ERole roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    /**
     * Business logic: Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole(ERole.ROLE_ADMIN);
    }

    /**
     * Business logic: Award badge with validation
     */
    public void awardBadge(Long badgeId) {
        // Check if user already has this badge
        boolean alreadyHasBadge = userBadges.stream()
                .anyMatch(badge -> badge.getBadgeId().equals(badgeId));

        if (alreadyHasBadge) {
            throw new IllegalArgumentException("User already has this badge");
        }

        // Create new user badge
        UserBadge userBadge = new UserBadge();
        userBadge.setUser(this);
        userBadge.setBadgeId(badgeId);
        userBadge.setEarnedDate(LocalDateTime.now());

        userBadges.add(userBadge);
    }

    /**
     * Business logic: Promote user with validation
     */
    public void promoteToAdmin() {
        if (isAdmin()) {
            throw new IllegalArgumentException("User is already admin");
        }

        // Find or create ADMIN role
        Role adminRole = roles.stream()
                .filter(role -> role.getName() == ERole.ROLE_ADMIN)
                .findFirst()
                .orElse(new Role(ERole.ROLE_ADMIN));

        roles.add(adminRole);
    }
}
