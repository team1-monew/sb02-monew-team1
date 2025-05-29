package com.team1.monew.notification.entity;

import com.team1.monew.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "notifications")
@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 대상 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 50)
    private String resourceType;

    // 관련 리소스 ID (예: 댓글 ID, 기사 ID 등) — 다형적 참조를 위해 Long으로 보관 // 수정 필요!!!
    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false)
    private boolean confirmed = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    protected Notification() {
    }

    public Notification(User user, String content, String resourceType, Long resourceId) {
        this.user = user;
        this.content = content;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.confirmed = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }


}

