package com.team1.monew.interest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long subscriberCount = 0L;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public void updateSubscriberCount(Long count) {
        this.subscriberCount = count;
    }

    public Interest(String name) {
        this.name = name;
        this.createdAt = Instant.now();
    }

}
