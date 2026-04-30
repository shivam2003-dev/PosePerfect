package com.example.poseperfect.domain.model

/**
 * Normalized landmark with coordinates in [0, 1] range.
 * z is relative depth (negative = closer to camera).
 * visibility in [0, 1]; higher = more confident the landmark is visible.
 */
data class PoseLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float = 0f
)

