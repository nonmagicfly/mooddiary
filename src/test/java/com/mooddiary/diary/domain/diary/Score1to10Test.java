package com.mooddiary.diary.domain.diary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Score1to10Test {
    @Test
    void shouldAcceptMinValue() {
        Score1to10 score = Score1to10.of(1);
        assertEquals(1, score.value());
    }

    @Test
    void shouldAcceptMaxValue() {
        Score1to10 score = Score1to10.of(10);
        assertEquals(10, score.value());
    }

    @Test
    void shouldRejectBelowMin() {
        assertThrows(IllegalArgumentException.class, () -> Score1to10.of(0));
    }

    @Test
    void shouldRejectAboveMax() {
        assertThrows(IllegalArgumentException.class, () -> Score1to10.of(11));
    }
}

