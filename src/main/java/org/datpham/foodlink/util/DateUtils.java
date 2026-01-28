package org.datpham.foodlink.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DateUtils {

    private DateUtils() {
    }

    // TODO: Add date/time helpers used across the project.
    public static LocalDate today() {
        return LocalDate.now(Clock.systemUTC());
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(Clock.systemUTC());
    }
}
