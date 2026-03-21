package com.mooddiary.diary.infrastructure.persistence.jpa.repository;

import com.mooddiary.diary.infrastructure.persistence.jpa.PhotoJpaEntity;
import com.mooddiary.diary.infrastructure.persistence.jpa.DiaryEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoJpaRepository extends JpaRepository<PhotoJpaEntity, UUID> {
    @Query("""
            select p
            from PhotoJpaEntity p
            join DiaryEntryJpaEntity d on d.id = p.entryId
            where p.id = :photoId and d.userId = :userId
            """)
    Optional<PhotoJpaEntity> findByIdAndDiaryEntryUserId(UUID photoId, UUID userId);

    @Query("""
            select p
            from PhotoJpaEntity p
            join DiaryEntryJpaEntity d on d.id = p.entryId
            where d.userId = :userId and p.entryId = :diaryEntryId
            order by p.createdAt desc
            """)
    List<PhotoJpaEntity> findByDiaryEntryIdAndUserId(UUID diaryEntryId, UUID userId);

    @Query("""
            delete from PhotoJpaEntity p
            where p.id = :photoId
            and exists (
              select 1 from DiaryEntryJpaEntity d
              where d.id = p.entryId and d.userId = :userId
            )
            """)
    int deleteByIdAndDiaryEntryUserId(UUID photoId, UUID userId);
}

