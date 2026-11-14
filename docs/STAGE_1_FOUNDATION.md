# Stage 1 Foundation Workflow

## How it works

MARK starts in `IDLE` and exposes one state controller to the UI. A future provider can emit transcripts, audio, and tool calls, but no provider is connected by this foundation. Any tool call must first resolve through `ToolRegistry`, pass app-side validation, pass permission checks, and then execute. Risky actions additionally require a confirmation hash matching the exact payload. Tool results are verified before they can be described as successful.

## Android APIs used

- `android.app.Service.startForeground`
- `android.hardware.camera2.CameraManager.setTorchMode`
- `android.hardware.camera2.CameraManager.TorchCallback`
- `android.os.BatteryManager`
- `ActivityCompat.requestPermissions`
- `Context.checkSelfPermission`

## Permissions

- `RECORD_AUDIO`: declared and requested only from the home screen; no recording starts yet.
- `POST_NOTIFICATIONS`: declared for the future foreground notification; runtime request UI is still a next-step item.
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MICROPHONE`: declared for the future mic service.
- `ACCESS_NETWORK_STATE`: declared for future provider/network status.
- Flashlight and battery tools require no runtime permission.

## Android 13 restrictions / gotchas

Microphone use in the background requires a visible microphone foreground-service notification. Android can reject or interrupt torch changes when the camera is in use. Notification permission must be handled before relying on MARK's notification UX. This foundation does not claim those flows are complete.

## Build and test steps

1. Open the repository in Android Studio.
2. Sync Gradle and run the unit test task.
3. Install the debug APK on an Android 13+ device.
4. Confirm the home screen says no microphone is recording.
5. Tap **Grant microphone permission** and inspect the Android runtime dialog.
6. Inspect the app process and confirm no foreground microphone service starts from this screen.
7. Unit-test that two different confirmation payloads have different hashes.

## Failure cases

The app must say that the provider is not connected rather than pretending to listen. A missing camera flashlight returns `Unsupported`; an Android torch failure returns `Failure`; a missing permission returns `NeedsPermission`; an unknown tool is rejected.

## Verification

Battery status is read from the sticky `ACTION_BATTERY_CHANGED` intent. Flashlight state is confirmed through `TorchCallback`; if the callback does not confirm, the result remains unverified. Confirmation hashes bind approval to `toolName + exact JSON payload`.

**Device verification: NOT TESTED ON DEVICE.**
