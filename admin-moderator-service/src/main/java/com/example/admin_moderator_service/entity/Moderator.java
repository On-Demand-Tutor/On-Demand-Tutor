package com.example.admin_moderator_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Moderator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime joinedAt;

    @Column(nullable = false, unique = true)
    private Long userId;
}
