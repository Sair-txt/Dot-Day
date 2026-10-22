package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.local.PreferencesManager
import com.example.data.model.DayStatus
import com.example.data.model.WidgetBackgroundMode
import com.example.data.model.YearInfo
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.roundToInt

class YearInDotsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearInDotsGlanceWidget()
}

class YearInDotsGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val countToday = PreferencesManager.getCountTodayAsRemaining(context)
        val backgroundMode = PreferencesManager.getWidgetBackgroundMode(context)
        val backgroundOpacity = PreferencesManager.getWidgetBackgroundOpacity(context)
        val accentColor = try {
            android.graphics.Color.parseColor(PreferencesManager.getAccentColorHex(context))
        } catch (_: IllegalArgumentException) {
            android.graphics.Color.parseColor("#191F45")
        }
        val yearInfo = YearInfo.current(countTodayAsRemaining = countToday, now = LocalDate.now())

        provideContent {
            val widgetSize = LocalSize.current
            val isLandscape4x2 = widgetSize.width >= 170.dp
            val density = context.resources.displayMetrics.density

            // Sensible, balanced padding for 4x2 landscape cards
            val padHorizontalDp = if (isLandscape4x2) 12.dp else 10.dp
            val padVerticalDp = if (isLandscape4x2) 10.dp else 8.dp
            val headerAndSpacingDp = if (isLandscape4x2) 24f else 20f

            // Calculate exact space available for the grid after subtracting padding and header
            val availableWidthDp = (widgetSize.width.value - padHorizontalDp.value * 2f).coerceAtLeast(80f)
            val availableHeightDp = (widgetSize.height.value - (padVerticalDp.value * 2f + headerAndSpacingDp)).coerceAtLeast(36f)

            // High-resolution bitmap matching the exact aspect ratio of the available inner space
            val scale = maxOf(density, 2.0f)
            val gridWidthPx = (availableWidthDp * scale).roundToInt().coerceAtLeast(100)
            val gridHeightPx = (availableHeightDp * scale).roundToInt().coerceAtLeast(40)

            val dotMatrixBitmap = generateDotGridBitmap(
                yearInfo = yearInfo,
                widthPx = gridWidthPx,
                heightPx = gridHeightPx,
                accentArgb = accentColor
            )

            val textPrimary = Color(0xFFF5F5F7)
            val textSecondary = Color(0xFFA3A4AD)
            val surfaceColor = when (backgroundMode) {
                WidgetBackgroundMode.SOLID -> Color(0xFF202126)
                WidgetBackgroundMode.TRANSPARENT -> null
                WidgetBackgroundMode.TRANSLUCENT -> Color(0xFF202126).copy(
                    alpha = backgroundOpacity / 100f
                )
            }
            val outlineColor = Color(0xFFF5F5F7).copy(alpha = 0.12f)

            val rootModifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(18.dp)
                .clickable(actionStartActivity<MainActivity>())

            Box(modifier = rootModifier) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .then(surfaceColor?.let { GlanceModifier.background(it) } ?: GlanceModifier)
                        .cornerRadius(18.dp)
                        .padding(
                            horizontal = padHorizontalDp,
                            vertical = padVerticalDp
                        ),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = yearInfo.year.toString(),
                                style = TextStyle(
                                    color = ColorProvider(textSecondary),
                                    fontSize = if (isLandscape4x2) 14.sp else 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = yearInfo.remainingDaysLabel,
                                style = TextStyle(
                                    color = ColorProvider(textPrimary),
                                    fontSize = if (isLandscape4x2) 16.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(if (isLandscape4x2) 5.dp else 3.dp))

                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(dotMatrixBitmap),
                                contentDescription = "${yearInfo.totalDays} day dots for ${yearInfo.year}; today uses the selected accent color",
                                contentScale = ContentScale.Fit,
                                modifier = GlanceModifier.fillMaxSize()
                            )
                        }
                    }
                }

                if (backgroundMode == WidgetBackgroundMode.TRANSLUCENT) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(outlineColor)
                        ) {}
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(outlineColor)
                        ) {}
                    }
                    Row(modifier = GlanceModifier.fillMaxSize()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(outlineColor)
                        ) {}
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Box(
                            modifier = GlanceModifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(outlineColor)
                        ) {}
                    }
                }
            }
        }
    }

    /**
     * Draws exactly one circle for each calendar day: 365 on common years and 366 on leap
     * years. The 37-column grid uses as many rows as needed, leaving any final unused cells
     * transparent. A slim dark edge around white dots helps preserve definition on light
     * wallpaper; the current day uses the selected accent centre and a white ring.
     */
    private fun generateDotGridBitmap(
        yearInfo: YearInfo,
        widthPx: Int,
        heightPx: Int,
        accentArgb: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val aspect = widthPx.toFloat() / heightPx.toFloat().coerceAtLeast(1f)
        val idealCols = kotlin.math.sqrt(aspect * yearInfo.totalDays).roundToInt()
        val columns = idealCols.coerceIn(24, 45)
        val rows = ceil(yearInfo.totalDays / columns.toDouble()).toInt()
        val cellW = widthPx.toFloat() / columns
        val cellH = heightPx.toFloat() / rows
        val dotRadius = minOf(cellW, cellH) * 0.35f
        val edgeWidth = maxOf(0.7f, minOf(cellW, cellH) * 0.09f)

        val darkEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#111216")
            style = Paint.Style.STROKE
            strokeWidth = edgeWidth
        }
        val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#F5F5F7")
            style = Paint.Style.FILL
        }
        val todayAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentArgb
            style = Paint.Style.FILL
        }
        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#F5F5F7")
            style = Paint.Style.STROKE
            strokeWidth = maxOf(0.8f, edgeWidth * 1.3f)
        }
        val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#3D3E45")
            style = Paint.Style.FILL
        }

        for (dayIndex in 0 until yearInfo.totalDays) {
            val column = dayIndex % columns
            val row = dayIndex / columns
            val cx = column * cellW + cellW / 2f
            val cy = row * cellH + cellH / 2f
            when (yearInfo.getDayStatus(dayIndex + 1)) {
                DayStatus.PAST -> {
                    canvas.drawCircle(cx, cy, dotRadius, completedPaint)
                    canvas.drawCircle(cx, cy, dotRadius, darkEdgePaint)
                }
                DayStatus.TODAY -> {
                    canvas.drawCircle(cx, cy, dotRadius + edgeWidth, darkEdgePaint)
                    canvas.drawCircle(cx, cy, dotRadius + edgeWidth * 0.35f, todayRingPaint)
                    canvas.drawCircle(cx, cy, dotRadius * 0.68f, todayAccentPaint)
                }
                DayStatus.FUTURE -> canvas.drawCircle(cx, cy, dotRadius, futurePaint)
            }
        }
        return bitmap
    }
}
