package com.mooddiary.diary.application.photo;

public record PhotoUploadFileCommand(
        String originalFileName,
        String contentType,
        long size,
        byte[] content
) {
}

