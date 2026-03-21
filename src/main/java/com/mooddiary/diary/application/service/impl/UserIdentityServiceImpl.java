package com.mooddiary.diary.application.service.impl;

import com.mooddiary.diary.application.port.out.UserRepositoryPort;
import com.mooddiary.diary.application.service.UserIdentityService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserIdentityServiceImpl implements UserIdentityService {
    private final UserRepositoryPort userRepositoryPort;

    public UserIdentityServiceImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public UUID getOrCreateUserId(String keycloakSubject) {
        return userRepositoryPort.getOrCreateByKeycloakSubject(keycloakSubject);
    }
}

