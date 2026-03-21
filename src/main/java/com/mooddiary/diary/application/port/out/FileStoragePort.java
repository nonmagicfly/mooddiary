package com.mooddiary.diary.application.port.out;

public interface FileStoragePort {

    String save(byte[] content, String relativePath);

    boolean delete(String relativePath);
}
