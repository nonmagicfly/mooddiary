package com.mooddiary.diary.application.service.impl;

import com.mooddiary.diary.application.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceImplTest {
    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Test
    void shouldDelegateToUserRepositoryPort() {
        UserIdentityServiceImpl service = new UserIdentityServiceImpl(userRepositoryPort);
        UUID userId = UUID.randomUUID();
        String keycloakSubject = "sub-1";

        when(userRepositoryPort.getOrCreateByKeycloakSubject(keycloakSubject)).thenReturn(userId);

        UUID result = service.getOrCreateUserId(keycloakSubject);

        assertEquals(userId, result);
    }
}

