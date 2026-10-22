package com.example.ui.year

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CountdownEvent
import com.example.data.model.DayStatus
import com.example.data.model.YearInfo
import com.example.ui.theme.CompletedDotColor
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.FutureDotColor
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.TodayDotRingColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DayDetailDialog(
    dayOfYear: Int,
    yearInfo: YearInfo,
    accentColor: Color,
    eventsOnDate: List<CountdownEvent>,
    onDismiss: () -> Unit,
    onCreateEvent: (LocalDate) -> Unit
) {
    val date = yearInfo.getDateForDay(dayOfYear)
    val status = yearInfo.getDayStatus(dayOfYear)
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    val daysDiff = ChronoUnit.DAYS.between(yearInfo.todayDate, date)

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220))
        ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
                .testTag("day_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    val (badgeText, badgeColor, badgeBg, badgeBorder) = when (status) {
                        DayStatus.PAST -> Quadruple("COMPLETED", TextPrimary, ElevatedSurface, OutlineMuted)
                        DayStatus.TODAY -> Quadruple("TODAY", MaterialTheme.colorScheme.onPrimary, accentColor, accentColor.copy(alpha = 0.72f))
                        DayStatus.FUTURE -> Quadruple("UPCOMING", TextSecondary, ElevatedSurface, OutlineMuted)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_day_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Title
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day count info with dot indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dotColor = when (status) {
                        DayStatus.PAST -> CompletedDotColor
                        DayStatus.TODAY -> accentColor
                        DayStatus.FUTURE -> FutureDotColor
                    }
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .then(
                                if (status == DayStatus.TODAY) {
                                    Modifier.border(1.5.dp, TodayDotRingColor, CircleShape)
                                } else Modifier
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Day $dayOfYear of ${yearInfo.totalDays}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val relativeText = when {
                    daysDiff == 0L -> "This is today!"
                    daysDiff == 1L -> "Tomorrow"
                    daysDiff == -1L -> "Yesterday"
                    daysDiff > 0L -> "$daysDiff days from now"
                    else -> "${-daysDiff} days ago"
                }

                Text(
                    text = relativeText,
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Events on this day if any
                if (eventsOnDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Events on this day:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    eventsOnDate.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElevatedSurface)
                                .border(1.dp, OutlineMuted, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = event.emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = event.title,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create Event Button in Midnight Navy with understated border
                Button(
                    onClick = {
                        onDismiss()
                        onCreateEvent(date)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("create_event_for_day_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.72f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Countdown for this Day",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
