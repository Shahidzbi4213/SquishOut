package com.squishout.game.util

data class CalendarDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int,   // 1..31
    val dayOfWeek: Int // 0=Sunday, 1=Monday, ..., 6=Saturday
) {
    val monthKey: String get() = "$year-${month.toString().padStart(2, '0')}"
    val dateKey: String get() = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    val monthName: String get() = when (month) {
        1 -> "JANUARY"
        2 -> "FEBRUARY"
        3 -> "MARCH"
        4 -> "APRIL"
        5 -> "MAY"
        6 -> "JUNE"
        7 -> "JULY"
        8 -> "AUGUST"
        9 -> "SEPTEMBER"
        10 -> "OCTOBER"
        11 -> "NOVEMBER"
        12 -> "DECEMBER"
        else -> "MONTH"
    }
}

object CalendarUtils {
    fun epochDayToDate(epochDay: Long): CalendarDate {
        val z = epochDay + 719468L
        val era = (if (z >= 0) z else z - 146096L) / 146097L
        val doe = (z - era * 146097L).toInt()
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = (yoe + era * 400).toInt()
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = if (mp < 10) mp + 3 else mp - 9
        val year = if (m <= 2) y + 1 else y
        val dow = (((epochDay + 4) % 7 + 7) % 7).toInt()
        return CalendarDate(year = year, month = m, day = d, dayOfWeek = dow)
    }

    fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    fun firstDayOfWeekInMonth(year: Int, month: Int): Int {
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 9 else month - 3
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * m + 2) / 5
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val epochDay = era * 146097L + doe - 719468L
        return (((epochDay + 4) % 7 + 7) % 7).toInt()
    }

    fun dateToEpochDay(year: Int, month: Int, day: Int): Long {
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 9 else month - 3
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * m + 2) / 5 + (day - 1)
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097L + doe - 719468L
    }

    fun currentEpochDay(epochMs: Long = currentTimeMillis()): Long {
        return epochMs / (24 * 60 * 60 * 1000L)
    }
}
