package com.mooddiary.diary.domain.diary;

import java.util.UUID;

public record DiaryEntryTag(UUID diaryEntryId, UUID tagId) {
}

