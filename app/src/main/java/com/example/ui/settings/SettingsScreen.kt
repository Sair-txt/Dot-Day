package com.example.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Locale
import com.example.data.model.GridDensity
import com.example.data.model.UserSettings
import com.example.data.model.WidgetBackgroundMode
import com.example.ui.WidgetRefreshStatus
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.OutlineMuted
import com.example.ui.theme.PrimaryNavyLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceHighlighted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    settings: UserSettings,
    accentColor: Color,
    onAccentChanged: (String) -> Unit,
    onCountTodayChanged: (Boolean) -> Unit,
    onDensityChanged: (GridDensity) -> Unit,
    onWidgetBackgroundModeChanged: (WidgetBackgroundMode) -> Unit,
    onWidgetBackgroundOpacityChanged: (Int) -> Unit,
    onSyncWidget: () -> Unit,
    onSeedSamples: () -> Unit,
    widgetRefreshStatus: WidgetRefreshStatus = WidgetRefreshStatus.Idle,
    modifier: Modifier = Modifier
) {
    var draftWidgetOpacity by remember(settings.widgetBackgroundOpacity) {
        mutableStateOf(settings.widgetBackgroundOpacity.toFloat())
    }
    var draftHsl by remember(settings.accentHex) { mutableStateOf(hexToHsl(settings.accentHex)) }
    val draftAccentColor = hslToComposeColor(draftHsl)
    val animatedDraftAccentColor by animateColorAsState(
        targetValue = draftAccentColor,
        animationSpec = tween(durationMillis = 120),
        label = "accentSwatchTransition"
    )
    val draftAccentHex = hslToHex(draftHsl)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF151720), DeepCharcoal, DeepCharcoal)))
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 110.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Make Dot Day feel like yours",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Section: Accent Color
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Accent Color Studio",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose any hue and saturation on the wheel, then tune its lightness. The accent is used for today's dot and selected controls; past and future dots stay neutral.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AccentColorWheel(
                            hsl = draftHsl,
                            onHslChanged = { draftHsl = it },
                            modifier = Modifier.size(224.dp).testTag("accent_color_wheel")
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(animatedDraftAccentColor)
                                .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = draftAccentHex,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Hue ${draftHsl.hue.roundToInt()}° · Saturation ${(draftHsl.saturation * 100).roundToInt()}%",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lightness", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("${(draftHsl.lightness * 100).roundToInt()}%", color = TextSecondary, fontSize = 12.sp)
                    }
                    Slider(
                        value = draftHsl.lightness,
                        onValueChange = { draftHsl = draftHsl.copy(lightness = it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedDraftAccentColor,
                            activeTrackColor = animatedDraftAccentColor,
                            inactiveTrackColor = OutlineMuted
                        ),
                        modifier = Modifier.testTag("accent_lightness_slider")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dark", color = TextTertiary, fontSize = 11.sp)
                        Text("Light", color = TextTertiary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onAccentChanged(draftAccentHex) },
                        enabled = !draftAccentHex.equals(settings.accentHex, ignoreCase = true),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedDraftAccentColor,
                            contentColor = if (animatedDraftAccentColor.luminance() > 0.46f) Color(0xFF111216) else Color.White,
                            disabledContainerColor = ElevatedSurface,
                            disabledContentColor = TextTertiary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().animateContentSize().testTag("apply_custom_accent")
                    ) {
                        Text(if (draftAccentHex.equals(settings.accentHex, ignoreCase = true)) "Accent applied" else "Apply custom color")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            draftHsl = hexToHsl("#191F45")
                            onAccentChanged("#191F45")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElevatedSurface,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restore_midnight_navy")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = PrimaryNavyLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Midnight Navy (#191F45)")
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Quick presets",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserSettings.PRESET_ACCENTS.forEach { (hex, name) ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = settings.accentHex.equals(hex, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ElevatedSurface else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) color.copy(alpha = 0.75f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        draftHsl = hexToHsl(hex)
                                        onAccentChanged(hex)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("select_color_$hex"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Grid Spacing / Density
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Year Grid Density",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Adjust the columns and size of dots on the main Year Canvas.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElevatedSurface)
                            .border(1.dp, OutlineMuted, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GridDensity.values().forEach { density ->
                            val isSelected = settings.gridDensity == density
                            val densityFill by animateColorAsState(
                                targetValue = if (isSelected) accentColor else Color.Transparent,
                                animationSpec = tween(180),
                                label = "gridDensitySelection"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(densityFill)
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, accentColor.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .clickable { onDensityChanged(density) }
                                    .padding(vertical = 8.dp)
                                    .testTag("density_${density.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = density.label,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Calculation Preferences
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Day Count Calculation",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Count today as remaining",
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (settings.countTodayAsRemaining)
                                    "Remaining includes today through Dec 31."
                                else
                                    "Remaining counts only days strictly after today.",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = settings.countTodayAsRemaining,
                            onCheckedChange = { onCountTodayChanged(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ElevatedSurface
                            ),
                            modifier = Modifier.testTag("switch_count_today")
                        )
                    }
                }
            }
        }

        // Section: Widget appearance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Widget Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Preview and choose how the year grid sits on your home screen.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WidgetAppearanceOption(
                            mode = WidgetBackgroundMode.SOLID,
                            opacity = draftWidgetOpacity.toInt(),
                            selected = settings.widgetBackgroundMode == WidgetBackgroundMode.SOLID,
                            accentColor = accentColor,
                            onClick = { onWidgetBackgroundModeChanged(WidgetBackgroundMode.SOLID) },
                            modifier = Modifier.weight(1f)
                        )
                        WidgetAppearanceOption(
                            mode = WidgetBackgroundMode.TRANSPARENT,
                            opacity = draftWidgetOpacity.toInt(),
                            selected = settings.widgetBackgroundMode == WidgetBackgroundMode.TRANSPARENT,
                            accentColor = accentColor,
                            onClick = { onWidgetBackgroundModeChanged(WidgetBackgroundMode.TRANSPARENT) },
                            modifier = Modifier.weight(1f)
                        )
                        WidgetAppearanceOption(
                            mode = WidgetBackgroundMode.TRANSLUCENT,
                            opacity = draftWidgetOpacity.toInt(),
                            selected = settings.widgetBackgroundMode == WidgetBackgroundMode.TRANSLUCENT,
                            accentColor = accentColor,
                            onClick = { onWidgetBackgroundModeChanged(WidgetBackgroundMode.TRANSLUCENT) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (settings.widgetBackgroundMode == WidgetBackgroundMode.TRANSLUCENT) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Background opacity",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${draftWidgetOpacity.toInt()}%",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("widget_bg_opacity_value")
                            )
                        }
                        Slider(
                            value = draftWidgetOpacity,
                            onValueChange = { draftWidgetOpacity = it.toInt().toFloat() },
                            onValueChangeFinished = {
                                onWidgetBackgroundOpacityChanged(draftWidgetOpacity.toInt().coerceIn(0, 100))
                            },
                            valueRange = 0f..100f,
                            steps = 99,
                            modifier = Modifier.testTag("widget_bg_opacity_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wallpaper visible", color = TextTertiary, fontSize = 11.sp)
                            Text("Opaque charcoal", color = TextTertiary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is a translucent charcoal tint, not a live blur: Jetpack Glance cannot blur the wallpaper behind a widget. Transparent mode works best on darker wallpaper; choose translucent or solid on bright backgrounds for readable text.",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Section: Home Screen Widget
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Landscape 4 × 2 Home-Screen Widget",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dot Day includes a native Jetpack Glance widget designed for a 4 × 2 landscape footprint:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Long-press an empty area on your Android home screen.\n2. Open Widgets.\n3. Find Dot Day and drag the widget onto your home screen.\n4. The widget shows the complete year dot grid and the days left count.",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val isRefreshing = widgetRefreshStatus is WidgetRefreshStatus.Refreshing

                    Button(
                        onClick = onSyncWidget,
                        enabled = !isRefreshing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (widgetRefreshStatus) {
                                is WidgetRefreshStatus.Success -> Color(0xFF1B4D3E)
                                is WidgetRefreshStatus.Failure -> Color(0xFF5A1E1E)
                                is WidgetRefreshStatus.NoWidgets -> ElevatedSurface
                                else -> accentColor
                            },
                            contentColor = when (widgetRefreshStatus) {
                                is WidgetRefreshStatus.NoWidgets -> TextPrimary
                                else -> MaterialTheme.colorScheme.onPrimary
                            },
                            disabledContainerColor = accentColor.copy(alpha = 0.6f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (widgetRefreshStatus) {
                                is WidgetRefreshStatus.Success -> Color(0xFF34D399).copy(alpha = 0.6f)
                                is WidgetRefreshStatus.Failure -> Color(0xFFF87171).copy(alpha = 0.6f)
                                is WidgetRefreshStatus.NoWidgets -> OutlineMuted
                                else -> accentColor.copy(alpha = 0.72f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .testTag("sync_widget_button")
                    ) {
                        when (widgetRefreshStatus) {
                            is WidgetRefreshStatus.Refreshing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refreshing Dots…", fontWeight = FontWeight.SemiBold)
                            }
                            is WidgetRefreshStatus.Success -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF34D399)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(widgetRefreshStatus.message, fontWeight = FontWeight.SemiBold)
                            }
                            is WidgetRefreshStatus.NoWidgets -> {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("No Active Widgets Placed", fontWeight = FontWeight.SemiBold)
                            }
                            is WidgetRefreshStatus.Failure -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(widgetRefreshStatus.message, fontWeight = FontWeight.SemiBold)
                            }
                            is WidgetRefreshStatus.Idle -> {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refresh Widget Now", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (widgetRefreshStatus is WidgetRefreshStatus.NoWidgets) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Tip: Long-press your phone's home screen, tap 'Widgets', find 'Dot Day', and drag it to your screen to see your 365-day dots live.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section: Data & Backup
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted.copy(alpha = 0.9f)),
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Local Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dot Day operates 100% offline. All events and preferences are persisted locally on your device with Room and SharedPreferences. No remote accounts, analytics, or background telemetry.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onSeedSamples,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElevatedSurface,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineMuted),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("seed_sample_events_button")
                    ) {
                        Text("Add Sample Countdowns (Birthdays & Holidays)")
                    }
                }
            }
        }

        // Footer version info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dot Day v1.0",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Minimalist Time & Countdown Visualizer",
                    color = TextTertiary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class HslColor(val hue: Float, val saturation: Float, val lightness: Float)

private fun hexToHsl(hex: String): HslColor {
    val parsed = try {
        android.graphics.Color.parseColor(hex)
    } catch (_: IllegalArgumentException) {
        android.graphics.Color.parseColor("#191F45")
    }
    val r = android.graphics.Color.red(parsed) / 255f
    val g = android.graphics.Color.green(parsed) / 255f
    val b = android.graphics.Color.blue(parsed) / 255f
    val maxChannel = max(r, max(g, b))
    val minChannel = min(r, min(g, b))
    val delta = maxChannel - minChannel
    val lightness = (maxChannel + minChannel) / 2f
    val saturation = if (delta == 0f) 0f else delta / (1f - abs(2f * lightness - 1f)).coerceAtLeast(0.0001f)
    val hue = when {
        delta == 0f -> 0f
        maxChannel == r -> 60f * (((g - b) / delta) % 6f)
        maxChannel == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    return HslColor(hue, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
}

private fun hslToComposeColor(hsl: HslColor): Color {
    val hue = ((hsl.hue % 360f) + 360f) % 360f
    val saturation = hsl.saturation.coerceIn(0f, 1f)
    val lightness = hsl.lightness.coerceIn(0f, 1f)
    val chroma = (1f - abs(2f * lightness - 1f)) * saturation
    val x = chroma * (1f - abs((hue / 60f) % 2f - 1f))
    val m = lightness - chroma / 2f
    val (r1, g1, b1) = when {
        hue < 60f -> Triple(chroma, x, 0f)
        hue < 120f -> Triple(x, chroma, 0f)
        hue < 180f -> Triple(0f, chroma, x)
        hue < 240f -> Triple(0f, x, chroma)
        hue < 300f -> Triple(x, 0f, chroma)
        else -> Triple(chroma, 0f, x)
    }
    return Color((r1 + m).coerceIn(0f, 1f), (g1 + m).coerceIn(0f, 1f), (b1 + m).coerceIn(0f, 1f))
}

private fun hslToHex(hsl: HslColor): String {
    val color = hslToComposeColor(hsl)
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return String.format(Locale.US, "#%02X%02X%02X", red, green, blue)
}

@Composable
private fun AccentColorWheel(
    hsl: HslColor,
    onHslChanged: (HslColor) -> Unit,
    modifier: Modifier = Modifier
) {
    fun pick(position: Offset, width: Float, height: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        val dx = position.x - centerX
        val dy = position.y - centerY
        val radius = min(width, height) / 2f
        if (radius <= 0f) return
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        val hue = (((atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI) + 360.0) % 360.0).toFloat()
        onHslChanged(hsl.copy(hue = hue, saturation = (distance / radius).coerceIn(0f, 1f)))
    }

    Canvas(
        modifier = modifier
            .pointerInput(hsl.lightness) {
                detectTapGestures { pick(it, size.width.toFloat(), size.height.toFloat()) }
            }
            .pointerInput(hsl.lightness) {
                detectDragGestures(
                    onDragStart = { pick(it, size.width.toFloat(), size.height.toFloat()) },
                    onDrag = { change, _ ->
                        pick(change.position, size.width.toFloat(), size.height.toFloat())
                        change.consume()
                    }
                )
            }
    ) {
        val radius = min(size.width, size.height) / 2f - 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val rings = 24
        val segments = 180
        val ringStep = radius / rings

        // Draw a real HSL disc: angle chooses hue, distance from center chooses saturation,
        // and the separate slider controls lightness. Small overlapping arc strokes avoid seams.
        drawCircle(
            color = hslToComposeColor(HslColor(hsl.hue, 0f, 0.5f)),
            radius = ringStep * 1.05f,
            center = center
        )
        for (ring in 0 until rings) {
            val ringRadius = ringStep * (ring + 0.5f)
            val saturation = (ring + 0.5f) / rings.toFloat()
            for (segment in 0 until segments) {
                val hue = segment * 360f / segments
                drawArc(
                    color = hslToComposeColor(HslColor(hue, saturation, 0.5f)),
                    startAngle = hue,
                    sweepAngle = 360f / segments + 0.35f,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = Size(ringRadius * 2f, ringRadius * 2f),
                    style = Stroke(width = ringStep * 1.12f)
                )
            }
        }

        val selectedRadius = hsl.saturation * radius
        val markerCenter = Offset(
            center.x + cos(hsl.hue * PI.toFloat() / 180f) * selectedRadius,
            center.y + sin(hsl.hue * PI.toFloat() / 180f) * selectedRadius
        )
        val markerRadius = max(5f, radius * 0.045f)
        drawCircle(Color(0xFF111216), radius = markerRadius + 3f, center = markerCenter)
        drawCircle(Color.White, radius = markerRadius + 1.3f, center = markerCenter)
        drawCircle(draftlessMarkerColor(hsl), radius = markerRadius - 0.8f, center = markerCenter)
    }
}

private fun draftlessMarkerColor(hsl: HslColor): Color = hslToComposeColor(hsl)

@Composable
private fun WidgetAppearanceOption(
    mode: WidgetBackgroundMode,
    opacity: Int,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val label = when (mode) {
        WidgetBackgroundMode.SOLID -> "Solid"
        WidgetBackgroundMode.TRANSPARENT -> "Transparent"
        WidgetBackgroundMode.TRANSLUCENT -> "Translucent"
    }
    val optionFill by animateColorAsState(
        targetValue = if (selected) SurfaceHighlighted else ElevatedSurface,
        animationSpec = tween(180),
        label = "widgetModeFill"
    )
    val optionStroke by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.9f) else OutlineMuted,
        animationSpec = tween(180),
        label = "widgetModeStroke"
    )
    Column(
        modifier = modifier
            .animateContentSize()
            .clip(shape)
            .background(optionFill)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = optionStroke,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WidgetAppearancePreview(mode = mode, opacity = opacity, accentColor = accentColor)
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            color = if (selected) TextPrimary else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun WidgetAppearancePreview(
    mode: WidgetBackgroundMode,
    opacity: Int,
    accentColor: Color
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(shape)
            .border(1.dp, OutlineMuted, shape)
    ) {
        // This patterned layer is the simulated launcher wallpaper. The Transparent preview
        // paints no widget surface over it; the app widget itself follows the same rule.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFF383940))
            drawRect(
                color = Color(0xFF202126),
                topLeft = Offset(size.width * 0.58f, 0f),
                size = Size(size.width * 0.42f, size.height)
            )
            drawRect(
                color = Color(0xFF2B2C33),
                topLeft = Offset(0f, size.height * 0.68f),
                size = Size(size.width * 0.58f, size.height * 0.32f)
            )
        }

        if (mode != WidgetBackgroundMode.TRANSPARENT) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (mode == WidgetBackgroundMode.SOLID) {
                            Color(0xFF202126)
                        } else {
                            Color(0xFF202126).copy(alpha = opacity.coerceIn(0, 100) / 100f)
                        }
                    )
                    .then(
                        if (mode == WidgetBackgroundMode.TRANSLUCENT) {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.12f), shape)
                        } else Modifier
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("2026", color = TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Text("72 DAYS LEFT", color = TextPrimary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(5.dp))
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val columns = 19
                val rows = 5
                val stepX = size.width / columns
                val stepY = size.height / rows
                val radius = minOf(stepX, stepY) * 0.31f
                val currentDot = 75
                for (i in 0 until columns * rows) {
                    val x = (i % columns) * stepX + stepX / 2f
                    val y = (i / columns) * stepY + stepY / 2f
                    when {
                        i < currentDot -> {
                            drawCircle(Color(0xFF111216), radius = radius + 0.3f, center = Offset(x, y))
                            drawCircle(Color(0xFFF5F5F7), radius = radius, center = Offset(x, y))
                        }
                        i == currentDot -> {
                            drawCircle(Color(0xFFF5F5F7), radius = radius + 0.55f, center = Offset(x, y), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8f))
                            drawCircle(accentColor, radius = radius * 0.72f, center = Offset(x, y))
                        }
                        else -> drawCircle(Color(0xFF3D3E45), radius = radius, center = Offset(x, y))
                    }
                }
            }
        }
    }
}

