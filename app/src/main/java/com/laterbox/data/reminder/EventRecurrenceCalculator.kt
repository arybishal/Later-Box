package com.laterbox.data.reminder

import java.util.Calendar
import java.util.TimeZone

data class RecurrenceCalculationResult(
    val nextOccurrenceEpochMs: Long,
    val daysRemaining: Int,
    val turningAgeOrYears: Int?,
    val isToday: Boolean,
    val isTomorrow: Boolean,
    val formattedStatus: String
)

object EventRecurrenceCalculator {

    /**
     * Determines whether a given year is a leap year according to the Gregorian calendar.
     */
    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Calculates the next upcoming occurrence for an annual recurring event (Birthday or Anniversary)
     * based on the stored original date and a reference timestamp (default now).
     *
     * Non-leap-year policy for February 29:
     * When a Birthday or Anniversary originated on February 29, in non-leap years it occurs on February 28.
     * In leap years, it occurs on February 29.
     */
    fun calculateNextOccurrence(
        originalDateMs: Long,
        referenceTimeMs: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): RecurrenceCalculationResult {
        val origCal = Calendar.getInstance(timeZone).apply { timeInMillis = originalDateMs }
        val refCal = Calendar.getInstance(timeZone).apply { timeInMillis = referenceTimeMs }

        val origYear = origCal.get(Calendar.YEAR)
        val origMonth = origCal.get(Calendar.MONTH) // 0-based, February is Calendar.FEBRUARY (1)
        val origDay = origCal.get(Calendar.DAY_OF_MONTH)
        val origHour = origCal.get(Calendar.HOUR_OF_DAY)
        val origMinute = origCal.get(Calendar.MINUTE)

        val refYear = refCal.get(Calendar.YEAR)

        // Evaluate candidate in the current reference year
        val candidateCal = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, refYear)
            set(Calendar.MONTH, origMonth)
            val dayToSet = if (origMonth == Calendar.FEBRUARY && origDay == 29 && !isLeapYear(refYear)) {
                28 // Non-leap year policy: February 28
            } else {
                origDay
            }
            set(Calendar.DAY_OF_MONTH, dayToSet)
            set(Calendar.HOUR_OF_DAY, origHour)
            set(Calendar.MINUTE, origMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If today is the event date, consider today as the active occurrence
        val isCandidateSameDay = isSameDay(candidateCal, refCal)
        val finalYear = if (candidateCal.timeInMillis < referenceTimeMs && !isCandidateSameDay) {
            // Already passed earlier this year, rolls over to next year
            refYear + 1
        } else {
            refYear
        }

        val nextCal = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, finalYear)
            set(Calendar.MONTH, origMonth)
            val dayToSet = if (origMonth == Calendar.FEBRUARY && origDay == 29 && !isLeapYear(finalYear)) {
                28
            } else {
                origDay
            }
            set(Calendar.DAY_OF_MONTH, dayToSet)
            set(Calendar.HOUR_OF_DAY, origHour)
            set(Calendar.MINUTE, origMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextOccurrenceMs = nextCal.timeInMillis
        val turningAge = if (origYear > 1800 && finalYear >= origYear) {
            finalYear - origYear
        } else null

        val daysRemaining = calculateCalendarDaysBetween(refCal, nextCal)
        val isToday = daysRemaining == 0
        val isTomorrow = daysRemaining == 1

        val status = when {
            isToday -> "Today!"
            isTomorrow -> "Tomorrow"
            daysRemaining == 7 -> "In 1 week"
            daysRemaining == 14 -> "In 2 weeks"
            daysRemaining == 30 -> "In 30 days"
            else -> "In $daysRemaining days"
        }

        return RecurrenceCalculationResult(
            nextOccurrenceEpochMs = nextOccurrenceMs,
            daysRemaining = daysRemaining,
            turningAgeOrYears = turningAge,
            isToday = isToday,
            isTomorrow = isTomorrow,
            formattedStatus = status
        )
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun calculateCalendarDaysBetween(startCal: Calendar, endCal: Calendar): Int {
        val s = (startCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val e = (endCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = e.timeInMillis - s.timeInMillis
        return (diffMs / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
    }
}
