package com.mooddiary.diary.domain.diary;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiaryEntryLockRulesTest {

    @Test
    void firstThreeDaysIncludingEntryDateAreEditable() {
        LocalDate entry = LocalDate.of(2026, 5, 10);
        assertFalse(DiaryEntryLockRules.isEditLocked(entry, LocalDate.of(2026, 5, 10)));
        assertFalse(DiaryEntryLockRules.isEditLocked(entry, LocalDate.of(2026, 5, 11)));
        assertFalse(DiaryEntryLockRules.isEditLocked(entry, LocalDate.of(2026, 5, 12)));
        assertTrue(DiaryEntryLockRules.isEditLocked(entry, LocalDate.of(2026, 5, 13)));
    }
}
