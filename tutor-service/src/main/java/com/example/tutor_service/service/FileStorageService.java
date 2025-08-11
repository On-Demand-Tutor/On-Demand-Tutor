package com.example.tutor_service.service;

import com.example.tutor_service.config.FileStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(FileStorageProperties properties) {
        this.fileStorageLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    /**
     * Validate and store file. Returns stored relative path (e.g. uploads/uuid_name.mp4)
     */
    public String storeFile(MultipartFile file) {
        // Validate not empty
        if (file.isEmpty()) {
            throw new RuntimeException("Empty file");
        }

        // Validate content type / extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int idx = originalFilename.lastIndexOf('.');
        if (idx >= 0) ext = originalFilename.substring(idx + 1).toLowerCase();

        boolean allowed = ext.equals("mp4") || ext.equals("pdf") || ext.equals("docx");
        if (!allowed) {
            throw new RuntimeException("File type not allowed. Allowed: mp4, pdf, docx");
        }

        // Create unique filename
        String filename = Instant.now().toEpochMilli() + "_" + UUID.randomUUID() + "_" + originalFilename;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(filename);
            // copy (replace existing)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // return path relative to application root (so you can serve with /uploads/{filename})
            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename, ex);
        }
    }

    public Path getFilePath(String filename) {
        return fileStorageLocation.resolve(filename).normalize();
    }

    public byte[] loadFileAsBytes(String filename) throws IOException {
        Path filePath = getFilePath(filename);
        return Files.readAllBytes(filePath);
    }

    public boolean fileExists(String filename) {
        Path p = getFilePath(filename);
        return Files.exists(p);
    }
}
