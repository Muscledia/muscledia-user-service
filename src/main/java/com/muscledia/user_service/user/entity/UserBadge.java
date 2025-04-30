package com.muscledia.user_service.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long userBadgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;

    @Column(name = "progress", columnDefinition = "INT DEFAULT 0")
    private int progress = 0;

    @Column(name = "earned_date")
    private LocalDateTime earnedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.earnedDate == null) {
            this.earnedDate = LocalDateTime.now();
        }
    }
}
