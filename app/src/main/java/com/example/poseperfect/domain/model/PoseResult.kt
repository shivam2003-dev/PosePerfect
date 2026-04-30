package com.example.poseperfect.domain.model

/**
 * A single-frame pose detection result containing 33 MediaPipe landmarks.
 */
data class PoseResult(
    val landmarks: List<PoseLandmark>,
    val timestampMs: Long = System.currentTimeMillis()
) {
    /** Returns true if we have all 33 MediaPipe landmarks. */
    fun isValid(): Boolean = landmarks.size == 33

    fun getLandmark(index: Int): PoseLandmark? = landmarks.getOrNull(index)
}

