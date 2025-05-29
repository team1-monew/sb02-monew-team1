package com.team1.monew.useractivity.entity;

import com.team1.monew.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "user_activities")
@Getter
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String actionType;

    @Column(nullable = true, length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    protected UserActivity() {
    }

    public UserActivity(User user, String actionType, String description) {
        this.user = user;
        this.actionType = actionType;
        this.description = description;
        this.createdAt = Instant.now();
    }

}
