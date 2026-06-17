#!/usr/bin/env bash
# build.sh - Build LighterBrowser APK from CLI
# Requires: JDK 17, Android SDK (ANDROID_HOME set)
set -euo pipefail

cd "$(dirname "$0")"

if [ -z "${ANDROID_HOME:-}" ]; then
  echo "ERROR: ANDROID_HOME not set. Example:"
  echo "  export ANDROID_HOME=\$HOME/Android/Sdk"
  exit 1
fi

if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
  echo ">>> gradle-wrapper.jar missing. Downloading..."
  mkdir -p gradle/wrapper
  curl -L -o gradle/wrapper/gradle-wrapper.jar \
    https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar
fi

chmod +x gradlew

echo ">>> Building debug APK..."
./gradlew assembleDebug --no-daemon

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo ""
  echo "BUILD SUCCESS"
  echo "APK: $APK"
  echo "Install on connected device: adb install -r $APK"
else
  echo "BUILD FAILED - check output above"
  exit 1
fi
