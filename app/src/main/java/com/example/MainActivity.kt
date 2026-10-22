package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.events.AddEditEventDialog
import com.example.ui.events.EventsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.DotDayTheme
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.year.DayDetailDialog
import com.example.ui.year.YearInDotsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val yearInfo by viewModel.yearInfo.collectAsState()
            val events by viewModel.events.collectAsState()
            val selectedDayOfYear by viewModel.selectedDayOfYear.collectAsState()
            val isAddEventOpen by viewModel.isAddEventDialogOpen.collectAsState()
            val editingEvent by viewModel.editingEvent.collectAsState()
            val prefilledDate by viewModel.prefilledDateForNewEvent.collectAsState()
            val widgetRefreshStatus by viewModel.widgetRefreshStatus.collectAsState()

            val accentColor = try {
                Color(android.graphics.Color.parseColor(settings.accentHex))
            } catch (e: Exception) {
                PrimaryNavy
            }

            var currentTab by rememberSaveable { mutableIntStateOf(0) }

            // Handle system back navigation: return to Year tab if on other tabs
            BackHandler(enabled = currentTab != 0) {
                currentTab = 0
            }

            DotDayTheme(accentColor = accentColor) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    containerColor = DeepCharcoal,
                    bottomBar = {
                        DotDayBottomBar(
                            currentTab = currentTab,
                            accentColor = accentColor,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                val direction = if (targetState >= initialState) 1 else -1
                                (fadeIn(animationSpec = tween(220)) +
                                    slideInHorizontally(animationSpec = tween(280)) { fullWidth -> fullWidth / 18 * direction }) togetherWith
                                    (fadeOut(animationSpec = tween(140)) +
                                        slideOutHorizontally(animationSpec = tween(220)) { fullWidth -> -fullWidth / 24 * direction })
                            },
                            label = "premiumTabTransition"
                        ) { tab ->
                            when (tab) {
                                0 -> YearInDotsScreen(
                                    yearInfo = yearInfo,
                                    accentColor = accentColor,
                                    density = settings.gridDensity,
                                    events = events,
                                    onDotClicked = { day ->
                                        viewModel.selectDay(day)
                                    }
                                )
                                1 -> EventsScreen(
                                    events = events,
                                    accentColor = accentColor,
                                    onAddClicked = { viewModel.openAddEventDialog() },
                                    onEditClicked = { event -> viewModel.openEditEventDialog(event) },
                                    onDeleteClicked = { event -> viewModel.deleteEvent(event) },
                                    onToggleStarred = { event -> viewModel.toggleStarred(event) },
                                    onSeedSamples = { viewModel.seedSampleEvents() }
                                )
                                2 -> SettingsScreen(
                                    settings = settings,
                                    accentColor = accentColor,
                                    onAccentChanged = { viewModel.setAccentColor(it) },
                                    onCountTodayChanged = { viewModel.setCountTodayAsRemaining(it) },
                                    onDensityChanged = { viewModel.setGridDensity(it) },
                                    onWidgetBackgroundModeChanged = { viewModel.setWidgetBackgroundMode(it) },
                                    onWidgetBackgroundOpacityChanged = { viewModel.setWidgetBackgroundOpacity(it) },
                                    onSyncWidget = { viewModel.refreshWidgetManually() },
                                    onSeedSamples = { viewModel.seedSampleEvents() },
                                    widgetRefreshStatus = widgetRefreshStatus
                                )
                            }
                        }
                    }

                    // Day Detail Dialog
                    selectedDayOfYear?.let { day ->
                        val date = yearInfo.getDateForDay(day)
                        val eventsOnDate = events.filter { it.targetDate == date }

                        DayDetailDialog(
                            dayOfYear = day,
                            yearInfo = yearInfo,
                            accentColor = accentColor,
                            eventsOnDate = eventsOnDate,
                            onDismiss = { viewModel.selectDay(null) },
                            onCreateEvent = { targetDate ->
                                viewModel.openAddEventDialog(prefilledDate = targetDate)
                            }
                        )
                    }

                    // Add / Edit Event Dialog
                    if (isAddEventOpen) {
                        AddEditEventDialog(
                            initialEvent = editingEvent,
                            prefilledDate = prefilledDate,
                            defaultAccentHex = settings.accentHex,
                            onDismiss = { viewModel.closeEventDialog() },
                            onSave = { id, title, targetDate, startDate, emoji, accentHex, isStarred ->
                                viewModel.saveEvent(
                                    id = id,
                                    title = title,
                                    targetDate = targetDate,
                                    startDate = startDate,
                                    emoji = emoji,
                                    accentHex = accentHex,
                                    isStarred = isStarred
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DotDayBottomBar(
    currentTab: Int,
    accentColor: Color,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = DeepCharcoal,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.dp, OutlineMuted.copy(alpha = 0.88f), RoundedCornerShape(24.dp))
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                index = 0,
                selected = currentTab == 0,
                label = "Year",
                selectedIcon = Icons.Filled.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                accentColor = accentColor,
                onClick = { onTabSelected(0) },
                testTag = "nav_tab_year"
            )

            BottomNavItem(
                index = 1,
                selected = currentTab == 1,
                label = "Countdowns",
                selectedIcon = Icons.Filled.HourglassEmpty,
                unselectedIcon = Icons.Outlined.HourglassEmpty,
                accentColor = accentColor,
                onClick = { onTabSelected(1) },
                testTag = "nav_tab_events"
            )

            BottomNavItem(
                index = 2,
                selected = currentTab == 2,
                label = "Settings",
                selectedIcon = Icons.Filled.Tune,
                unselectedIcon = Icons.Outlined.Tune,
                accentColor = accentColor,
                onClick = { onTabSelected(2) },
                testTag = "nav_tab_settings"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    index: Int,
    selected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val selectedSurface by animateColorAsState(
        targetValue = if (selected) accentColor else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "navSelectionColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "navIconScale"
    )

    Box(
        modifier = Modifier
            .animateContentSize(animationSpec = spring(dampingRatio = 0.86f, stiffness = 520f))
            .clip(RoundedCornerShape(16.dp))
            .background(selectedSurface)
            .then(
                if (selected) Modifier.border(1.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (selected) 15.dp else 13.dp, vertical = 9.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(150)) + expandHorizontally(tween(190)),
                exit = fadeOut(tween(100)) + shrinkHorizontally(tween(150))
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
