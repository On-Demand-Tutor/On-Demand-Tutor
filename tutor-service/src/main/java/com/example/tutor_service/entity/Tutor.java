package com.example.tutor_service.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;



import jakarta.validation.constraints.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private Long userId;

    @NotBlank
    private String qualifications;

    @NotBlank
    private String skills;

    @NotNull(message = "Rating không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating phải >= 0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating phải <= 5")
    private Double rating;  

    @NotBlank
    private String teachingGrades;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    
    @NotNull(message = "Giá không được để trống")
    @PositiveOrZero(message = "Giá phải >= 0")
    private Double price;

   
    @NotBlank(message = "Thời gian không được để trống")
    private String availableTime;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
}

