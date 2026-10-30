package com.devquest.common

import java.time.Clock
import java.time.LocalDate

object CalendarRules {
    fun streak(dates: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in dates) today else today.minusDays(1)
        var count = 0
        while (day in dates) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun buckets(end: java.time.Instant, seconds: Int, zone: java.time.ZoneId): Map<String, Int> {
        require(seconds >= 0)
        var cursor = end.minusSeconds(seconds.toLong())
        val result = linkedMapOf<String, Int>()
        while (cursor < end) {
            val date = cursor.atZone(zone).toLocalDate()
            val boundary = date.plusDays(1).atStartOfDay(zone).toInstant()
            val stop = minOf(boundary, end)
            val duration = java.time.Duration.between(cursor, stop).seconds.toInt()
            result[date.toString()] = (result[date.toString()] ?: 0) + duration
            cursor = stop
        }
        return result
    }

    fun today(clock: Clock): String = LocalDate.now(clock).toString()
}
