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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    // @PostMapping("/{id}/promo")
    // public ResponseEntity<?> uploadPromoFile(@PathVariable Long id,
    //                                          @RequestParam("file") MultipartFile file) {
    //     Optional<Tutor> optTutor = tutorService.getTutorById(id);
    //     if (optTutor.isEmpty()) {
    //         return ResponseEntity.notFound().build();
    //     }

    //     // Lưu file local
    //     String storedFilename = fileStorageService.storeFile(file);

    //     // Cập nhật DB
    //     Tutor tutor = optTutor.get();
    //     tutor.setPromoFile(storedFilename);
    //     tutorService.updateTutor(id, tutor);

    //     String downloadUrl = String.format("/api/tutors/%d/promo/download", id);
    //     return ResponseEntity.ok(new UploadResponse(storedFilename, downloadUrl));
    // }

    // // ===== Download promo file =====
    // @GetMapping("/{id}/promo/download")
    // public ResponseEntity<?> downloadPromoFile(@PathVariable Long id) {
    //     Optional<Tutor> optTutor = tutorService.getTutorById(id);
    //     if (optTutor.isEmpty() || optTutor.get().getPromoFile() == null) {
    //         return ResponseEntity.notFound().build();
    //     }

    //     String filename = optTutor.get().getPromoFile();
    //     try {
    //         Path path = fileStorageService.getFilePath(filename);
    //         byte[] data = fileStorageService.loadFileAsBytes(filename);
    //         String contentType = Files.probeContentType(path);
    //         if (contentType == null) {
    //             contentType = "application/octet-stream";
    //         }

    //         return ResponseEntity.ok()
    //                 .contentType(MediaType.parseMediaType(contentType))
    //                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
    //                 .body(new ByteArrayResource(data));
    //     } catch (IOException e) {
    //         return ResponseEntity.internalServerError().body("Error reading file");
    //     }
    // }

    // // DTO cho upload response
    // static class UploadResponse {
    //     private String filename;
    //     private String url;

    //     public UploadResponse(String filename, String url) {
    //         this.filename = filename;
    //         this.url = url;
    //     }

    //     public String getFilename() {
    //         return filename;
    //     }

    //     public String getUrl() {
    //         return url;
    //     }
    // }
}
