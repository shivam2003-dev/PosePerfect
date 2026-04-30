#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# download_model.sh  –  Downloads the MediaPipe Pose Landmarker Lite model
# into app/src/main/assets/ so Android can bundle it in the APK.
# ─────────────────────────────────────────────────────────────────────────────

set -e

ASSETS_DIR="app/src/main/assets"
MODEL_NAME="pose_landmarker_lite.task"
MODEL_URL="https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task"

echo "📁  Creating assets directory..."
mkdir -p "$ASSETS_DIR"

echo "⬇️   Downloading $MODEL_NAME ..."
curl -L "$MODEL_URL" -o "$ASSETS_DIR/$MODEL_NAME"

echo "✅  Model saved to $ASSETS_DIR/$MODEL_NAME"
echo "    Size: $(du -sh "$ASSETS_DIR/$MODEL_NAME" | cut -f1)"

