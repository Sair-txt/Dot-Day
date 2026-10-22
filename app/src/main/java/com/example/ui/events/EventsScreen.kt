package com.example.ui.events

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountdownEvent
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.FutureDotColor
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EventsScreen(
    events: List<CountdownEvent>,
    accentColor: Color,
    onAddClicked: () -> Unit,
    onEditClicked: (CountdownEvent) -> Unit,
    onDeleteClicked: (CountdownEvent) -> Unit,
    onToggleStarred: (CountdownEvent) -> Unit,
    onSeedSamples: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Upcoming, 2 = Past
    var eventToDelete by remember { mutableStateOf<CountdownEvent?>(null) }
    val now = LocalDate.now()

    val filteredEvents = remember(events, selectedFilter, now) {
        when (selectedFilter) {
            1 -> events.filter { !it.isPast(now) }
            2 -> events.filter { it.isPast(now) }
            else -> events
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF151720), DeepCharcoal, DeepCharcoal)))
            .testTag("events_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp, top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Countdowns",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        val activeCount = events.count { !it.isPast(now) }
                        AnimatedContent(
                            targetState = activeCount,
                            transitionSpec = { (fadeIn(tween(160)) + scaleIn(initialScale = 0.98f, animationSpec = tween(160))) togetherWith
                                (fadeOut(tween(100)) + scaleOut(targetScale = 1.02f, animationSpec = tween(100))) },
                            label = "activeCountdownCount"
                        ) { count ->
                            Text(
                                text = "$count active ${if (count == 1) "countdown" else "countdowns"}",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Add Button in Header
                    Button(
                        onClick = onAddClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.72f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("header_add_event_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "New", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "All (${events.size})",
                        isSelected = selectedFilter == 0,
                        accentColor = accentColor,
                        onClick = { selectedFilter = 0 },
                        testTag = "filter_all"
                    )
                    FilterChip(
                        label = "Upcoming",
                        isSelected = selectedFilter == 1,
                        accentColor = accentColor,
                        onClick = { selectedFilter = 1 },
                        testTag = "filter_upcoming"
                    )
                    FilterChip(
                        label = "Past",
                        isSelected = selectedFilter == 2,
                        accentColor = accentColor,
                        onClick = { selectedFilter = 2 },
                        testTag = "filter_past"
                    )
                }
            }

            // Empty state
            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⏳",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Countdowns Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Track birthdays, holidays, trips, and important life milestones with visual dots.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onAddClicked,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.72f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("empty_add_event_button")
                                ) {
                                    Text("Create Event", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onSeedSamples,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SurfaceElevated,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("empty_seed_events_button")
                                ) {
                                    Text("Load Samples")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        accentColor = accentColor,
                        now = now,
                        onEdit = { onEditClicked(event) },
                        onDelete = { eventToDelete = event },
                        onToggleStarred = { onToggleStarred(event) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete Countdown?", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete \"${event.title}\"? This cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClicked(event)
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val chipFill by animateColorAsState(
        targetValue = if (isSelected) accentColor else SurfaceCard,
        animationSpec = tween(180),
        label = "filterChipFill"
    )
    val chipStroke by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.82f) else OutlineMuted,
        animationSpec = tween(180),
        label = "filterChipStroke"
    )
    Box(
        modifier = Modifier
            .animateContentSize(spring(dampingRatio = 0.86f, stiffness = 520f))
            .clip(RoundedCornerShape(12.dp))
            .background(chipFill)
            .border(1.dp, chipStroke, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EventCard(
    event: CountdownEvent,
    accentColor: Color,
    now: LocalDate,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStarred: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val eventAccent = try {
        Color(android.graphics.Color.parseColor(event.accentHex))
    } catch (e: Exception) {
        accentColor
    }

    val daysRemaining = event.daysRemaining(now)
    val isPast = event.isPast(now)
    val isToday = event.isToday(now)
    val cardBorder by animateColorAsState(
        targetValue = if (event.isStarred) accentColor.copy(alpha = 0.82f) else OutlineMuted,
        animationSpec = tween(220),
        label = "eventCardBorder"
    )
    val starScale by animateFloatAsState(
        targetValue = if (event.isStarred) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 520f),
        label = "eventStarScale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(dampingRatio = 0.88f, stiffness = 500f))
            .testTag("event_card_${event.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Card Top Row: Emoji + Title + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(eventAccent.copy(alpha = 0.14f))
                            .border(1.dp, eventAccent.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = event.emoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (event.isStarred) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Pinned",
                                    tint = Color(0xFFFFB347),
                                    modifier = Modifier.size(16.dp).graphicsLayer { scaleX = starScale; scaleY = starScale }
                                )
                            }
                        }
                        Text(
                            text = event.targetDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp).testTag("event_options_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (event.isStarred) "Unpin" else "Pin to top", color = TextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (event.isStarred) Icons.Outlined.StarBorder else Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB347)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleStarred()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit", color = TextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFEF5350)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Countdown Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    val statusText = when {
                        isToday -> "TODAY!"
                        daysRemaining == 1L -> "1 DAY LEFT"
                        daysRemaining > 1L -> "$daysRemaining DAYS LEFT"
                        daysRemaining == -1L -> "1 DAY AGO"
                        else -> "${-daysRemaining} DAYS AGO"
                    }
                    val countColor = when {
                        isToday -> Color.White
                        isPast -> TextTertiary
                        event.accentHex.equals("#191F45", ignoreCase = true) -> TextPrimary
                        else -> eventAccent
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = countColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Dot journey: visual progress dots
                val progress = event.progress(now).coerceIn(0f, 1f)
                val animatedEventProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 520),
                    label = "eventProgressDots"
                )
                val totalProgressDots = 10
                val filledDots = (animatedEventProgress * totalProgressDots).toInt().coerceIn(0, totalProgressDots)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fillDotColor = if (event.accentHex.equals("#191F45", ignoreCase = true)) {
                        com.example.ui.theme.CompletedDotColor
                    } else {
                        eventAccent
                    }
                    for (i in 0 until totalProgressDots) {
                        val isFilled = i < filledDots
                        val progressDotColor by animateColorAsState(
                            targetValue = if (isFilled) fillDotColor else FutureDotColor,
                            animationSpec = tween(260),
                            label = "eventProgressDot$i"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(progressDotColor)
                        )
                    }
                }
            }
        }
    }
}
