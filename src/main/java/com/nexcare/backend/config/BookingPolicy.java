package com.nexcare.backend.config;

/**
 * Shared appointment-booking limits used by availability discovery and
 * booking validation. The window includes today, so five calendar days means
 * today through today + 4 days in the business time zone.
 */
public final class BookingPolicy {

    public static final int BOOKING_WINDOW_DAYS = 5;

    public static final long MAXIMUM_ADVANCE_DAYS =
            BOOKING_WINDOW_DAYS - 1L;

    public static final String BOOKING_WINDOW_DAYS_TEXT = "5";

    private BookingPolicy() {
    }
}
