package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CountdownEvent
import com.example.data.model.DayStatus
import com.example.data.model.YearInfo
import com.example.data.model.WidgetBackgroundMode
import com.example.data.local.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Dot Day`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Dot Day", appName)
    }

    @Test
    fun `year info correctly handles leap year and regular year`() {
        val leapYearDate = LocalDate.of(2024, 7, 1)
        val leapInfo = YearInfo.current(countTodayAsRemaining = true, now = leapYearDate)
        assertTrue(leapInfo.isLeapYear)
        assertEquals(366, leapInfo.totalDays)

        val regularYearDate = LocalDate.of(2026, 7, 1)
        val regularInfo = YearInfo.current(countTodayAsRemaining = true, now = regularYearDate)
        assertFalse(regularInfo.isLeapYear)
        assertEquals(365, regularInfo.totalDays)
    }

    @Test
    fun `countdown event calculates remaining days correctly`() {
        val today = LocalDate.of(2026, 10, 21)
        val eventTomorrow = CountdownEvent(
            title = "Launch",
            targetDate = LocalDate.of(2026, 10, 22),
            startDate = today
        )
        assertEquals(1L, eventTomorrow.daysRemaining(today))
        assertEquals("Tomorrow", eventTomorrow.formattedRemaining(today))

        val eventToday = CountdownEvent(
            title = "Birthday",
            targetDate = today,
            startDate = today.minusDays(10)
        )
        assertEquals(0L, eventToday.daysRemaining(today))
        assertEquals("Today", eventToday.formattedRemaining(today))

        val eventFuture = CountdownEvent(
            title = "New Year",
            targetDate = LocalDate.of(2027, 1, 1),
            startDate = today
        )
        assertEquals(72L, eventFuture.daysRemaining(today))
        assertEquals("72 days left", eventFuture.formattedRemaining(today))
    }


    @Test
    fun `remaining days label respects count today setting and date boundary`() {
        val octTwentyFirst = LocalDate.of(2026, 10, 21)
        val includeToday = YearInfo.current(countTodayAsRemaining = true, now = octTwentyFirst)
        val excludeToday = YearInfo.current(countTodayAsRemaining = false, now = octTwentyFirst)

        assertEquals(72, includeToday.remainingDaysCount)
        assertEquals("72 DAYS LEFT", includeToday.remainingDaysLabel)
        assertEquals(71, excludeToday.remainingDaysCount)
        assertEquals("71 DAYS LEFT", excludeToday.remainingDaysLabel)

        val finalDayIncludingToday = YearInfo.current(
            countTodayAsRemaining = true,
            now = LocalDate.of(2026, 12, 31)
        )
        val finalDayExcludingToday = YearInfo.current(
            countTodayAsRemaining = false,
            now = LocalDate.of(2026, 12, 31)
        )
        assertEquals("1 DAY LEFT", finalDayIncludingToday.remainingDaysLabel)
        assertEquals("0 DAYS LEFT", finalDayExcludingToday.remainingDaysLabel)
    }

    @Test
    fun `widget appearance preferences persist through a new preferences manager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences(PreferencesManager.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()

        val firstManager = PreferencesManager(context)
        firstManager.updateWidgetBackgroundMode(WidgetBackgroundMode.TRANSLUCENT)
        firstManager.updateWidgetBackgroundOpacity(42)
        firstManager.updateCountToday(false)

        val recreatedManager = PreferencesManager(context)
        assertEquals(WidgetBackgroundMode.TRANSLUCENT, recreatedManager.settingsFlow.value.widgetBackgroundMode)
        assertEquals(42, recreatedManager.settingsFlow.value.widgetBackgroundOpacity)
        assertFalse(recreatedManager.settingsFlow.value.countTodayAsRemaining)
        assertEquals(WidgetBackgroundMode.TRANSLUCENT, PreferencesManager.getWidgetBackgroundMode(context))
        assertEquals(42, PreferencesManager.getWidgetBackgroundOpacity(context))
    }

    @Test
    fun `widget background opacity is clamped to valid percentage`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences(PreferencesManager.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()

        val manager = PreferencesManager(context)
        manager.updateWidgetBackgroundOpacity(-7)
        assertEquals(0, manager.settingsFlow.value.widgetBackgroundOpacity)
        manager.updateWidgetBackgroundOpacity(120)
        assertEquals(100, manager.settingsFlow.value.widgetBackgroundOpacity)
    }

    @Test
    fun `default user settings use Midnight Navy and Compact density`() {
        val settings = com.example.data.model.UserSettings.DEFAULT
        assertEquals("#191F45", settings.accentHex)
        assertEquals(com.example.data.model.GridDensity.COMPACT, settings.gridDensity)
        assertTrue(settings.countTodayAsRemaining)
    }

    @Test
    fun `custom accent color persists and updates preferences manager`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = PreferencesManager(context)
        manager.updateAccent("#7AE2C0")
        assertEquals("#7AE2C0", manager.settingsFlow.value.accentHex)
        assertEquals("#7AE2C0", PreferencesManager.getAccentColorHex(context))

        // Restore Midnight Navy
        manager.updateAccent("#191F45")
        assertEquals("#191F45", manager.settingsFlow.value.accentHex)
        assertEquals("#191F45", PreferencesManager.getAccentColorHex(context))
    }

    @Test
    fun `day status correctly classifies past today and future`() {
        val today = LocalDate.of(2026, 6, 15) // Day 166
        val yearInfo = YearInfo.current(countTodayAsRemaining = true, now = today)
        val todayDayOfYear = today.dayOfYear

        assertEquals(DayStatus.PAST, yearInfo.getDayStatus(todayDayOfYear - 1))
        assertEquals(DayStatus.TODAY, yearInfo.getDayStatus(todayDayOfYear))
        assertEquals(DayStatus.FUTURE, yearInfo.getDayStatus(todayDayOfYear + 1))
    }

    @Test
    fun `widget update helper refreshWidgets handles empty widget environment safely`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = com.example.widget.WidgetUpdateHelper.refreshWidgets(context)
        org.junit.Assert.assertNotNull(result)
        // In Robolectric environment with no widgets on home screen, it should safely return NoWidgets or Success
        assertTrue(result is com.example.widget.WidgetRefreshResult.NoWidgets || result is com.example.widget.WidgetRefreshResult.Success)
    }

    @Test
    fun `widget refresh status sealed hierarchy models all interactive states`() {
        val idle: com.example.ui.WidgetRefreshStatus = com.example.ui.WidgetRefreshStatus.Idle
        val refreshing: com.example.ui.WidgetRefreshStatus = com.example.ui.WidgetRefreshStatus.Refreshing
        val success = com.example.ui.WidgetRefreshStatus.Success("Widget updated", 2)
        val noWidgets = com.example.ui.WidgetRefreshStatus.NoWidgets("No active widgets")
        val failure = com.example.ui.WidgetRefreshStatus.Failure("Failed")

        assertTrue(idle is com.example.ui.WidgetRefreshStatus.Idle)
        assertTrue(refreshing is com.example.ui.WidgetRefreshStatus.Refreshing)
        assertEquals(2, success.count)
        assertEquals("Widget updated", success.message)
        assertEquals("No active widgets", noWidgets.message)
        assertEquals("Failed", failure.message)
    }
}
