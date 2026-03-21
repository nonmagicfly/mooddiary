package com.mooddiary.diary.application.analytics;

import java.util.UUID;

public record TagFrequencyAnalytics(
        UUID tagId,
        String tagName,
        String tagColor,
        long count
) {
}

