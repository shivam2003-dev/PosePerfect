package com.example.poseperfect.domain.model

/**
 * Pre-defined pose templates with tolerance thresholds (all angles in degrees).
 *
 * @param maxShoulderTiltDeg   Max allowed shoulder tilt from horizontal
 * @param minSpineAngleDeg     Min spine angle from vertical (higher = straighter)
 * @param maxHeadTiltDeg       Max allowed head tilt from vertical
 * @param maxBodyRotationDeg   Max allowed body rotation around vertical axis
 */
enum class PoseTemplate(
    val displayName: String,
    val emoji: String,
    val description: String,
    val maxShoulderTiltDeg: Float,
    val minSpineAngleDeg: Float,
    val maxHeadTiltDeg: Float,
    val maxBodyRotationDeg: Float
) {
    PROFESSIONAL(
        displayName = "Professional",
        emoji = "💼",
        description = "LinkedIn / headshot pose — level shoulders, straight spine",
        maxShoulderTiltDeg = 5f,
        minSpineAngleDeg = 75f,
        maxHeadTiltDeg = 8f,
        maxBodyRotationDeg = 15f
    ),
    CASUAL(
        displayName = "Casual",
        emoji = "😎",
        description = "Relaxed everyday pose with generous tolerances",
        maxShoulderTiltDeg = 12f,
        minSpineAngleDeg = 60f,
        maxHeadTiltDeg = 15f,
        maxBodyRotationDeg = 30f
    ),
    POWER(
        displayName = "Power Pose",
        emoji = "💪",
        description = "Commanding presence — squared shoulders, upright spine",
        maxShoulderTiltDeg = 4f,
        minSpineAngleDeg = 80f,
        maxHeadTiltDeg = 5f,
        maxBodyRotationDeg = 10f
    )
}

