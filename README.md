# PosePerfect 🏃‍♂️✨

> **Real-time AI pose correction app** — powered by MediaPipe Pose Landmarker and CameraX.

---

## What It Does

| Feature | Details |
|---|---|
| Live camera feed | CameraX (front/back) |
| Pose detection | MediaPipe Pose Landmarker Lite (on-device, <100 ms) |
| Skeleton overlay | 33 joints + body connections drawn in Compose Canvas |
| Real-time feedback | Rule-based engine — shoulder level, spine angle, head tilt, body rotation |
| Pose score | 0–100 quality badge |
| Pose templates | Professional 💼 · Casual 😎 · Power 💪 |
| Smooth tracking | 5-frame rolling average to reduce jitter |

---

## Project Structure

```
app/src/main/java/com/example/poseperfect/
├── MainActivity.kt
├── presentation/
│   ├── screen/         CameraScreen.kt   ← full Compose UI + overlay drawing
│   ├── viewmodel/      PoseViewModel.kt  ← state holder
│   └── theme/          Theme.kt          ← dark neon theme
├── domain/
│   ├── model/          PoseLandmark, PoseResult, FeedbackItem, PoseTemplate
│   └── usecase/        GetPoseFeedbackUseCase.kt  ← smooth + analyse + score
├── data/
│   └── repository/     PoseRepository.kt
└── ml/
    ├── PoseLandmarkIndex.kt   33 landmark indices + skeleton connections
    ├── PoseMath.kt            Vector math: shoulder tilt, spine, head, rotation
    ├── FeedbackEngine.kt      Rule-based posture analyser
    └── PoseAnalyzer.kt        CameraX ImageAnalysis.Analyzer + MediaPipe bridge
```

---

## Setup Instructions

### 1. Prerequisites

| Tool | Minimum Version |
|---|---|
| Android Studio | Meerkat / 2024.3+ |
| Android SDK | API 24+ (target 36) |
| Gradle | 9.4.1 (bundled in wrapper) |
| Kotlin | 2.1.0 |

### 2. Clone and open

```bash
git clone <your-repo>
cd PosePerfect
# Open in Android Studio: File → Open → select the PosePerfect folder
```

### 3. Download the ML model (already done if you ran the script)

```bash
bash download_model.sh
```

The model (`pose_landmarker_lite.task`, ~5.7 MB) is placed in:
```
app/src/main/assets/pose_landmarker_lite.task
```

### 4. Build & Run

1. Connect a physical Android device (API 24+) — **real device recommended** for camera
2. In Android Studio click **▶ Run** (or `Shift+F10`)
3. Grant camera permission when prompted
4. Stand 1–2 metres from the camera and hold a pose

---

## Key Classes

### `PoseAnalyzer`
- Implements `ImageAnalysis.Analyzer`
- Converts each `ImageProxy` frame to a `Bitmap` (with rotation correction)
- Calls `PoseLandmarker.detectAsync()` — non-blocking, result comes back on listener
- Maps 33 MediaPipe `NormalizedLandmark`s → domain `PoseLandmark`s

### `FeedbackEngine`
Rule-based checks against the selected `PoseTemplate` thresholds:

| Metric | How it's computed |
|---|---|
| Shoulder tilt | `atan2(Δy, Δx)` of the shoulder line — ideal = 0° |
| Spine deviation | `atan2(|Δx|, |Δy|)` of shoulder-mid → hip-mid vector from vertical |
| Head tilt | `atan2(|Δx|, |Δy|)` of nose → shoulder-mid from vertical |
| Body rotation | `atan2(|Δz|, shoulder_width)` using MediaPipe's z-depth |

### `GetPoseFeedbackUseCase`
Combines:
1. **5-frame smoothing** — rolling average of landmark x/y/z to reduce jitter
2. **FeedbackEngine analysis** → `List<FeedbackItem>`
3. **PoseMath.calculatePoseScore()** → `Int` 0–100

### `CameraScreen` (Compose)
- Full-screen `Box`: camera preview + skeleton `Canvas` + floating UI cards
- `DisposableEffect(isFrontCamera)` rebinds CameraX when camera flips
- Skeleton draw maps normalised [0,1] coords → canvas pixels; x is mirrored for front cam

---

## Changing Thresholds

Edit `PoseTemplate.kt`:

```kotlin
PROFESSIONAL(
    maxShoulderTiltDeg = 5f,   // ← tighten/loosen as needed
    minSpineAngleDeg   = 75f,
    maxHeadTiltDeg     = 8f,
    maxBodyRotationDeg = 15f
)
```

Add new templates by adding new enum entries.

---

## Performance Notes

- **STRATEGY_KEEP_ONLY_LATEST** in `ImageAnalysis` drops queued frames to avoid lag
- **LIVE_STREAM** mode in MediaPipe is non-blocking: `detectAsync` returns immediately
- 5-frame rolling average adds ~3 ms of compute but removes jitter
- Target inference latency: **<80 ms** on mid-range devices

---

## Permissions

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Requested at runtime via `ActivityResultContracts.RequestPermission`.

