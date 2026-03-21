package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.infrastructure.persistence.jpa.UserJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryJpaAdapterTest {
    @Mock
    private UserJpaRepository userJpaRepository;

    @Test
    void shouldReturnExistingUserId() {
        UserRepositoryJpaAdapter adapter = new UserRepositoryJpaAdapter(userJpaRepository);

        UUID id = UUID.randomUUID();
        String keycloakSubject = "sub-1";

        UserJpaEntity existing = new UserJpaEntity();
        existing.setId(id);
        existing.setKeycloakSubject(keycloakSubject);
        existing.setCreatedAt(Instant.now());
        existing.setUpdatedAt(Instant.now());

        when(userJpaRepository.findByKeycloakSubject(keycloakSubject)).thenReturn(Optional.of(existing));

        UUID result = adapter.getOrCreateByKeycloakSubject(keycloakSubject);

        assertEquals(id, result);
        verify(userJpaRepository, never()).save(any(UserJpaEntity.class));
    }

    @Test
    void shouldCreateUserWhenMissing() {
        UserRepositoryJpaAdapter adapter = new UserRepositoryJpaAdapter(userJpaRepository);

        String keycloakSubject = "sub-1";
        when(userJpaRepository.findByKeycloakSubject(keycloakSubject)).thenReturn(Optional.empty());

        ArgumentCaptor<UserJpaEntity> captor = ArgumentCaptor.forClass(UserJpaEntity.class);
        when(userJpaRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        UUID result = adapter.getOrCreateByKeycloakSubject(keycloakSubject);

        verify(userJpaRepository).save(any(UserJpaEntity.class));
        assertNotNull(result);
        assertEquals(keycloakSubject, captor.getValue().getKeycloakSubject());
        assertNotNull(captor.getValue().getId());
        assertNotNull(captor.getValue().getCreatedAt());
    }
}

