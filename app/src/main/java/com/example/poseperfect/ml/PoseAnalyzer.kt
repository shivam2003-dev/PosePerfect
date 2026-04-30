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
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * CameraX ImageAnalysis.Analyzer that runs MediaPipe Pose Landmarker
 * on each frame and forwards results to onResult.
 *
 * The model file pose_landmarker_lite.task must be placed in src/main/assets/.
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

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result: PoseLandmarkerResult, _: MPImage ->
                handleResult(result)
            }
            .setErrorListener { error: RuntimeException ->
                Log.e(TAG, "MediaPipe error: ${error.message}", error)
            }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap().let { bmp ->
            val rotation = imageProxy.imageInfo.rotationDegrees
            if (rotation != 0) rotateBitmap(bmp, rotation) else bmp
        }
        imageProxy.close()

        val mpImage = BitmapImageBuilder(bitmap).build()
        poseLandmarker.detectAsync(mpImage, SystemClock.uptimeMillis())
    }

    private fun handleResult(result: PoseLandmarkerResult) {
        val poseList = result.landmarks()
        if (poseList.isEmpty()) {
            onNoDetection()
            return
        }

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

    fun close() {
        runCatching { poseLandmarker.close() }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    companion object {
        private const val TAG = "PoseAnalyzer"
        private const val MODEL_ASSET_PATH = "pose_landmarker_lite.task"
    }
}

