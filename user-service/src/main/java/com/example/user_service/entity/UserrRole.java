package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserrRole {
    @Id
    private String name;
    private String description;
}

