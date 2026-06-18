package com.pomodorotimer.ui.theme

import androidx.compose.ui.graphics.Color

// Light mode - web v4 (alphas boosted for no-blur Compose)
val LightBackground = Color(0xFFF5F0EB)
val LightSurface = Color.Black.copy(alpha = 0.06f)
val LightOnBackground = Color(0xFF2C2420)
val LightOnSurface = Color(0xFF2C2420)
val LightSecondary = Color(0xFF2C2420).copy(alpha = 0.6f)
val LightPrimary = Color(0xFFFFFFFF).copy(alpha = 0.40f)  // white glass for buttons
val LightPrimaryVariant = Color(0xFF2C2420).copy(alpha = 0.35f)
val LightAccent = Color(0xFF2C2420).copy(alpha = 0.45f)
val LightCard = Color.Black.copy(alpha = 0.06f)

// Dark mode (alphas boosted 2-3x to compensate for no backdrop-filter)
val DarkBackground = Color(0xFF2A2520)
val DarkSurface = Color.White.copy(alpha = 0.12f)
val DarkOnBackground = Color(0xFFF0EAE4)
val DarkOnSurface = Color(0xFFF0EAE4)
val DarkSecondary = Color(0xFFF0EAE4).copy(alpha = 0.65f)
val DarkPrimary = Color(0xFFF0EAE4).copy(alpha = 0.35f)
val DarkPrimaryVariant = Color(0xFFF0EAE4).copy(alpha = 0.5f)
val DarkAccent = Color(0xFFF0EAE4).copy(alpha = 0.6f)
val DarkCard = Color.White.copy(alpha = 0.12f)

// Ring colors (alphas boosted for visibility without blur)
val RingTrackLight = Color(0xFF2C2420).copy(alpha = 0.18f)
val RingFillLight = Color(0xFF2C2420).copy(alpha = 0.45f)
val RingCompleteLight = Color(0xFF2C2420).copy(alpha = 0.3f)

val RingTrackDark = Color(0xFFF0EAE4).copy(alpha = 0.22f)
val RingFillDark = Color(0xFFF0EAE4).copy(alpha = 0.6f)
val RingCompleteDark = Color(0xFFF0EAE4).copy(alpha = 0.45f)

// Legacy names used by components
val ProgressComplete = RingCompleteDark
val ProgressEmpty = RingTrackLight
val ProgressEmptyDark = RingTrackDark
