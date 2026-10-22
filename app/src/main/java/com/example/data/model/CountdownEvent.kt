package com.example.data.model

import com.example.data.local.EventEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CountdownEvent(
    val id: Long = 0,
    val title: String,
    val targetDate: LocalDate,
    val startDate: LocalDate,
    val emoji: String = "✨",
    val accentHex: String = "#A99AFF",
    val isStarred: Boolean = false
) {
    fun daysRemaining(from: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.DAYS.between(from, targetDate)
    }

    fun isToday(from: LocalDate = LocalDate.now()): Boolean = targetDate == from

    fun isPast(from: LocalDate = LocalDate.now()): Boolean = targetDate.isBefore(from)

    fun formattedRemaining(from: LocalDate = LocalDate.now()): String {
        val diff = daysRemaining(from)
        return when {
            diff == 0L -> "Today"
            diff == 1L -> "Tomorrow"
            diff == -1L -> "Yesterday"
            diff > 1L -> "$diff days left"
            else -> "${-diff} days ago"
        }
    }

    /**
     * Calculates the progress from startDate to targetDate.
     * Returns a float between 0f and 1f.
     */
    fun progress(from: LocalDate = LocalDate.now()): Float {
        val total = ChronoUnit.DAYS.between(startDate, targetDate)
        if (total <= 0) return 1f
        val elapsed = ChronoUnit.DAYS.between(startDate, from)
        return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    fun toEntity(): EventEntity {
        return EventEntity(
            id = id,
            title = title,
            targetDateEpochDay = targetDate.toEpochDay(),
            startDateEpochDay = startDate.toEpochDay(),
            emoji = emoji,
            accentHex = accentHex,
            isStarred = isStarred
        )
    }

    companion object {
        fun fromEntity(entity: EventEntity): CountdownEvent {
            return CountdownEvent(
                id = entity.id,
                title = entity.title,
                targetDate = LocalDate.ofEpochDay(entity.targetDateEpochDay),
                startDate = LocalDate.ofEpochDay(entity.startDateEpochDay),
                emoji = entity.emoji,
                accentHex = entity.accentHex,
                isStarred = entity.isStarred
            )
        }
    }
}
