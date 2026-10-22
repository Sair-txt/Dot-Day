package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateEpochDay: Long, // LocalDate.toEpochDay()
    val startDateEpochDay: Long,  // For progress calculation
    val emoji: String = "✨",
    val accentHex: String = "#A99AFF",
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
