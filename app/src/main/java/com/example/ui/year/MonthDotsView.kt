package com.example.ui.year

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.DayStatus
import com.example.data.model.YearInfo
import com.example.ui.theme.CompletedDotColor
import com.example.ui.theme.FutureDotColor
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TodayDotRingColor
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthDotsView(
    yearInfo: YearInfo,
    accentColor: Color,
    eventDaysOfYear: Set<Int>,
    onDotClicked: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (monthIndex in 1..12) {
            val month = Month.of(monthIndex)
            val monthLength = month.length(yearInfo.isLeapYear)
            val firstDayOfYearInMonth = java.time.LocalDate.of(yearInfo.year, monthIndex, 1).dayOfYear

            val isCurrentMonth = yearInfo.todayDate.monthValue == monthIndex
            val isPastMonth = yearInfo.todayDate.monthValue > monthIndex

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("month_card_$monthIndex")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            color = TextPrimary,
                            fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp
                        )

                        val statusLabel = when {
                            isPastMonth -> "Completed"
                            isCurrentMonth -> "Current month"
                            else -> "Upcoming"
                        }
                        Text(
                            text = statusLabel,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Flow of dots for the month
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (dayInMonth in 1..monthLength) {
                            val dayOfYear = firstDayOfYearInMonth + dayInMonth - 1
                            val status = yearInfo.getDayStatus(dayOfYear)
                            val hasEvent = eventDaysOfYear.contains(dayOfYear)

                            val dotColor = when (status) {
                                DayStatus.PAST -> CompletedDotColor
                                DayStatus.TODAY -> accentColor
                                DayStatus.FUTURE -> FutureDotColor
                            }

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .then(
                                        if (status == DayStatus.TODAY) {
                                            Modifier.border(1.5.dp, TodayDotRingColor, CircleShape)
                                        } else if (hasEvent) {
                                            val ringColor = if (accentColor == com.example.ui.theme.PrimaryNavy) com.example.ui.theme.PrimaryNavyLight else accentColor
                                            Modifier.border(1.2.dp, ringColor, CircleShape)
                                        } else Modifier
                                    )
                                    .clickable { onDotClicked(dayOfYear) },
                                contentAlignment = Alignment.Center
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
