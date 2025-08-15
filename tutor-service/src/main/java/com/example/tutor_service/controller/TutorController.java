package com.example.tutor_service.controller;

import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorRepository tutorRepository;

    @PostMapping
    public Tutor createTutor(@RequestBody Map<String, Object> tutorData) {
        Tutor tutor = Tutor.builder()
                .userId(Long.valueOf(tutorData.get("userId").toString()))
                .qualifications((String) tutorData.get("qualifications"))
                .skills((String) tutorData.get("skills"))
                .teachingGrades((String) tutorData.get("teachingGrades"))
                .build();

        return tutorRepository.save(tutor);
    }
}
