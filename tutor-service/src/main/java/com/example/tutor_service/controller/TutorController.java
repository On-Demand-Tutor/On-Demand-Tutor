package com.example.tutor_service.controller;

import com.example.tutor_service.entity.Tutor;

// import com.example.tutor_service.service.FileStorageService;
import com.example.tutor_service.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.tutor_service.repository.TutorRepository;
import lombok.RequiredArgsConstructor;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tutors")
public class TutorController {
    @Autowired
    // private FileStorageService fileStorageService;


    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    public List<Tutor> getAllTutors() {
        return tutorService.getAllTutors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutor> getTutorById(@PathVariable Long id) {
        return tutorService.getTutorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Tutor> getTutorByUserId(@PathVariable Long userId) {
        return tutorService.getTutorByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTutor(@Valid @RequestBody Tutor tutor) {
        try {
            Tutor saved = tutorService.createTutor(tutor);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace(); // In stack trace ra console backend
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Tutor> updateTutor(@PathVariable Long id,@Valid @RequestBody Tutor tutor) {
        Tutor updated = tutorService.updateTutor(id, tutor);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTutor(@PathVariable Long id) {
        if (tutorService.getTutorById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        tutorService.deleteTutor(id);
        return ResponseEntity.noContent().build();
    }

}
