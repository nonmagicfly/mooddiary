package com.mooddiary.diary.application.diary;

import java.util.UUID;

public record DiaryEntryDeleteResult(
        UUID userId,
        UUID diaryEntryId
) {
}

