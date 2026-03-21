package com.mooddiary.diary.application.diary;

import java.time.LocalDate;
import java.util.UUID;

public record DiaryEntryListQuery(
        LocalDate from,
        LocalDate to,
        int limit
) {
}

