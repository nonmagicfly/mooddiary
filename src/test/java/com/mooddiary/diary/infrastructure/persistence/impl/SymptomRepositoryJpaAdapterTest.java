package com.mooddiary.diary.infrastructure.persistence.impl;

import com.mooddiary.diary.domain.symptom.Symptom;
import com.mooddiary.diary.infrastructure.persistence.jpa.SymptomJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.repository.SymptomJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class SymptomRepositoryJpaAdapterTest {
    @Mock
    private SymptomJpaRepository symptomJpaRepository;

    @Test
    void shouldDelegateExistsByIdAndUserId() {
        SymptomRepositoryJpaAdapter adapter = new SymptomRepositoryJpaAdapter(symptomJpaRepository);

        UUID symptomId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(symptomJpaRepository.existsByIdAndUserId(symptomId, userId)).thenReturn(false);

        assertEquals(false, adapter.existsByIdAndUserId(symptomId, userId));
    }

    @Test
    void shouldDelegateExistsByUserIdAndName() {
        SymptomRepositoryJpaAdapter adapter = new SymptomRepositoryJpaAdapter(symptomJpaRepository);

        UUID userId = UUID.randomUUID();
        when(symptomJpaRepository.existsByUserIdAndName(userId, "pain")).thenReturn(true);

        assertEquals(true, adapter.existsByUserIdAndName(userId, "pain"));
    }

    @Test
    void shouldFindAllByUserIdMapping() {
        SymptomRepositoryJpaAdapter adapter = new SymptomRepositoryJpaAdapter(symptomJpaRepository);

        UUID userId = UUID.randomUUID();
        SymptomJpaEntity e1 = new SymptomJpaEntity();
        e1.setId(UUID.randomUUID());
        e1.setUserId(userId);
        e1.setName("pain");
        e1.setCreatedAt(Instant.now().minusSeconds(3600));
        e1.setUpdatedAt(Instant.now().minusSeconds(60));

        when(symptomJpaRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(e1));

        List<Symptom> result = adapter.findAllByUserId(userId);
        assertEquals(1, result.size());
        assertEquals("pain", result.get(0).getName());
    }

    @Test
    void shouldSaveMapping() {
        SymptomRepositoryJpaAdapter adapter = new SymptomRepositoryJpaAdapter(symptomJpaRepository);

        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now().minusSeconds(60);

        Symptom domain = new Symptom(symptomId, userId, "pain", createdAt, updatedAt);

        SymptomJpaEntity savedEntity = new SymptomJpaEntity();
        savedEntity.setId(symptomId);
        savedEntity.setUserId(userId);
        savedEntity.setName("pain");
        savedEntity.setCreatedAt(createdAt);
        savedEntity.setUpdatedAt(updatedAt);

        when(symptomJpaRepository.save(any(SymptomJpaEntity.class))).thenReturn(savedEntity);

        Symptom result = adapter.save(domain);

        ArgumentCaptor<SymptomJpaEntity> captor = ArgumentCaptor.forClass(SymptomJpaEntity.class);
        verify(symptomJpaRepository).save(captor.capture());

        assertEquals(symptomId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals("pain", result.getName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void shouldDelegateDeleteByIdAndUserId() {
        SymptomRepositoryJpaAdapter adapter = new SymptomRepositoryJpaAdapter(symptomJpaRepository);

        UUID userId = UUID.randomUUID();
        UUID symptomId = UUID.randomUUID();
        when(symptomJpaRepository.deleteByIdAndUserId(symptomId, userId)).thenReturn(1L);

        assertEquals(true, adapter.deleteByIdAndUserId(symptomId, userId));
    }
}

