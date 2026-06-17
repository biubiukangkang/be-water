package com.pomodorotimer.ui.theme

import androidx.compose.ui.graphics.Color

// Light mode - web v4
val LightBackground = Color(0xFFF5F0EB)
val LightSurface = Color.Black.copy(alpha = 0.03f)
val LightOnBackground = Color(0xFF2C2420)
val LightOnSurface = Color(0xFF2C2420)
val LightSecondary = Color(0xFF2C2420).copy(alpha = 0.45f)
val LightPrimary = Color(0xFF2C2420).copy(alpha = 0.08f)
val LightPrimaryVariant = Color(0xFF2C2420).copy(alpha = 0.15f)
val LightAccent = Color(0xFF2C2420).copy(alpha = 0.35f)
val LightCard = Color.Black.copy(alpha = 0.03f)

// Dark mode
val DarkBackground = Color(0xFF2A2520)
val DarkSurface = Color.White.copy(alpha = 0.06f)
val DarkOnBackground = Color(0xFFF0EAE4)
val DarkOnSurface = Color(0xFFF0EAE4)
val DarkSecondary = Color(0xFFF0EAE4).copy(alpha = 0.5f)
val DarkPrimary = Color(0xFFF0EAE4).copy(alpha = 0.2f)
val DarkPrimaryVariant = Color(0xFFF0EAE4).copy(alpha = 0.35f)
val DarkAccent = Color(0xFFF0EAE4).copy(alpha = 0.5f)
val DarkCard = Color.White.copy(alpha = 0.06f)

// Ring colors
val RingTrackLight = Color(0xFF2C2420).copy(alpha = 0.1f)
val RingFillLight = Color(0xFF2C2420).copy(alpha = 0.3f)
val RingCompleteLight = Color(0xFF2C2420).copy(alpha = 0.2f)

val RingTrackDark = Color(0xFFF0EAE4).copy(alpha = 0.12f)
val RingFillDark = Color(0xFFF0EAE4).copy(alpha = 0.5f)
val RingCompleteDark = Color(0xFFF0EAE4).copy(alpha = 0.35f)

// Legacy names used by components
val ProgressComplete = RingCompleteDark
val ProgressEmpty = RingTrackLight
val ProgressEmptyDark = RingTrackDark
