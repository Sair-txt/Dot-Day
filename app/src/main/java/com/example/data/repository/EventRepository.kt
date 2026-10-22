package com.example.data.repository

import com.example.data.local.EventDao
import com.example.data.model.CountdownEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class EventRepository(private val eventDao: EventDao) {

    val eventsFlow: Flow<List<CountdownEvent>> = eventDao.getAllEventsFlow().map { list ->
        list.map { CountdownEvent.fromEntity(it) }
    }

    suspend fun insertEvent(event: CountdownEvent): Long {
        return eventDao.insertEvent(event.toEntity())
    }

    suspend fun updateEvent(event: CountdownEvent) {
        eventDao.updateEvent(event.toEntity())
    }

    suspend fun deleteEvent(event: CountdownEvent) {
        eventDao.deleteEvent(event.toEntity())
    }

    suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(id)
    }

    suspend fun seedSampleEventsIfEmpty() {
        if (eventDao.getCount() == 0) {
            seedSampleEvents()
        }
    }

    suspend fun seedSampleEvents() {
        val now = LocalDate.now()
        val nextYear = now.year + 1

        val samples = listOf(
            CountdownEvent(
                title = "New Year $nextYear",
                targetDate = LocalDate.of(nextYear, 1, 1),
                startDate = LocalDate.of(now.year, 1, 1),
                emoji = "🎆",
                accentHex = "#A99AFF",
                isStarred = true
            ),
            CountdownEvent(
                title = "Next Birthday",
                targetDate = now.plusMonths(3).withDayOfMonth(15),
                startDate = now.minusMonths(1),
                emoji = "🎂",
                accentHex = "#FF8BA7",
                isStarred = false
            ),
            CountdownEvent(
                title = "Weekend Roadtrip",
                targetDate = now.plusWeeks(2),
                startDate = now,
                emoji = "🚗",
                accentHex = "#7AE2C0",
                isStarred = false
            ),
            CountdownEvent(
                title = "Summer Solstice",
                targetDate = if (now.isBefore(LocalDate.of(now.year, 6, 21))) {
                    LocalDate.of(now.year, 6, 21)
                } else {
                    LocalDate.of(nextYear, 6, 21)
                },
                startDate = now.minusDays(30),
                emoji = "☀️",
                accentHex = "#FFB347",
                isStarred = false
            )
        )

        for (sample in samples) {
            eventDao.insertEvent(sample.toEntity())
        }
    }
}
