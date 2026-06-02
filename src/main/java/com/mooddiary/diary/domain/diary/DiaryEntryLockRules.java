package com.mooddiary.diary.domain.diary;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
