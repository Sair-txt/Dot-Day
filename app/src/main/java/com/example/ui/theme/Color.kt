package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Dot Day Strict Design System Palette
 * Single Source of Truth
 */

// Neutral Foundation
val Background = Color(0xFF111216)              // App background: #111216
val DeepCharcoal = Background                   // Backward-compatibility alias
val SurfacePrimary = Color(0xFF202126)          // Primary surface / widget surface: #202126
val WidgetSurface = SurfacePrimary              // Widget surface alias
val SurfaceCard = SurfacePrimary                // Card surface alias
val SurfaceDark = Background                    // Screen dark background alias
val ElevatedSurface = Color(0xFF292A30)         // Elevated surface: #292A30
val SurfaceElevated = ElevatedSurface           // Alias
val SurfaceHighlighted = Color(0xFF32333A)      // Pressed or highlighted neutral surface: #32333A

// Text Hierarchy
val TextPrimary = Color(0xFFF5F5F7)             // Primary text: #F5F5F7
val TextSecondary = Color(0xFFA3A4AD)           // Secondary text: #A3A4AD
val TextTertiary = Color(0xFF6D6F78)            // Tertiary and disabled text: #6D6F78

// Borders & Dividers
val OutlineMuted = Color(0xFF373840)            // Dividers and subtle outlines: #373840
val DividerColor = OutlineMuted

// Dot Visualization States
val CompletedDotColor = Color(0xFFF5F5F7)       // Completed-day dots: #F5F5F7
val TodayDotColor = Color(0xFF191F45)           // Current-day dot center: Midnight Navy #191F45
val TodayDotRingColor = Color(0xFFF5F5F7)       // Current-day dot outer ring: Near-white #F5F5F7
val FutureDotColor = Color(0xFF3D3E45)          // Future-day dots: #3D3E45

// Brand Accent
val PrimaryNavy = Color(0xFF191F45)             // Brand Accent (Midnight Navy): #191F45
val BrandAccent = PrimaryNavy
val AccentForeground = Color(0xFFFFFFFF)        // Foreground on accent: #FFFFFF
val PrimaryNavyLight = Color(0xFF41476B)        // Understated accent outline: #41476B
val AccentOutline = PrimaryNavyLight

// Optional Personalization Accents for Event Highlights
val AccentMidnightNavy = PrimaryNavy
val AccentLavender = Color(0xFFA99AFF)
val AccentNeoMint = Color(0xFF7AE2C0)
val AccentElectricAmber = Color(0xFFFFB347)
val AccentRoseQuartz = Color(0xFFFF8BA7)
val AccentCyberCyan = Color(0xFF5CE1E6)
val AccentSilver = Color(0xFFE2E2EA)
val SoftLavender = CompletedDotColor            // Re-routed to neutral completed dot color
