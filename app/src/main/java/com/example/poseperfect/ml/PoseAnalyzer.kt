package com.example.poseperfect.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.poseperfect.domain.model.PoseLandmark
import com.example.poseperfect.domain.model.PoseResult
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerOptions
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * CameraX [ImageAnalysis.Analyzer] that runs MediaPipe Pose Landmarker
 * on each frame and forwards results to [onResult].
 *
 * The model file `pose_landmarker_lite.task` must be placed in src/main/assets/.
 * Download: https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task
 */
class PoseAnalyzer(
    context: Context,
    private val onResult: (PoseResult) -> Unit,
    private val onNoDetection: () -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val poseLandmarker: PoseLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_ASSET_PATH)
            .build()

        val options = PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener(::handleResult)
            .setErrorListener { error ->
                Log.e(TAG, "MediaPipe error: ${error.message}", error)
            }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    // ── ImageAnalysis.Analyzer ────────────────────────────────────────────────

    override fun analyze(imageProxy: ImageProxy) {
        // Convert ImageProxy → Bitmap (ARGB_8888), rotate to upright orientation
        val bitmap = imageProxy.toBitmap().let { bmp ->
            val rotation = imageProxy.imageInfo.rotationDegrees
            if (rotation != 0) rotateBitmap(bmp, rotation) else bmp
        }
        imageProxy.close()  // safe to close — toBitmap() copies pixel data

        val mpImage = BitmapImageBuilder(bitmap).build()
        // detectAsync is non-blocking; result arrives via resultListener callback
        poseLandmarker.detectAsync(mpImage, SystemClock.uptimeMillis())
    }

    // ── Result handling ───────────────────────────────────────────────────────

    private fun handleResult(result: PoseLandmarkerResult, @Suppress("UNUSED_PARAMETER") input: Any) {
        val poseList = result.landmarks()
        if (poseList.isEmpty()) {
            onNoDetection()
            return
        }

        // Convert MediaPipe NormalizedLandmarks → domain model
        val landmarks: List<PoseLandmark> = poseList[0].map { lm ->
            PoseLandmark(
                x = lm.x(),
                y = lm.y(),
                z = lm.z(),
                visibility = lm.visibility().orElse(0f)
            )
        }

        onResult(PoseResult(landmarks = landmarks))
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun close() {
        runCatching { poseLandmarker.close() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    companion object {
        private const val TAG = "PoseAnalyzer"
        private const val MODEL_ASSET_PATH = "pose_landmarker_lite.task"
    }
}

