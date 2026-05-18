package com.mooddiary.diary.domain.diary;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entry stays editable for three calendar days anchored on {@code entryDate} (day 0, 1, 2),
 * then is treated as locked for analytics/UI ({@code isCompleted} on the entity).
 */
public final class DiaryEntryLockRules {

    private DiaryEntryLockRules() {
    }

    public static boolean isEditLocked(LocalDate entryDate, LocalDate today) {
        if (entryDate == null || today == null) {
            return true;
        }
        long daysAfterEntry = ChronoUnit.DAYS.between(entryDate, today);
        return daysAfterEntry > 2;
    }
}
