package com.team1.monew.useractivity.entity;

import com.team1.monew.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    protected UserActivity() {
    }

    public UserActivity(User user, String actionType, String description) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

}
