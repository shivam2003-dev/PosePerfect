package com.example.poseperfect.ml

import com.example.poseperfect.domain.model.FeedbackItem
import com.example.poseperfect.domain.model.FeedbackPriority
import com.example.poseperfect.domain.model.PoseLandmark
import com.example.poseperfect.domain.model.PoseResult
import com.example.poseperfect.domain.model.PoseTemplate
import com.example.poseperfect.ml.PoseLandmarkIndex.LEFT_HIP
import com.example.poseperfect.ml.PoseLandmarkIndex.LEFT_SHOULDER
import com.example.poseperfect.ml.PoseLandmarkIndex.NOSE
import com.example.poseperfect.ml.PoseLandmarkIndex.RIGHT_HIP
import com.example.poseperfect.ml.PoseLandmarkIndex.RIGHT_SHOULDER
import kotlin.math.abs

/**
 * Rule-based feedback engine.
 * Analyses a [PoseResult] against the active [PoseTemplate] and emits
 * a prioritised list of [FeedbackItem]s.
 */
class FeedbackEngine(private var activeTemplate: PoseTemplate = PoseTemplate.PROFESSIONAL) {

    /** Switch active pose template at runtime. */
    fun setTemplate(template: PoseTemplate) {
        activeTemplate = template
    }

    /**
     * Analyse [poseResult] and return actionable feedback sorted by priority.
     */
    fun analyze(poseResult: PoseResult): List<FeedbackItem> {
        if (!poseResult.isValid()) return emptyList()

        val l = poseResult.landmarks

        val nose = l[NOSE]
        val ls = l[LEFT_SHOULDER]
        val rs = l[RIGHT_SHOULDER]
        val lh = l[LEFT_HIP]
        val rh = l[RIGHT_HIP]

        // Require shoulders to be visible
        if (!isVisible(ls) || !isVisible(rs)) {
            return listOf(
                FeedbackItem(
                    "Step back — show your full shoulders",
                    FeedbackPriority.HIGH,
                    "📏"
                )
            )
        }

        val feedback = mutableListOf<FeedbackItem>()
        val t = activeTemplate

        // ── 1. Shoulder alignment ─────────────────────────────────────────────
        val shoulderTilt = PoseMath.shoulderTiltDegrees(ls, rs)
        val shoulderTiltAbs = abs(shoulderTilt)

        if (shoulderTiltAbs > t.maxShoulderTiltDeg) {
            when {
                shoulderTiltAbs > 15f -> feedback += FeedbackItem(
                    "Straighten your shoulders — they are very uneven",
                    FeedbackPriority.HIGH, "🔄"
                )
                shoulderTiltAbs > t.maxShoulderTiltDeg * 1.5f -> feedback += FeedbackItem(
                    "Straighten your shoulders",
                    FeedbackPriority.MEDIUM, "🔄"
                )
                else -> feedback += FeedbackItem(
                    if (shoulderTilt > 0)
                        "Lower your left shoulder slightly"
                    else
                        "Lower your right shoulder slightly",
                    FeedbackPriority.LOW, "↔️"
                )
            }
        }

        // ── 2. Spine angle ────────────────────────────────────────────────────
        if (isVisible(lh) && isVisible(rh)) {
            val spineDeviation = PoseMath.spineAngleFromVertical(ls, rs, lh, rh)

            if (spineDeviation > (90f - t.minSpineAngleDeg)) {
                when {
                    spineDeviation > 30f -> feedback += FeedbackItem(
                        "Stand up straight — you're slouching significantly",
                        FeedbackPriority.HIGH, "🏃"
                    )
                    spineDeviation > 15f -> feedback += FeedbackItem(
                        "Straighten your back",
                        FeedbackPriority.MEDIUM, "⬆️"
                    )
                    else -> feedback += FeedbackItem(
                        "Subtle lean detected — straighten slightly",
                        FeedbackPriority.LOW, "📐"
                    )
                }
            }
        }

        // ── 3. Head tilt ──────────────────────────────────────────────────────
        val headTilt = PoseMath.headTiltDegrees(nose, ls, rs)

        if (headTilt > t.maxHeadTiltDeg) {
            when {
                headTilt > 20f -> feedback += FeedbackItem(
                    "Keep your head level — large forward/back tilt detected",
                    FeedbackPriority.HIGH, "👆"
                )
                headTilt > t.maxHeadTiltDeg * 1.5f -> feedback += FeedbackItem(
                    "Tilt your chin slightly up",
                    FeedbackPriority.MEDIUM, "🔼"
                )
                else -> feedback += FeedbackItem(
                    "Keep your chin level",
                    FeedbackPriority.LOW, "😐"
                )
            }
        }

        // ── 4. Body rotation ──────────────────────────────────────────────────
        val bodyRotation = PoseMath.bodyRotationDegrees(ls, rs)

        if (bodyRotation > t.maxBodyRotationDeg) {
            val rotationInt = bodyRotation.toInt()
            val direction = if (ls.z > rs.z) "right" else "left"
            feedback += FeedbackItem(
                "Turn your body ~${rotationInt}° to the $direction",
                FeedbackPriority.MEDIUM, "↩️"
            )
        }

        // ── Perfect pose ──────────────────────────────────────────────────────
        if (feedback.isEmpty()) {
            feedback += FeedbackItem("Perfect pose! Hold it! 🎉", FeedbackPriority.LOW, "✅")
        }

        return feedback.sortedWith(
            compareByDescending { it.priority.ordinal }
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * A landmark is considered visible if its confidence > 0.5, or if the model
     * did not provide visibility info at all (visibility == 0f default).
     */
    private fun isVisible(lm: PoseLandmark): Boolean =
        lm.visibility < 0.01f || lm.visibility >= 0.5f
}

