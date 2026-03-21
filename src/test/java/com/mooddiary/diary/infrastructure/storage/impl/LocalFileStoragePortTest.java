package com.mooddiary.diary.infrastructure.storage.impl;

import com.mooddiary.diary.application.photo.impl.PhotoUploadProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStoragePortTest {

    @Test
    void shouldSaveAndDeleteFile(@TempDir Path tempDir) throws IOException {
        PhotoUploadProperties props = new PhotoUploadProperties();
        props.setStorageDir(tempDir.toString());

        LocalFileStoragePort storage = new LocalFileStoragePort(props);

        String relativePath = "photos/u/d/" + UUID.randomUUID() + "_a.png";
        byte[] content = new byte[] {1, 2, 3};

        String saved = storage.save(content, relativePath);
        assertEquals(relativePath, saved);

        Path target = tempDir.resolve(relativePath);
        assertTrue(Files.exists(target));
        assertArrayEquals(content, Files.readAllBytes(target));

        boolean deleted = storage.delete(relativePath);
        assertTrue(deleted);
        assertTrue(!Files.exists(target));
    }
}

