package com.example.ui.year

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountdownEvent
import com.example.data.model.GridDensity
import com.example.data.model.YearInfo
import com.example.ui.theme.Background
import com.example.ui.theme.CompletedDotColor
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.FutureDotColor
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.TodayDotRingColor
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun YearInDotsScreen(
    yearInfo: YearInfo,
    accentColor: Color,
    density: GridDensity,
    events: List<CountdownEvent>,
    onDotClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Full Grid, 1 = By Month
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val todayFormatted = yearInfo.todayDate.format(dateFormatter)
    val animatedProgress by animateFloatAsState(
        targetValue = (yearInfo.progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 760, easing = FastOutSlowInEasing),
        label = "yearProgress"
    )
    val animatedProgressPercent = animatedProgress * 100f

    val eventDaysOfYear = remember(events, yearInfo.year) {
        events
            .filter { it.targetDate.year == yearInfo.year }
            .map { it.targetDate.dayOfYear }
            .toSet()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF151720), Background, Background)))
            .testTag("year_in_dots_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dot Day",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Leap year chip
                    if (yearInfo.isLeapYear) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElevatedSurface)
                                .border(1.dp, OutlineMuted, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LEAP YEAR (366 DAYS)",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Hero Stats Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .animateContentSize()
                    .testTag("year_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${yearInfo.year}",
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "%.1f%%", animatedProgressPercent),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "of year elapsed",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar: completed near-white #F5F5F7, track charcoal #3D3E45
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (accentColor.luminance() > 0.18f) accentColor else CompletedDotColor,
                        trackColor = FutureDotColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detailed counts row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            number = "${yearInfo.remainingDaysCount}",
                            label = if (yearInfo.countTodayAsRemaining) "Days Left (inc. today)" else "Days Left",
                            accentColor = accentColor
                        )

                        StatItem(
                            number = "${yearInfo.currentDayOfYear} / ${yearInfo.totalDays}",
                            label = "Day of Year",
                            accentColor = TextSecondary
                        )

                        StatItem(
                            number = "${yearInfo.pastDaysCount}",
                            label = "Days Past",
                            accentColor = TextSecondary
                        )
                    }
                }
            }
        }

        // Legend Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = CompletedDotColor, label = "Past days")
                Spacer(modifier = Modifier.width(18.dp))
                LegendItem(
                    color = accentColor,
                    label = "Today (Day ${yearInfo.currentDayOfYear})",
                    ringColor = TodayDotRingColor
                )
                Spacer(modifier = Modifier.width(18.dp))
                LegendItem(color = FutureDotColor, label = "Future")
            }
        }

        // View Mode Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) "ALL ${yearInfo.totalDays} DAYS IN DOTS" else "12 MONTHS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                val gridTabFill by animateColorAsState(
                    targetValue = if (selectedTab == 0) accentColor else Color.Transparent,
                    animationSpec = tween(180),
                    label = "gridTabFill"
                )
                val monthsTabFill by animateColorAsState(
                    targetValue = if (selectedTab == 1) accentColor else Color.Transparent,
                    animationSpec = tween(180),
                    label = "monthsTabFill"
                )
                Row(
                    modifier = Modifier
                        .animateContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElevatedSurface)
                        .border(1.dp, OutlineMuted, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(gridTabFill)
                            .then(
                                if (selectedTab == 0) Modifier.border(1.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("tab_full_grid")
                    ) {
                        Text(
                            text = "Grid",
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(monthsTabFill)
                            .then(
                                if (selectedTab == 1) Modifier.border(1.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("tab_months")
                    ) {
                        Text(
                            text = "Months",
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Grid content with a subtle directional transition between full-year and month views.
        item {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically(tween(250)) { height -> height / 18 }) togetherWith
                        (fadeOut(tween(130)) + slideOutVertically(tween(180)) { height -> -height / 24 })
                },
                label = "yearGridModeTransition"
            ) { tab ->
                if (tab == 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Tap a dot to explore that day",
                                color = TextTertiary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                            )

                            YearDotsCanvas(
                                yearInfo = yearInfo,
                                accentColor = accentColor,
                                density = density,
                                eventDaysOfYear = eventDaysOfYear,
                                onDotClicked = onDotClicked
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        MonthDotsView(
                            yearInfo = yearInfo,
                            accentColor = accentColor,
                            eventDaysOfYear = eventDaysOfYear,
                            onDotClicked = onDotClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    number: String,
    label: String,
    accentColor: Color
) {
    Column {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    ringColor: Color? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (ringColor != null) Modifier.border(1.2.dp, ringColor, CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
