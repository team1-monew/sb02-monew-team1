package com.team1.monew.interest.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long subscriberCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Interest가 save되면, 거기에 맞춰서 keyword가 save / remove됨 (Cascade, orphanRemoval)
    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();

    // 양방향 연관관계 편의 메서드
    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
        keyword.updateInterest(this);
    }

    public void updateKeywords(List<Keyword> keywords) {
        this.keywords.clear();
        keywords.forEach(this::addKeyword);
    }

    public void updateSubscriberCount(Long count) {
        this.subscriberCount = count;
    }

    public Interest(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

}
