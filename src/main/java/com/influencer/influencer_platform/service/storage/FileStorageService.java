package com.influencer.influencer_platform.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file, String folder, Long userId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path directory = Paths.get(uploadDir, folder, String.valueOf(userId));
            Files.createDirectories(directory);

            String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String safeName = UUID.randomUUID() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = directory.resolve(safeName);
            Files.copy(file.getInputStream(), target);

            return "/uploads/" + folder + "/" + userId + "/" + safeName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file", ex);
        }
    }
}