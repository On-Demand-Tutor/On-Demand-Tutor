package com.example.feedback_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long tutorId;
    private Long courseId;

    private Integer rating;
    @Column(length = 1000)
    private String comment;
    private LocalDateTime createdAt;
}