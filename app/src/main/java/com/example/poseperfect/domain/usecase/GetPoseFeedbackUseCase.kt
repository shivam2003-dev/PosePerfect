package com.example.poseperfect.domain.usecase

import com.example.poseperfect.domain.model.FeedbackItem
import com.example.poseperfect.domain.model.PoseResult
import com.example.poseperfect.domain.model.PoseTemplate
import com.example.poseperfect.ml.FeedbackEngine
import com.example.poseperfect.ml.PoseLandmarkIndex.LEFT_HIP
import com.example.poseperfect.ml.PoseLandmarkIndex.LEFT_SHOULDER
import com.example.poseperfect.ml.PoseLandmarkIndex.NOSE
import com.example.poseperfect.ml.PoseLandmarkIndex.RIGHT_HIP
import com.example.poseperfect.ml.PoseLandmarkIndex.RIGHT_SHOULDER
import com.example.poseperfect.ml.PoseMath
import kotlin.math.abs

/**
 * Orchestrates pose analysis: smoothing → feedback generation → scoring.
 */
class GetPoseFeedbackUseCase {

    private val feedbackEngine = FeedbackEngine()
    private val recentLandmarksBuffer = ArrayDeque<List<com.example.poseperfect.domain.model.PoseLandmark>>()

    fun setTemplate(template: PoseTemplate) = feedbackEngine.setTemplate(template)

    data class Result(
        val smoothedPose: PoseResult,
        val feedback: List<FeedbackItem>,
        val score: Int
    )

    operator fun invoke(raw: PoseResult): Result {
        val smoothed = smooth(raw)
        val feedback = feedbackEngine.analyze(smoothed)
        val score = score(smoothed)
        return Result(smoothed, feedback, score)
    }

    // ── Landmark smoothing (rolling average) ──────────────────────────────────

    private fun smooth(current: PoseResult): PoseResult {
        if (!current.isValid()) return current

        recentLandmarksBuffer.addLast(current.landmarks)
        if (recentLandmarksBuffer.size > SMOOTHING_WINDOW) recentLandmarksBuffer.removeFirst()
        if (recentLandmarksBuffer.size < 2) return current

        val smoothed = current.landmarks.mapIndexed { idx, lm ->
            val avgX = recentLandmarksBuffer.map { it[idx].x }.average().toFloat()
            val avgY = recentLandmarksBuffer.map { it[idx].y }.average().toFloat()
            val avgZ = recentLandmarksBuffer.map { it[idx].z }.average().toFloat()
            lm.copy(x = avgX, y = avgY, z = avgZ)
        }
        return current.copy(landmarks = smoothed)
    }

    // ── Scoring ───────────────────────────────────────────────────────────────

    private fun score(pose: PoseResult): Int {
        if (!pose.isValid()) return 0
        val l = pose.landmarks
        val ls = l[LEFT_SHOULDER]; val rs = l[RIGHT_SHOULDER]
        val lh = l[LEFT_HIP];      val rh = l[RIGHT_HIP]
        val nose = l[NOSE]

        val shoulderTilt  = abs(PoseMath.shoulderTiltDegrees(ls, rs))
        val spineDeviation = PoseMath.spineAngleFromVertical(ls, rs, lh, rh)
        val headTilt      = PoseMath.headTiltDegrees(nose, ls, rs)
        val bodyRotation  = PoseMath.bodyRotationDegrees(ls, rs)

        return PoseMath.calculatePoseScore(shoulderTilt, spineDeviation, headTilt, bodyRotation)
    }

    companion object {
        private const val SMOOTHING_WINDOW = 5
    }
}

