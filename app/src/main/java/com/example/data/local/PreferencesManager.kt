package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GridDensity
import com.example.data.model.UserSettings
import com.example.data.model.WidgetBackgroundMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): UserSettings {
        val accent = prefs.getString(KEY_ACCENT, "#191F45") ?: "#191F45"
        val countToday = prefs.getBoolean(KEY_COUNT_TODAY, true)
        val densityName = prefs.getString(KEY_DENSITY, GridDensity.COMPACT.name) ?: GridDensity.COMPACT.name
        val density = try {
            GridDensity.valueOf(densityName)
        } catch (e: Exception) {
            GridDensity.COMPACT
        }
        val showPercent = prefs.getBoolean(KEY_SHOW_PERCENT, true)
        val bgModeName = prefs.getString(KEY_WIDGET_BG_MODE, WidgetBackgroundMode.SOLID.name) ?: WidgetBackgroundMode.SOLID.name
        val bgMode = try {
            WidgetBackgroundMode.valueOf(bgModeName)
        } catch (e: Exception) {
            WidgetBackgroundMode.SOLID
        }
        val bgOpacity = prefs.getInt(KEY_WIDGET_BG_OPACITY, 65).coerceIn(0, 100)

        return UserSettings(
            accentHex = accent,
            countTodayAsRemaining = countToday,
            gridDensity = density,
            showPercent = showPercent,
            widgetBackgroundMode = bgMode,
            widgetBackgroundOpacity = bgOpacity
        )
    }

    fun updateAccent(hex: String) {
        prefs.edit().putString(KEY_ACCENT, hex).apply()
        _settingsFlow.value = _settingsFlow.value.copy(accentHex = hex)
    }

    fun updateCountToday(countTodayAsRemaining: Boolean) {
        prefs.edit().putBoolean(KEY_COUNT_TODAY, countTodayAsRemaining).apply()
        _settingsFlow.value = _settingsFlow.value.copy(countTodayAsRemaining = countTodayAsRemaining)
    }

    fun updateGridDensity(density: GridDensity) {
        prefs.edit().putString(KEY_DENSITY, density.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(gridDensity = density)
    }

    fun updateShowPercent(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_PERCENT, show).apply()
        _settingsFlow.value = _settingsFlow.value.copy(showPercent = show)
    }

    fun updateWidgetBackgroundMode(mode: WidgetBackgroundMode) {
        prefs.edit().putString(KEY_WIDGET_BG_MODE, mode.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(widgetBackgroundMode = mode)
    }

    fun updateWidgetBackgroundOpacity(opacity: Int) {
        val clamped = opacity.coerceIn(0, 100)
        prefs.edit().putInt(KEY_WIDGET_BG_OPACITY, clamped).apply()
        _settingsFlow.value = _settingsFlow.value.copy(widgetBackgroundOpacity = clamped)
    }

    companion object {
        const val PREFS_NAME = "dotday_prefs"
        const val KEY_ACCENT = "accent_hex"
        const val KEY_COUNT_TODAY = "count_today_as_remaining"
        const val KEY_DENSITY = "grid_density"
        const val KEY_SHOW_PERCENT = "show_percent"
        const val KEY_WIDGET_BG_MODE = "widget_bg_mode"
        const val KEY_WIDGET_BG_OPACITY = "widget_bg_opacity"

        fun getAccentColorHex(context: Context): String {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACCENT, "#191F45") ?: "#191F45"
        }

        fun getCountTodayAsRemaining(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_COUNT_TODAY, true)
        }

        fun getWidgetBackgroundMode(context: Context): WidgetBackgroundMode {
            val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_WIDGET_BG_MODE, WidgetBackgroundMode.SOLID.name) ?: WidgetBackgroundMode.SOLID.name
            return try {
                WidgetBackgroundMode.valueOf(name)
            } catch (e: Exception) {
                WidgetBackgroundMode.SOLID
            }
        }

        fun getWidgetBackgroundOpacity(context: Context): Int {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_WIDGET_BG_OPACITY, 65).coerceIn(0, 100)
        }
    }
}
