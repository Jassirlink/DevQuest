package com.devquest

import com.devquest.common.CalendarRules
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class CalendarTest {
    @Test
    fun streakAllowsYesterdayAndIgnoresFuture() {
        val t = LocalDate.of(2026, 10, 29)
        assertEquals(
            2,
            CalendarRules.streak(setOf(t.minusDays(1), t.minusDays(2), t.plusDays(1)), t),
        )
        assertEquals(0, CalendarRules.streak(setOf(t.minusDays(2)), t))
    }

    @Test
    fun midnightUsesLocalZone() {
        val c = Clock.fixed(Instant.parse("2026-10-28T18:30:00Z"), ZoneId.of("Asia/Kolkata"))
        assertEquals("2026-10-29", CalendarRules.today(c))
        assertEquals(
            mapOf("2026-10-28" to 3, "2026-10-29" to 2),
            CalendarRules.buckets(c.instant().plusSeconds(2), 5, c.zone),
        )
    }

    @Test
    fun daylightSavingUsesCalendarDates() {
        val zone = ZoneId.of("America/New_York")
        val start = LocalDate.of(2026, 3, 8).atStartOfDay(zone).toInstant()
        val end = LocalDate.of(2026, 3, 9).atStartOfDay(zone).toInstant()
        assertEquals(23 * 3600, Duration.between(start, end).seconds.toInt())
        assertEquals(mapOf("2026-03-08" to 82800), CalendarRules.buckets(end, 82800, zone))
    }

    @Test
    fun repeatedDateDoesNotExtendStreak() {
        val d = LocalDate.of(2026, 10, 29)
        assertEquals(1, CalendarRules.streak(listOf(d, d).toSet(), d))
    }
}
