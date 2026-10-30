# DevQuest — Learn. Build. Conquer.

An Android-only, offline-first learning app written in Kotlin and Jetpack Compose. No sign-in, network permission, remote images, compiler service or AI backend. Includes 25 substantive lessons across ten paths, 42 questions, three coding challenges, four Knowledge Forge scenes, bookmarks, interview practice, progress analytics and persistent preferences.

## Run

Open this directory in Android Studio, select JDK 17, let Gradle sync, then run `app` on an Android 8.0+ device (API 26+). Install SDK Platform 36 and Build Tools 35.0.0 if prompted. `local.properties` is machine-specific and ignored; Android Studio generates it. The checked build uses Java 17, Gradle 8.14.3, AGP 8.13.1, Kotlin/Compose compiler 2.0.21, Compose BOM 2024.09.00, Room 2.7.0, Hilt 2.52 and KSP 2.0.21-1.0.27. Versions are pinned in the catalog. SDK 36 is the compile and target SDK.

```sh
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

If Gradle sync reports `Cannot find a Java installation ... languageVersion=17`, open Android Studio > Settings > Build, Execution, Deployment > Build Tools > Gradle and select an installed JDK 17 as the **Gradle JDK**. Use **Add JDK** if it is not listed. On an Apple Silicon Mac with Homebrew OpenJDK 17, its home is typically `/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`. Then sync the project again.

This project's `GRADLE_LOCAL_JAVA_HOME` selection reads `java.home` from the ignored `.gradle/config.properties` file. That file must point to JDK 17 on your machine. Android Studio's bundled runtime may be JDK 21; it does not satisfy the explicit `jvmToolchain(17)` requirement in `domain`, `core:model` and `core:common`. For terminal builds, set `JAVA_HOME` to the same JDK 17 directory. JDK download repositories are not configured, so Gradle needs a discoverable local JDK 17.

First launch seeds local content, opens Learn / Practice / Master onboarding and goal selection, then enters Home. Subsequent launches restore Room progress and DataStore preferences. Bottom tabs are Home, Learn, Practice and Profile. Knowledge Forge and detailed Progress are available from Home. All lessons are available immediately.

## Verify

```sh
python3 scripts/validate_content.py
./gradlew spotlessCheck
./gradlew :domain:test :core:common:test :core:motion:testDebugUnitTest
./gradlew :data:testDebugUnitTest :app:testDebugUnitTest
./gradlew :app:lintDebug
# Requires a connected Android device or functioning emulator:
./gradlew :data:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

Robolectric tests use API 34 and download an Android test runtime on first execution. They are host-side Android simulations, not device tests. Their cache is isolated beneath each module's build directory. Room in-memory integration tests cover seed idempotency, concurrent duplicate completion, first-attempt quiz credit, repository recreation, bookmarks, reading positions, coding drafts, interviews, activity deduplication and reset. Compose tests exercise onboarding, Home-to-lesson navigation and quiz feedback/results. Pure tests cover grading, retry selection, progress, midnight/DST date buckets and projection/animation bounds.

See `VERIFICATION.md` for actual results and environment limitations.

## Data and reward rules

Content and user records live in separate normalized Room tables with foreign keys and unique keys. Room exports schema v1 in `core/database/schemas`. There is no destructive migration fallback. For a future schema change, add and test an explicit `Migration`, register it in `StorageModule.database`, increment the database version and commit the exported schema. Version 1 has no predecessor to migrate from.

Lesson completion and +20 XP commit together. Each question's **first scored answer** creates a permanent reward key: +5 if correct, 0 if incorrect. A later retry cannot earn credit for that question. Interviews earn +15 once per session ID; coding self-review earns +15 once per challenge. Reward events are append-only until the user explicitly confirms reset. Unique keys and transactions enforce these rules even with concurrent taps. Achievements are derived from stored XP thresholds.

Study time is measured in five-second foreground intervals on learning/practice/Forge screens. Partial intervals under five seconds are not credited. Calendar bucketing splits intervals at local midnight, using zone-aware day boundaries rather than fixed 24-hour arithmetic. Completing an activity also marks the date active even if no full time interval elapsed. Historical activity keeps its original local date when the device timezone changes. The repository accepts an injectable Clock. Streaks include consecutive dates ending today or yesterday.

## Content authoring

Edit `data/src/main/assets/content.json`. Keep stable IDs, valid foreign keys and question option indexes. Each lesson has typed `text`, `code` and `exercise` blocks. Add lessons to a chapter and chapters to a path. Increase `version` to apply content upserts on an existing install; user progress remains untouched. Do not reuse an old ID for a different concept. Removed content remains available in the database so progress references are not orphaned; a future retirement policy should be an explicit migration. Run the validator and repository tests after changes.

Coding tasks explain constraints, example IO, hints and a reference solution. They save editable Kotlin drafts and offer **self-review only**; nothing executes or automatically grades arbitrary code. Interviews have a local stopwatch, revealed model answers and confidence ratings. No recording UI or audio permission is included.

## Architecture and interaction

See `ARCHITECTURE.md` and `IMPLEMENTATION_PLAN.md`. All requested modules are present. The activity-scoped Hilt ViewModel owns immutable `UiState`, accepts sealed `Action` values, calls domain contracts/use cases and uses SavedStateHandle for pending quiz navigation. Features are cohesive rendering modules rather than one ViewModel per small screen. UI never accesses Room directly. Navigation uses serialized route types; Room holds completed answers while saveable state holds current selections and drafts.

Dark/light/system themes use an ink, blue, violet and amber palette. Layouts scroll in landscape, constrain readable width on tablets and use two-column path cards above 640dp. Buttons use Material touch targets. Scenes provide textual node descriptions and explicit controls. Reduced motion honors both the preference and Android animator enablement. Playback is lifecycle-gated; no animation loop runs while the screen is backgrounded. System bars use edge-to-edge handling.

The four original scenes explain architecture layers, MVVM flow, coroutine concurrency and Compose state. A small MIT projection helper is adapted from Compose Lab; this is source adaptation, not a library integration. See `THIRD_PARTY_NOTICES` for exact attribution and portability findings.

## Scope and limits

This release is a compact starter curriculum, not an exhaustive Android course. Content, interview prompts and self-review solutions are bundled locally. There is no cloud sync, login, voice recording, arbitrary Kotlin execution or AI interviewer. Adaptive layouts prioritize readable scrolling and accessible controls; physical-device visual/performance/accessibility review remains part of release acceptance. Quiz answers persist per submission; unsaved coding drafts and UI selections depend on Android saved-state restoration limits. Read position is saved when leaving a lesson and held in Compose saved state across recreation.
