package com.muscledia.user_service.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_champions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserChampion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_champion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "champion_id", nullable = false)
    private Long championId;

    @Column(name = "current_exercise_count", columnDefinition = "INT DEFAULT 0")
    private int currentExerciseCount = 0;

    @Column(name = "defeated", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean defeated = false;

    @Column(name = "defeat_date")
    private LocalDateTime defeatedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
