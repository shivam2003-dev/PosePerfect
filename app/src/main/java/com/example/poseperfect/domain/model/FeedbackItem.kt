package com.example.poseperfect.domain.model

enum class FeedbackPriority { HIGH, MEDIUM, LOW }

/**
 * A single actionable posture correction hint shown to the user.
 */
data class FeedbackItem(
    val message: String,
    val priority: FeedbackPriority = FeedbackPriority.MEDIUM,
    val emoji: String = "⚠️"
)

