package com.mooddiary.diary.infrastructure.persistence.jpa.analytics;

import java.util.UUID;

public class TagFrequencyAnalyticsRow {
    private final UUID tagId;
    private final String tagName;
    private final String tagColor;
    private final long count;

    public TagFrequencyAnalyticsRow(UUID tagId, String tagName, String tagColor, long count) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.tagColor = tagColor;
        this.count = count;
    }

    public UUID getTagId() {
        return tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagColor() {
        return tagColor;
    }

    public long getCount() {
        return count;
    }
}

