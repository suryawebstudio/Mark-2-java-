# MARK

MARK is a Java Android foundation for a voice-controlled personal assistant. The repository uses package root `com.suryawebstudio.mark`, minSdk 26, targetSdk 34, and compileSdk 34.

## Current scope: Stage 1 foundation

Implemented:

- A Java Android application entry point and manual `AppContainer`.
- A single `VoiceStateController` with `IDLE`, `LISTENING`, `THINKING`, and `SPEAKING` states.
- Allowlisted tool contracts, validation results, risk levels, and result types.
- `TaskExecutor` that rejects unknown tools, validates arguments, checks permissions, binds confirmations to SHA-256 payload hashes, and verifies results.
- Verified `battery_info` and `flashlight` tools using Android APIs.
- A foreground-service notification boundary for future microphone sessions.
- Honest home-screen status: microphone capture and provider connection are not enabled by this foundation.
- Unit coverage for confirmation-payload binding.

Not implemented yet (intentionally, not hidden): Gemini Live WebSocket/audio capture, Room persistence, transcripts, reconnect/backoff, full Compose UI, permission-management screen, and the later productivity/communication stages. The foreground service contains a `TODO(MARK)` at the point where AudioRecord and a secure provider session must be wired.

## Build and test

The repository includes the Gradle wrapper, so from a machine with Android Studio or the Android SDK installed:

```bash
./gradlew test
./gradlew assembleDebug
```

The verified debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected Android device with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

In Android Studio, open this repository, let Gradle sync, select the `app` configuration, and press **Run**. The launcher activity is `com.suryawebstudio.mark.MainActivity`.

Expected behavior on a device: the home screen reports `IDLE`, states that no microphone is recording, and allows the user to request `RECORD_AUDIO`. The registered tools remain in the in-app allowlist; no model can invoke an unknown name.

The debug APK build has been verified with Gradle 8.5, Android Gradle Plugin 8.2.2, Java 21, compileSdk 34, and build-tools 34.0.0. There is no emulator or physical Android device attached to this session, so device behavior remains **NOT TESTED ON DEVICE**.

## Android API / permission notes

The manifest declares `RECORD_AUDIO`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, and `FOREGROUND_SERVICE_MICROPHONE`. Microphone capture must not start until runtime permission is granted and the foreground notification is visible. Android 13+ requires notification permission for MARK's own notification UX. Flashlight state is verified through `CameraManager.TorchCallback`; it can still fail if another app owns the camera.

## Security boundaries

No API key is stored in this repository or APK configuration. A future provider must obtain a short-lived token from a Cloudflare Worker. Outbound communication, deletion, payment, and other irreversible actions must remain confirmation-gated. Website, notification, email, and file text must be treated as untrusted data.
