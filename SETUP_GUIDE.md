# 🚀 PosePerfect - Setup & Run Guide

## Quick Start: Running on Android Device/Emulator

### Prerequisites
- ✅ Android Studio installed
- ✅ Android SDK (API 26+)
- ✅ Gradle 9.4.1 (bundled)
- ✅ Kotlin 2.1.0

---

## Option 1: Run via Android Studio (Recommended) ⭐

### Setup:
1. Open `/Users/shivamkumar/AndroidStudioProjects/PosePerfect` in Android Studio
2. Sync Gradle dependencies (Android Studio will auto-prompt)
3. Wait for indexing to complete

### Launch on Emulator:
**Create Virtual Device (if needed):**
- In Android Studio: **Tools → Device Manager**
- Click **Create Device**
- Choose **Pixel 6** (or similar, API 33+)
- Click **Create**

**Run App:**
1. Select device in Device Manager (or connect physical phone)
2. In top toolbar, select your device from the dropdown
3. Click **Run ▶️** button (or press `Shift+F10`)

---

## Option 2: Run via Terminal

### Prerequisites:
Make sure you have an emulator running or physical device connected.

### Start Emulator (Mac/Linux):
```bash
# List available emulators
~/Library/Android/sdk/emulator/emulator -list-avds

# Start specific emulator (example)
~/Library/Android/sdk/emulator/emulator -avd Pixel_6_API_33 &
```

**If no emulator exists, create one:**
```bash
~/Library/Android/sdk/cmdline-tools/latest/bin/sdkmanager --install "system-images;android-33;default;arm64-v8a"
~/Library/Android/sdk/cmdline-tools/latest/bin/avdmanager create avd -n "Pixel_6_API_33" \
  -k "system-images;android-33;default;arm64-v8a" -d "pixel_6"
```

### Install & Run:
```bash
cd /Users/shivamkumar/AndroidStudioProjects/PosePerfect

# Install on connected device/emulator
./gradlew installDebug

# Launch the app
~/Library/Android/sdk/platform-tools/adb shell am start -n \
  com.example.poseperfect/.MainActivity
```

---

## Option 3: Install APK Directly

### Pre-built APK location:
```
app/build/outputs/apk/debug/app-debug.apk (73 MB)
```

### Install to device:
```bash
~/Library/Android/sdk/platform-tools/adb install \
  app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 First Launch - What to Expect

### Permission Screen:
- App will request **Camera permission**
- Tap **Grant Permission** ✅

### Camera Screen:
- Full-screen camera feed
- Skeleton overlay (if person is detected)
- Top-left: **Pose templates** (Professional, Casual, Power)
- Top-right: **Pose Score** (0-100)
- Bottom: **Feedback messages** + **Flip camera button**

### Using the App:
1. **Stand in front of camera** (1-2 meters away)
2. **View real-time feedback:**
   - 🟢 Green = Optimal
   - 🟡 Orange = Minor adjustments
   - 🔴 Red = Major corrections
3. **Switch templates** to see different pose standards
4. **Flip camera** button at bottom-center

---

## ✅ Checking Installation Success

After launching, you should see:

```
✓ Camera preview loads
✓ Skeleton appears when person is in frame
✓ Feedback messages (e.g., "Straighten your shoulders")
✓ Pose score updates in real-time
✓ No crash or ANR
```

---

## 🐛 Troubleshooting

### APK won't install
```bash
# Clear existing app
~/Library/Android/sdk/platform-tools/adb uninstall com.example.poseperfect

# Rebuild and install
./gradlew clean installDebug
```

### No camera detected
- Ensure emulator has camera enabled: **Device Settings → Camera → Check "Enabled"**
- Or use a physical device with camera

### App crashes on launch
```bash
# View logs
~/Library/Android/sdk/platform-tools/adb logcat | grep -i poseperfect
```

### Slow pose detection
- Camera resolution may be too high
- Try switching to back camera (Flip button)
- Close other CPU-heavy apps

---

## 📊 Performance Targets

- Frame processing: **< 100ms per frame**
- Skeleton overlay: **Smooth 30+ FPS**
- Pose smoothing: **5-frame rolling average**
- Model size: **5.7 MB** (in `/app/src/main/assets/`)

---

## 🎯 Feature Testing Checklist

- [ ] Camera feed displays
- [ ] Pose skeleton renders (arm, leg, torso connections)
- [ ] Shoulder alignment detection works
- [ ] Spine angle calculation accurate
- [ ] Head tilt feedback appears
- [ ] Pose score updates (0-100 range)
- [ ] Switching templates changes feedback
- [ ] Front/back camera flip works
- [ ] Permission handling works
- [ ] No crashes after 5 min usage

---

## 📝 Default Pose Templates

### 💼 Professional (LinkedIn Pose)
- Max shoulder tilt: **5°**
- Min spine angle: **75°**
- Max head tilt: **8°**
- Max body rotation: **15°**

### 😎 Casual
- Max shoulder tilt: **12°**
- Min spine angle: **60°**
- Max head tilt: **15°**
- Max body rotation: **30°**

### 💪 Power Pose
- Max shoulder tilt: **4°**
- Min spine angle: **80°**
- Max head tilt: **5°**
- Max body rotation: **10°**

---

## 🔧 Building from Source

```bash
cd /Users/shivamkumar/AndroidStudioProjects/PosePerfect

# Full clean rebuild
./gradlew clean assembleDebug

# Or with Gradle wrapper
./gradlew assembleRelease  # For Play Store release build
```

---

## 📦 Project Files

### Architecture
```
app/src/main/
├── java/com/example/poseperfect/
│   ├── presentation/  (UI, Compose)
│   ├── domain/       (Logic, Models)
│   ├── data/         (Repository)
│   └── ml/           (MediaPipe, Math)
├── assets/
│   └── pose_landmarker_lite.task  (5.7 MB model)
└── res/              (Themes, Strings, Drawables)
```

### Key Dependencies
- **CameraX 1.4.1** - Camera API
- **MediaPipe 0.10.14** - Pose detection
- **Jetpack Compose 2025.02** - UI
- **Kotlin 2.1.0** - Language

---

## 🚢 GitHub Repository

📍 **https://github.com/shivam2003-dev/PosePerfect**

- All source code
- Build configuration
- Assets (ML model)
- Documentation

---

## 💡 Next Steps

1. **Run on device** using steps above
2. **Test pose detection** with different poses
3. **Customize feedback messages** (in `FeedbackEngine.kt`)
4. **Add new templates** (in `PoseTemplate.kt`)
5. **Build release APK** for Google Play Store

---

**Happy posing!** 📸 ✨

