package com.mooddiary.diary.application.tag;

public record TagUpdateCommand(
        String name,
        String color
) {
}

