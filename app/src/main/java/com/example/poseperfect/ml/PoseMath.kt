package com.example.poseperfect.ml

import com.example.poseperfect.domain.model.PoseLandmark
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Pure math utilities for pose analysis.
 * All angle functions return degrees.
 */
object PoseMath {

    // ── Vector helpers ────────────────────────────────────────────────────────

    private fun length(dx: Float, dy: Float) = sqrt(dx * dx + dy * dy)

    /**
     * Angle (degrees) at [vertex] formed by rays [vertex→a] and [vertex→b].
     */
    fun angleBetween(
        vertex: PoseLandmark,
        a: PoseLandmark,
        b: PoseLandmark
    ): Float {
        val ax = a.x - vertex.x; val ay = a.y - vertex.y
        val bx = b.x - vertex.x; val by = b.y - vertex.y
        val dot = ax * bx + ay * by
        val denom = length(ax, ay) * length(bx, by)
        if (denom < 1e-6f) return 0f
        return Math.toDegrees(acos((dot / denom).coerceIn(-1f, 1f)).toDouble()).toFloat()
    }

    // ── Shoulder alignment ────────────────────────────────────────────────────

    /**
     * Tilt of the shoulder line from horizontal.
     * 0° = perfectly level; positive = right shoulder dips; negative = left shoulder dips.
     * (In image space y increases downward, so this is the signed tilt in degrees.)
     */
    fun shoulderTiltDegrees(
        leftShoulder: PoseLandmark,
        rightShoulder: PoseLandmark
    ): Float {
        val dx = rightShoulder.x - leftShoulder.x
        val dy = rightShoulder.y - leftShoulder.y
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }

    // ── Spine angle ───────────────────────────────────────────────────────────

    /**
     * Angle of the spine from vertical (0° = spine perfectly vertical / upright).
     * Bigger deviation = more lateral lean.
     */
    fun spineAngleFromVertical(
        leftShoulder: PoseLandmark,
        rightShoulder: PoseLandmark,
        leftHip: PoseLandmark,
        rightHip: PoseLandmark
    ): Float {
        val smx = (leftShoulder.x + rightShoulder.x) / 2f
        val smy = (leftShoulder.y + rightShoulder.y) / 2f
        val hmx = (leftHip.x + rightHip.x) / 2f
        val hmy = (leftHip.y + rightHip.y) / 2f
        val dx = smx - hmx
        val dy = smy - hmy       // negative → shoulders above hips (normal)
        // Angle from vertical = atan2(horizontal component, vertical component)
        return Math.toDegrees(atan2(abs(dx).toDouble(), max(abs(dy).toDouble(), 1e-4)).toDouble()).toFloat()
    }

    // ── Head tilt ─────────────────────────────────────────────────────────────

    /**
     * Lateral head tilt: angle between nose–shoulder-midpoint vector and vertical.
     * 0° = head perfectly upright.
     */
    fun headTiltDegrees(
        nose: PoseLandmark,
        leftShoulder: PoseLandmark,
        rightShoulder: PoseLandmark
    ): Float {
        val smx = (leftShoulder.x + rightShoulder.x) / 2f
        val smy = (leftShoulder.y + rightShoulder.y) / 2f
        val dx = nose.x - smx
        val dy = smy - nose.y   // positive = nose above shoulders (expected)
        return Math.toDegrees(atan2(abs(dx).toDouble(), max(abs(dy).toDouble(), 1e-4)).toDouble()).toFloat()
    }

    // ── Body rotation ─────────────────────────────────────────────────────────

    /**
     * Approximate body rotation around the vertical axis using z-depth difference.
     * Returns degrees; >0 means body is rotated.
     */
    fun bodyRotationDegrees(
        leftShoulder: PoseLandmark,
        rightShoulder: PoseLandmark
    ): Float {
        val zDiff = abs(leftShoulder.z - rightShoulder.z)
        val shoulderWidth = abs(leftShoulder.x - rightShoulder.x)
        if (shoulderWidth < 0.01f) return 0f
        return Math.toDegrees(atan2(zDiff.toDouble(), shoulderWidth.toDouble())).toFloat()
    }

    // ── Pose quality score ────────────────────────────────────────────────────

    /**
     * Score from 0–100 representing overall pose quality.
     * Deducts points based on each metric's deviation from ideal.
     */
    fun calculatePoseScore(
        shoulderTiltAbs: Float,
        spineDeviation: Float,
        headTiltAbs: Float,
        bodyRotation: Float
    ): Int {
        var score = 100f
        // Each metric can deduct up to 25 points
        score -= (shoulderTiltAbs / 20f * 25f).coerceAtMost(25f)  // 20° = max penalty
        score -= (spineDeviation / 30f * 25f).coerceAtMost(25f)   // 30° lean = max penalty
        score -= (headTiltAbs / 25f * 25f).coerceAtMost(25f)      // 25° tilt = max penalty
        score -= (bodyRotation / 40f * 25f).coerceAtMost(25f)     // 40° rotation = max penalty
        return score.toInt().coerceIn(0, 100)
    }
}

