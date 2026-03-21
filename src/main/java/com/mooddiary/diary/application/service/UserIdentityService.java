package com.mooddiary.diary.application.service;

import java.util.UUID;

public interface UserIdentityService {
    UUID getOrCreateUserId(String keycloakSubject);
}

