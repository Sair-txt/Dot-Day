package com.example.ui.year

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.DayStatus
import com.example.data.model.GridDensity
import com.example.data.model.YearInfo
import com.example.ui.theme.CompletedDotColor
import com.example.ui.theme.FutureDotColor
import com.example.ui.theme.TodayDotRingColor

@Composable
fun YearDotsCanvas(
    yearInfo: YearInfo,
    accentColor: Color,
    density: GridDensity,
    eventDaysOfYear: Set<Int>,
    onDotClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = density.columns
    val rows = (yearInfo.totalDays + columns - 1) / columns
    val canvasHeightDp = (rows * (density.dotRadiusDp * 2 + density.spacingDp * 2.2f) + 24).dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeightDp)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("year_dots_canvas")
            .pointerInput(yearInfo.totalDays, columns) {
                detectTapGestures { offset ->
                    val totalW = size.width
                    val cellWidth = totalW / columns
                    val cellHeight = size.height / rows

                    val col = (offset.x / cellWidth).toInt()
                    val row = (offset.y / cellHeight).toInt()

                    if (col in 0 until columns && row in 0 until rows) {
                        val dayIndex = row * columns + col
                        if (dayIndex in 0 until yearInfo.totalDays) {
                            onDotClicked(dayIndex + 1)
                        }
                    }
                }
            }
    ) {
        val totalW = size.width
        val totalH = size.height
        val cellW = totalW / columns
        val cellH = totalH / rows
        val baseRadius = (minOf(cellW, cellH) * 0.36f).coerceIn(3.0f, 9.5f)

        for (dayIndex in 0 until yearInfo.totalDays) {
            val dayOfYear = dayIndex + 1
            val col = dayIndex % columns
            val row = dayIndex / columns

            val centerX = col * cellW + cellW / 2f
            val centerY = row * cellH + cellH / 2f
            val center = Offset(centerX, centerY)

            val status = yearInfo.getDayStatus(dayOfYear)
            val hasEvent = eventDaysOfYear.contains(dayOfYear)

            when (status) {
                DayStatus.PAST -> {
                    // Solid near-white circles (#F5F5F7)
                    drawCircle(
                        color = CompletedDotColor,
                        radius = baseRadius,
                        center = center
                    )
                }
                DayStatus.TODAY -> {
                    // Thin near-white ring (#F5F5F7) so Midnight Navy center is visible against dark background
                    drawCircle(
                        color = TodayDotRingColor,
                        radius = baseRadius + 1.8f,
                        center = center,
                        style = Stroke(width = 1.4f)
                    )
                    // User-selected accent center, with a neutral outline for contrast.
                    drawCircle(
                        color = accentColor,
                        radius = baseRadius,
                        center = center
                    )
                }
                DayStatus.FUTURE -> {
                    // Muted charcoal (#3D3E45)
                    drawCircle(
                        color = FutureDotColor,
                        radius = baseRadius,
                        center = center
                    )
                }
            }

            // Milestone ring for days with custom countdown events
            if (hasEvent && status != DayStatus.TODAY) {
                val ringColor = if (accentColor == com.example.ui.theme.PrimaryNavy) com.example.ui.theme.PrimaryNavyLight else accentColor
                drawCircle(
                    color = ringColor,
                    radius = baseRadius + 2.8f,
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }
        }
    }
}
