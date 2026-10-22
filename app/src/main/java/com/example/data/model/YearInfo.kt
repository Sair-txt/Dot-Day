package com.example.data.model

import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class DayStatus {
    PAST, TODAY, FUTURE
}

data class YearInfo(
    val year: Int,
    val isLeapYear: Boolean,
    val totalDays: Int,
    val currentDayOfYear: Int,
    val todayDate: LocalDate,
    val countTodayAsRemaining: Boolean = true
) {
    // Past days strictly before today
    val pastDaysCount: Int = (currentDayOfYear - 1).coerceAtLeast(0)

    // Remaining days
    val remainingDaysCount: Int = if (countTodayAsRemaining) {
        (totalDays - currentDayOfYear + 1).coerceAtLeast(0)
    } else {
        (totalDays - currentDayOfYear).coerceAtLeast(0)
    }

    /** Compact, uppercase count label shared by the widget and its visual checks. */
    val remainingDaysLabel: String = when (remainingDaysCount) {
        1 -> "1 DAY LEFT"
        else -> "$remainingDaysCount DAYS LEFT"
    }

    // Completed days
    val completedDaysCount: Int = if (countTodayAsRemaining) {
        pastDaysCount
    } else {
        currentDayOfYear
    }

    val progressPercent: Float = if (totalDays > 0) {
        (completedDaysCount.toFloat() / totalDays.toFloat()) * 100f
    } else 0f

    fun getDayStatus(dayOfYear: Int): DayStatus {
        return when {
            dayOfYear < currentDayOfYear -> DayStatus.PAST
            dayOfYear == currentDayOfYear -> DayStatus.TODAY
            else -> DayStatus.FUTURE
        }
    }

    fun getDateForDay(dayOfYear: Int): LocalDate {
        return LocalDate.ofYearDay(year, dayOfYear.coerceIn(1, totalDays))
    }

    companion object {
        fun current(countTodayAsRemaining: Boolean = true, now: LocalDate = LocalDate.now()): YearInfo {
            val year = now.year
            val isLeap = Year.isLeap(year.toLong())
            val totalDays = if (isLeap) 366 else 365
            val dayOfYear = now.dayOfYear

            return YearInfo(
                year = year,
                isLeapYear = isLeap,
                totalDays = totalDays,
                currentDayOfYear = dayOfYear,
                todayDate = now,
                countTodayAsRemaining = countTodayAsRemaining
            )
        }
    }
}
