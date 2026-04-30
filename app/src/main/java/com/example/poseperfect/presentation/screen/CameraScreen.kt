package com.example.poseperfect.presentation.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poseperfect.domain.model.FeedbackItem
import com.example.poseperfect.domain.model.FeedbackPriority
import com.example.poseperfect.domain.model.PoseLandmark
import com.example.poseperfect.domain.model.PoseResult
import com.example.poseperfect.domain.model.PoseTemplate
import com.example.poseperfect.ml.PoseAnalyzer
import com.example.poseperfect.ml.PoseLandmarkIndex
import com.example.poseperfect.presentation.theme.ErrorRed
import com.example.poseperfect.presentation.theme.NeonBlue
import com.example.poseperfect.presentation.theme.NeonGreen
import com.example.poseperfect.presentation.theme.WarningAmber
import com.example.poseperfect.presentation.viewmodel.PoseViewModel
import java.util.concurrent.Executors

// ── Root screen ───────────────────────────────────────────────────────────────

@Composable
fun CameraScreen(viewModel: PoseViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        PermissionScreen { permissionLauncher.launch(Manifest.permission.CAMERA) }
        return
    }

    CameraContent(
        viewModel = viewModel,
        uiState = uiState,
        context = context,
        lifecycleOwner = lifecycleOwner
    )
}

// ── Camera content ────────────────────────────────────────────────────────────

@Composable
private fun CameraContent(
    viewModel: PoseViewModel,
    uiState: com.example.poseperfect.presentation.viewmodel.PoseUiState,
    context: Context,
    lifecycleOwner: LifecycleOwner
) {
    var analyzerRef by remember { mutableStateOf<PoseAnalyzer?>(null) }
    val previewView = remember { PreviewView(context) }

    // Bind / rebind whenever camera facing changes
    DisposableEffect(uiState.isFrontCamera) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = PoseAnalyzer(
            context = context,
            onResult = viewModel::onPoseDetected,
            onNoDetection = viewModel::onNoPoseDetected
        )
        analyzerRef = analyzer

        bindCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            isFront = uiState.isFrontCamera,
            analyzer = analyzer,
            executor = executor
        )

        onDispose {
            analyzer.close()
            executor.shutdown()
            ProcessCameraProvider.getInstance(context).get()?.unbindAll()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Camera preview ─────────────────────────────────────────────────
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // ── Skeleton overlay ───────────────────────────────────────────────
        uiState.poseResult?.let { pose ->
            PoseSkeletonOverlay(
                poseResult = pose,
                isMirrored = uiState.isFrontCamera,
                isOptimal = uiState.isOptimalPose,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Score badge ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isDetecting,
            enter = fadeIn(),
            exit  = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 16.dp)
        ) {
            ScoreBadge(score = uiState.poseScore)
        }

        // ── Template selector ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 56.dp, start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PoseTemplate.entries.forEach { tpl ->
                TemplateChip(
                    template = tpl,
                    selected = tpl == uiState.activeTemplate,
                    onClick = { viewModel.setTemplate(tpl) }
                )
            }
        }

        // ── Feedback panel ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.feedbackItems.isNotEmpty(),
            enter = fadeIn(tween(300)),
            exit  = fadeOut(tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
        ) {
            FeedbackPanel(feedbackItems = uiState.feedbackItems.take(3))
        }

        // ── Bottom controls ────────────────────────────────────────────────
        BottomBar(
            onFlipCamera = viewModel::flipCamera,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

        // ── No-detection hint ──────────────────────────────────────────────
        if (!uiState.isDetecting) {
            Text(
                text = "Point camera at a person",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ── Skeleton drawing ──────────────────────────────────────────────────────────

@Composable
fun PoseSkeletonOverlay(
    poseResult: PoseResult,
    isMirrored: Boolean,
    isOptimal: Boolean,
    modifier: Modifier = Modifier
) {
    val skeletonAlpha by animateFloatAsState(
        targetValue = if (poseResult.isValid()) 1f else 0f,
        animationSpec = tween(200),
        label = "skeletonAlpha"
    )
    val lineColor  = if (isOptimal) NeonGreen else NeonBlue
    val jointColor = if (isOptimal) NeonGreen else Color.White

    Canvas(modifier = modifier) {
        if (!poseResult.isValid() || skeletonAlpha == 0f) return@Canvas

        val lm = poseResult.landmarks

        // Draw connections
        PoseLandmarkIndex.POSE_CONNECTIONS.forEach { (startIdx, endIdx) ->
            val start = lm.getOrNull(startIdx) ?: return@forEach
            val end   = lm.getOrNull(endIdx)   ?: return@forEach
            if (start.visibility < 0.3f && start.visibility > 0f) return@forEach
            if (end.visibility   < 0.3f && end.visibility   > 0f) return@forEach

            drawLine(
                color     = lineColor.copy(alpha = skeletonAlpha * 0.85f),
                start     = lm2px(start, isMirrored),
                end       = lm2px(end, isMirrored),
                strokeWidth = 4f,
                cap       = StrokeCap.Round
            )
        }

        // Draw joints
        lm.forEachIndexed { _, landmark ->
            val vis = landmark.visibility
            if (vis in 0.01f..0.3f) return@forEachIndexed // skip low-confidence

            val pos = lm2px(landmark, isMirrored)
            drawCircle(
                color  = jointColor.copy(alpha = skeletonAlpha),
                radius = 7f,
                center = pos
            )
            drawCircle(
                color  = Color.Black.copy(alpha = skeletonAlpha * 0.6f),
                radius = 4f,
                center = pos
            )
        }
    }
}

/** Convert normalised [0,1] landmark coordinates to canvas pixels. */
private fun DrawScope.lm2px(lm: PoseLandmark, mirrored: Boolean): Offset {
    val x = if (mirrored) (1f - lm.x) else lm.x
    return Offset(x * size.width, lm.y * size.height)
}

// ── Score badge ───────────────────────────────────────────────────────────────

@Composable
fun ScoreBadge(score: Int) {
    val color = when {
        score >= 85 -> NeonGreen
        score >= 60 -> WarningAmber
        else        -> ErrorRed
    }
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text  = "$score",
                color = color,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = "POSE SCORE",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ── Feedback panel ────────────────────────────────────────────────────────────

@Composable
fun FeedbackPanel(feedbackItems: List<FeedbackItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        feedbackItems.forEach { item ->
            val bgColor = when (item.priority) {
                FeedbackPriority.HIGH   -> ErrorRed.copy(alpha = 0.88f)
                FeedbackPriority.MEDIUM -> WarningAmber.copy(alpha = 0.88f)
                FeedbackPriority.LOW    -> NeonGreen.copy(alpha = 0.88f)
            }
            val textColor = when (item.priority) {
                FeedbackPriority.LOW -> Color.Black
                else                 -> Color.White
            }
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(text = item.emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text  = item.message,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Template chip ─────────────────────────────────────────────────────────────

@Composable
fun TemplateChip(
    template: PoseTemplate,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) NeonGreen.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.6f)
    val tc = if (selected) Color.Black else Color.White

    Button(
        onClick = onClick,
        shape   = RoundedCornerShape(20.dp),
        colors  = ButtonDefaults.buttonColors(containerColor = bg),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text     = "${template.emoji} ${template.displayName}",
            color    = tc,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
fun BottomBar(onFlipCamera: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        FilledIconButton(
            onClick = onFlipCamera,
            shape   = CircleShape,
            colors  = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Flip camera",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ── Permission denied screen ──────────────────────────────────────────────────

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier    = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📷", fontSize = 64.sp)
            Text(
                text       = "Camera Permission Required",
                color      = Color.White,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Text(
                text    = "PosePerfect needs access to your camera to analyse your pose in real time.",
                color   = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick  = onRequestPermission,
                colors   = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── CameraX binding ───────────────────────────────────────────────────────────

private fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    isFront: Boolean,
    analyzer: PoseAnalyzer,
    executor: java.util.concurrent.ExecutorService
) {
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
        runCatching {
            val provider = future.get()
            provider.unbindAll()

            val selector = if (isFront)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }

            provider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
        }.onFailure { e ->
            Log.e("CameraScreen", "Camera bind failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

