package com.example.data.model

enum class GridDensity(val label: String, val spacingDp: Float, val dotRadiusDp: Float, val columns: Int) {
    COMPACT("Dense (25 cols)", 3.5f, 4f, 25),
    STANDARD("Standard (16 cols)", 5f, 5.5f, 16),
    SPACIOUS("Spacious (12 cols)", 7f, 6.5f, 12)
}

enum class WidgetBackgroundMode(val label: String, val description: String) {
    SOLID("Solid Charcoal", "Charcoal #202126 surface with rounded corners"),
    TRANSPARENT("Transparent", "Removes app background to show home screen wallpaper"),
    TRANSLUCENT("Translucent", "Charcoal tint with adjustable opacity; no live wallpaper blur")
}

data class UserSettings(
    val accentHex: String = "#191F45",
    val countTodayAsRemaining: Boolean = true,
    val gridDensity: GridDensity = GridDensity.COMPACT,
    val showPercent: Boolean = true,
    val widgetBackgroundMode: WidgetBackgroundMode = WidgetBackgroundMode.SOLID,
    val widgetBackgroundOpacity: Int = 65 // 0 to 100 percentage
) {
    companion object {
        val DEFAULT = UserSettings()

        val PRESET_ACCENTS = listOf(
            "#191F45" to "Midnight Navy",
            "#A99AFF" to "Soft Lavender",
            "#7AE2C0" to "Neo Mint",
            "#FFB347" to "Electric Amber",
            "#FF8BA7" to "Rose Quartz",
            "#5CE1E6" to "Cyber Cyan",
            "#E2E2EA" to "Minimal Silver"
        )
    }
}
