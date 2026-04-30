## 🎯 PosePerfect - How to Run on Emulator

Since no emulator is currently connected on this Mac, here's exactly how to run it:

### ✅ Steps to Run (Do This):

#### **Method 1: Android Studio (Easiest - Recommended)**

1. **Open the project in Android Studio:**
   ```bash
   open -a "Android Studio" /Users/shivamkumar/AndroidStudioProjects/PosePerfect
   ```

2. **Wait for Gradle sync** (should be automatic)

3. **Create Virtual Device** (if you don't have one):
   - Top menu: **Tools** → **Device Manager**
   - Click **Create Device**
   - Select "Pixel 6" or "Pixel 5"
   - Choose API level 33 or 34
   - Click **Next** → **Finish**

4. **Start the emulator:**
   - In Device Manager, click the Play button ▶️ next to your device
   - Wait ~30 seconds for emulator to boot

5. **Run the app:**
   - Select your emulator from the device dropdown (top toolbar)
   - Click the green **Run ▶️** button
   - Or press `Shift + F10`
   - Wait ~1-2 minutes for build

6. **Allow camera permission:**
   - When app launches, tap **Grant Permission**

7. **Test the app:**
   - Point the emulator's mock camera at your screen
   - Or tap the emulator screen to simulate a person
   - Watch the skeleton overlay + feedback!

---

#### **Method 2: Via Terminal (Advanced)**

```bash
cd /Users/shivamkumar/AndroidStudioProjects/PosePerfect

# Start emulator (assuming you have one named "Pixel_6_API_33")
~/Library/Android/sdk/emulator/emulator -avd Pixel_6_API_33 &

# Wait a few seconds for emulator to boot
sleep 30

# Install and run app
./gradlew installDebug launchDebug
```

---

#### **Method 3: Install Pre-built APK**

```bash
# APK already built at:
# /Users/shivamkumar/AndroidStudioProjects/PosePerfect/app/build/outputs/apk/debug/app-debug.apk

# Start emulator first
~/Library/Android/sdk/emulator/emulator -avd Pixel_6_API_33 &

# Then install
~/Library/Android/sdk/platform-tools/adb install \
  app/build/outputs/apk/debug/app-debug.apk

# Launch app
~/Library/Android/sdk/platform-tools/adb shell am start -n \
  com.example.poseperfect/.MainActivity
```

---

### 📊 What You'll See:

```
┌─────────────────────────────────────────┐
│  POSEPERFECT Camera Screen              │
├─────────────────────────────────────────┤
│                                         │
│  [💼 Professional 😎 Casual 💪 Power] │ ← Templates (tap to switch)
│                                         │
│  ╔════════════════════════════════╗    │
│  ║                                ║    │
│  ║   Camera Preview               ║    │ ← Live feed with
│  ║   (with skeleton overlay)      ║    │   skeleton
│  ║                                ║    │
│  ╚════════════════════════════════╝    │
│                                    85    │ ← Score badge
│                                         │
│  ⚠️  "Straighten your shoulders"       │ ← Feedback
│  ⚠️  "Keep your head level"            │
│                                         │
│              [↔️ Flip Camera]           │ ← Control button
│                                         │
└─────────────────────────────────────────┘
```

---

### ✨ Features to Test:

- [ ] Camera preview loads (shows black screen initially)
- [ ] When you move: skeleton appears (circles for joints, lines for connections)
- [ ] Feedback messages pop update in real-time
- [ ] Pose score badge appears and updates (0-100)
- [ ] Tap a template chip → feedback changes based on template
- [ ] Tap flip button → switches between front/back camera
- [ ] Tap "Grant Permission" → app works

---

### 🎮 Emulator Camera Simulation:

In the emulator, you can simulate camera input:

1. **Extended Controls** (in emulator):
   - Right side: Click `...` (More) → **Camera**
   - Set to "Virtual scene"
   - This gives emulator a mock pose

2. **Or just test:**
   - Tap screen to dismiss permission dialog
   - You'll see whatever the emulator's default camera returns

---

### 🐛 If Something Fails:

**App won't build:**
```bash
./gradlew clean assembleDebug
```

**APK won't install:**
```bash
~/Library/Android/sdk/platform-tools/adb uninstall com.example.poseperfect
./gradlew installDebug
```

**Check logs:**
```bash
~/Library/Android/sdk/platform-tools/adb logcat | grep -i poseperfect
```

**Emulator won't start:**
```bash
# List available emulators
~/Library/Android/sdk/emulator/emulator -list-avds

# If none, create one
~/Library/Android/sdk/cmdline-tools/latest/bin/avdmanager create avd \
  -n "Pixel_6_API_33" -k "system-images;android-33;default;arm64-v8a"
```

---

### 📱 Use Physical Phone Instead (Easiest):

1. Enable **Developer Mode**: Settings → About → Tap Build Number 7x
2. Enable **USB Debugging**: Settings → Developer Options → USB Debugging
3. Plug into Mac via USB
4. Tap "Always allow this computer" on phone
5. In Android Studio: select phone from device dropdown
6. Click Run ▶️

---

### 🔗 Resources:

- **Project Repo:** https://github.com/shivam2003-dev/PosePerfect
- **Full Setup Guide:** `SETUP_GUIDE.md` in repo
- **README:** `README.md` with architecture details

---

### ✅ Verification:

After running, verify:
```bash
# Check app is installed
~/Library/Android/sdk/platform-tools/adb shell pm list packages | grep poseperfect

# Check logs for errors
~/Library/Android/sdk/platform-tools/adb logcat
```

---

**That's it!** 🚀 You're ready to run PosePerfect on your emulator or device!

