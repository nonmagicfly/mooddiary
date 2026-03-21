package com.mooddiary.diary.domain.diary;

public record Score1to10(int value) {
    public Score1to10 {
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException("Score must be in range 1..10");
        }
    }

    public static Score1to10 of(int value) {
        return new Score1to10(value);
    }
}

