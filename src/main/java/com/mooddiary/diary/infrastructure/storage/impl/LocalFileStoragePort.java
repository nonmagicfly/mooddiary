package com.mooddiary.diary.infrastructure.storage.impl;

import com.mooddiary.diary.application.port.out.FileStoragePort;
import com.mooddiary.diary.application.photo.impl.PhotoUploadProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Component
public class LocalFileStoragePort implements FileStoragePort {
    private final PhotoUploadProperties properties;

    public LocalFileStoragePort(PhotoUploadProperties properties) {
        this.properties = properties;
    }

    @Override
    public String save(byte[] content, String relativePath) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("content is required");
        }
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("relativePath is required");
        }

        Path baseDir = Paths.get(properties.getStorageDir());
        Path target = baseDir.resolve(relativePath).normalize();
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file");
        }
    }

    @Override
    public boolean delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        Path baseDir = Paths.get(properties.getStorageDir());
        Path target = baseDir.resolve(relativePath).normalize();
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            return false;
        }
    }
}

