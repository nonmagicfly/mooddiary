package com.mooddiary.diary.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "diary_entries")
public class DiaryEntryJpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "mood_score", nullable = false)
    private short moodScore;

    @Column(name = "energy_score", nullable = false)
    private short energyScore;

    @Column(name = "productivity_score", nullable = false)
    private short productivityScore;

    @Column(name = "stress_score", nullable = false)
    private short stressScore;

    @Column(name = "sleep_quality_score", nullable = false)
    private short sleepQualityScore;

    @Column(name = "note")
    private String note;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "diary_entry_tags",
            joinColumns = @JoinColumn(name = "diary_entry_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id")
    )
    private Set<TagJpaEntity> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "diary_entry_symptoms",
            joinColumns = @JoinColumn(name = "diary_entry_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "symptom_id", referencedColumnName = "id")
    )
    private Set<SymptomJpaEntity> symptoms = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public short getMoodScore() {
        return moodScore;
    }

    public short getEnergyScore() {
        return energyScore;
    }

    public short getProductivityScore() {
        return productivityScore;
    }

    public short getStressScore() {
        return stressScore;
    }

    public short getSleepQualityScore() {
        return sleepQualityScore;
    }

    public String getNote() {
        return note;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<TagJpaEntity> getTags() {
        return tags;
    }

    public Set<SymptomJpaEntity> getSymptoms() {
        return symptoms;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setMoodScore(short moodScore) {
        this.moodScore = moodScore;
    }

    public void setEnergyScore(short energyScore) {
        this.energyScore = energyScore;
    }

    public void setProductivityScore(short productivityScore) {
        this.productivityScore = productivityScore;
    }

    public void setStressScore(short stressScore) {
        this.stressScore = stressScore;
    }

    public void setSleepQualityScore(short sleepQualityScore) {
        this.sleepQualityScore = sleepQualityScore;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setTags(Set<TagJpaEntity> tags) {
        this.tags = tags;
    }

    public void setSymptoms(Set<SymptomJpaEntity> symptoms) {
        this.symptoms = symptoms;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

