package com.squishout.game.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarUtilsTest {

    @Test
    fun testEpochDayZeroIs1970Jan1Thursday() {
        val date = CalendarUtils.epochDayToDate(0L)
        assertEquals(1970, date.year)
        assertEquals(1, date.month)
        assertEquals(1, date.day)
        assertEquals(4, date.dayOfWeek) // Thursday
        assertEquals("1970-01-01", date.dateKey)
        assertEquals("1970-01", date.monthKey)
        assertEquals("JANUARY", date.monthName)
    }

    @Test
    fun testRoundTripDateToEpochDay() {
        val testDates = listOf(
            Triple(2024, 2, 29), // Leap day
            Triple(2024, 1, 1),
            Triple(2024, 12, 31),
            Triple(2026, 10, 9),
            Triple(2000, 1, 1)  // Century leap year
        )

        for ((year, month, day) in testDates) {
            val epochDay = CalendarUtils.dateToEpochDay(year, month, day)
            val roundTrip = CalendarUtils.epochDayToDate(epochDay)
            assertEquals(year, roundTrip.year, "Year mismatch for $year-$month-$day")
            assertEquals(month, roundTrip.month, "Month mismatch for $year-$month-$day")
            assertEquals(day, roundTrip.day, "Day mismatch for $year-$month-$day")
        }
    }

    @Test
    fun testDaysInMonth() {
        assertEquals(31, CalendarUtils.daysInMonth(2024, 1))
        assertEquals(29, CalendarUtils.daysInMonth(2024, 2)) // 2024 is leap
        assertEquals(28, CalendarUtils.daysInMonth(2023, 2)) // 2023 is not leap
        assertEquals(28, CalendarUtils.daysInMonth(2100, 2)) // 2100 is not leap (century)
        assertEquals(29, CalendarUtils.daysInMonth(2000, 2)) // 2000 is leap (400-year)
        assertEquals(31, CalendarUtils.daysInMonth(2024, 3))
        assertEquals(30, CalendarUtils.daysInMonth(2024, 4))
        assertEquals(31, CalendarUtils.daysInMonth(2024, 12))
    }

    @Test
    fun testFirstDayOfWeekInMonth() {
        // 2024-10-01 was a Tuesday (dayOfWeek = 2 where 0=Sun, 1=Mon, 2=Tue)
        val dow = CalendarUtils.firstDayOfWeekInMonth(2024, 10)
        assertEquals(2, dow)
    }
}
