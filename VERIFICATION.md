# Verification — 29 October 2026

## Passed

- Android SDK 36 debug application build: `:app:assembleDebug`.
- App and data instrumentation APK builds: `:app:assembleDebugAndroidTest :data:assembleDebugAndroidTest`.
- 23 host-side tests, zero failures or skips:
  - Domain rules: 4.
  - Calendar, midnight and DST: 4.
  - Motion bounds and homography: 3.
  - Room/DataStore integration under Robolectric API 34: 8.
  - Compose UI flows under Robolectric API 34: 3.
  - Production Hilt application startup, seeding, onboarding, lesson completion and activity recreation under Robolectric API 34: 1.
- `spotlessApply spotlessCheck`: passed; Kotlin sources formatted using pinned ktfmt 0.54.
- `:app:lintDebug`: **0 errors, 28 warnings**. All warnings concern available dependency/plugin updates. Pinned compatible versions are intentionally retained; no baseline or error suppression was added.
- `python3 scripts/validate_content.py`: 10 paths, 25 lessons, 42 questions, 3 coding challenges; IDs, links and answer indexes valid.
- `apksigner verify`: debug APK signature valid.
- APK metadata confirms `com.devquest.android`, minSdk 26, target/compile SDK 36.
- APK permission audit: no network, microphone, storage or location permissions. AndroidX contributes only the application's signature-protected `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.

Final toolchain: OpenJDK 17.0.19, Gradle 8.14.3, AGP 8.13.1, Kotlin 2.0.21, KSP 2.0.21-1.0.27, Hilt 2.52, Room 2.7.0, Compose BOM 2024.09.00. The standard Gradle wrapper was regenerated using Gradle's `wrapper` task.

The final combined verification invocation was:

```sh
./gradlew spotlessApply :app:assembleDebug :domain:test :core:common:test \
  :core:motion:testDebugUnitTest :data:testDebugUnitTest :app:testDebugUnitTest \
  :app:assembleDebugAndroidTest :data:assembleDebugAndroidTest :app:lintDebug
```

It completed successfully. A subsequent formatting/package check and affected UI tests also passed after final formatting. XML test results and build/lint evidence are preserved in `verification/`.

## Device testing blocked by the installed emulator

The installed Android Emulator 37.2.12 could not start the existing `Medium_Phone_API_36.1` AVD. Commands attempted:

```sh
emulator -avd Medium_Phone_API_36.1 -no-snapshot -no-audio -no-boot-anim
emulator -avd Medium_Phone_API_36.1 -no-window -no-snapshot -no-audio -no-boot-anim -gpu swiftshader
emulator -avd Medium_Phone_API_36.1 -no-window -no-snapshot -no-audio -gpu off -verbose
adb devices
./gradlew --continue :app:connectedDebugAndroidTest :data:connectedDebugAndroidTest
```

The windowed emulator exited 134 with `Incompatible processor. This Qt build requires the following features: neon`. Headless alternatives exited 132 during startup. `adb devices` listed no device. Both connected-test tasks failed with `DeviceException: No connected devices!`; no device tests ran. This is an environment blocker, not a passing device result.

The application was launched and exercised **in Robolectric**, not on an emulator or physical device. Physical-device installation, visual review, screen-reader review, large-font/landscape screenshots, Android 16 behavior, and low-end frame performance remain unverified. Run the connected suites on a working API 26+ device before treating this as release-certified. The included APK is a debug build, not a Play release-signed artifact.
