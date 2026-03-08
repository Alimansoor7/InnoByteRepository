package com.hackathon.ai_code_generator.service;

import com.hackathon.ai_code_generator.dto.FileEntry;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileStorageService {
    private final Path rootLocation = Paths.get("./generated-code");

    public void saveFiles(List<FileEntry> files, String subfolder) throws IOException {
        Path targetDir = rootLocation.resolve(subfolder);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        for (FileEntry file : files) {
            // Sanitize filename to avoid directory traversal
            String filename = file.getFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            Path filePath = targetDir.resolve(filename).normalize();
            // Ensure the path stays within targetDir
            if (!filePath.startsWith(targetDir)) {
                throw new SecurityException("Invalid filename: " + filename);
            }
            Files.writeString(filePath, file.getContent());
        }
    }
}
