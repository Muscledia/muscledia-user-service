package com.muscledia.user_service.avatar.entity;

import com.muscledia.user_service.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "avatars")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Avatar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avatar_id")
    private Long avatarId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_type", nullable = false)
    private AvatarType avatarType;

    @Column(name = "current_level", columnDefinition = "INT DEFAULT 1")
    private Integer currentLevel = 1;

    @Column(name = "current_exp", columnDefinition = "INT DEFAULT 0")
    private Integer currentExp = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlocked_abilities", columnDefinition = "JSON")
    private Map<String, Object> unlockedAbilities;

    @Column(name = "flame_animation", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean flameAnimation = false;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();


}
