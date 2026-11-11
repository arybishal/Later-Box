package com.laterbox.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class EventRecurrenceCalculatorTest {

    private val tz = TimeZone.getTimeZone("UTC")

    @Test
    fun `test LeapYear detection`() {
        assertTrue(EventRecurrenceCalculator.isLeapYear(2024))
        assertTrue(EventRecurrenceCalculator.isLeapYear(2028))
        assertTrue(EventRecurrenceCalculator.isLeapYear(2000))
        assertFalse(EventRecurrenceCalculator.isLeapYear(2025))
        assertFalse(EventRecurrenceCalculator.isLeapYear(2026))
        assertFalse(EventRecurrenceCalculator.isLeapYear(2027))
        assertFalse(EventRecurrenceCalculator.isLeapYear(1900))
    }

    @Test
    fun `test Birthday turning age and next occurrence rollover`() {
        // Born March 15, 1995 at 09:00 UTC
        val birthCal = Calendar.getInstance(tz).apply {
            set(1995, Calendar.MARCH, 15, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val birthMs = birthCal.timeInMillis

        // Reference: November 11, 2026 (after March 15 in 2026)
        val refCal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 11, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refMs = refCal.timeInMillis

        val result = EventRecurrenceCalculator.calculateNextOccurrence(birthMs, refMs, tz)

        // Next occurrence should be March 15, 2027
        val nextCal = Calendar.getInstance(tz).apply { timeInMillis = result.nextOccurrenceEpochMs }
        assertEquals(2027, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, nextCal.get(Calendar.MONTH))
        assertEquals(15, nextCal.get(Calendar.DAY_OF_MONTH))

        // Age turning in 2027: 2027 - 1995 = 32
        assertEquals(32, result.turningAgeOrYears)
        assertFalse(result.isToday)
        assertTrue(result.daysRemaining > 0)
    }

    @Test
    fun `test Birthday occurrence on the actual day`() {
        val birthCal = Calendar.getInstance(tz).apply {
            set(1995, Calendar.MARCH, 15, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val birthMs = birthCal.timeInMillis

        // Reference: March 15, 2027 at 12:00 UTC
        val refCal = Calendar.getInstance(tz).apply {
            set(2027, Calendar.MARCH, 15, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refMs = refCal.timeInMillis

        val result = EventRecurrenceCalculator.calculateNextOccurrence(birthMs, refMs, tz)
        assertTrue(result.isToday)
        assertEquals(0, result.daysRemaining)
        assertEquals("Today!", result.formattedStatus)
        assertEquals(32, result.turningAgeOrYears)
    }

    @Test
    fun `test February 29 in non-leap year resolves to February 28`() {
        // Born February 29, 2000
        val birthCal = Calendar.getInstance(tz).apply {
            set(2000, Calendar.FEBRUARY, 29, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val birthMs = birthCal.timeInMillis

        // Reference: January 10, 2027 (2027 is NOT a leap year)
        val refCal = Calendar.getInstance(tz).apply {
            set(2027, Calendar.JANUARY, 10, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refMs = refCal.timeInMillis

        val result = EventRecurrenceCalculator.calculateNextOccurrence(birthMs, refMs, tz)
        val nextCal = Calendar.getInstance(tz).apply { timeInMillis = result.nextOccurrenceEpochMs }

        assertEquals(2027, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, nextCal.get(Calendar.MONTH))
        assertEquals(28, nextCal.get(Calendar.DAY_OF_MONTH)) // Policy: 28 Feb in non-leap years
        assertEquals(27, result.turningAgeOrYears)
    }

    @Test
    fun `test February 29 in leap year resolves to February 29`() {
        // Born February 29, 2000
        val birthCal = Calendar.getInstance(tz).apply {
            set(2000, Calendar.FEBRUARY, 29, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val birthMs = birthCal.timeInMillis

        // Reference: January 10, 2028 (2028 IS a leap year)
        val refCal = Calendar.getInstance(tz).apply {
            set(2028, Calendar.JANUARY, 10, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refMs = refCal.timeInMillis

        val result = EventRecurrenceCalculator.calculateNextOccurrence(birthMs, refMs, tz)
        val nextCal = Calendar.getInstance(tz).apply { timeInMillis = result.nextOccurrenceEpochMs }

        assertEquals(2028, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, nextCal.get(Calendar.MONTH))
        assertEquals(29, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(28, result.turningAgeOrYears)
    }

    @Test
    fun `test Anniversary milestone calculation`() {
        // Anniversary June 20, 2018
        val annCal = Calendar.getInstance(tz).apply {
            set(2018, Calendar.JUNE, 20, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val annMs = annCal.timeInMillis

        // Reference: July 1, 2026 (after June 20, 2026)
        val refCal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.JULY, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val refMs = refCal.timeInMillis

        val result = EventRecurrenceCalculator.calculateNextOccurrence(annMs, refMs, tz)
        val nextCal = Calendar.getInstance(tz).apply { timeInMillis = result.nextOccurrenceEpochMs }

        assertEquals(2027, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, nextCal.get(Calendar.MONTH))
        assertEquals(20, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, result.turningAgeOrYears) // 9th Anniversary in 2027
    }
}
