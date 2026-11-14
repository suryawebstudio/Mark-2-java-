# Gap list versus MARK_INSTRUCTIONS.md

1. The source instructions requested Kotlin/Compose, while the user explicitly requested Java. This repository uses Java and XML Views; no Kotlin is introduced.
2. No existing project was present, so there were no existing modules, manifests, permissions, services, or working features to preserve.
3. The existing `jarvis-ai-backend` Worker is reused. It has a `GEMINI_API_KEY` secret and a working `/live-token` endpoint. The Android app now connects to it using the current Gemini Live ephemeral-token WebSocket flow.
4. AudioRecord/AudioTrack, VAD interruption, and live transcripts are implemented for foreground use. Session resumption, Room persistence, quota counters, full permission-management UI, and background microphone operation remain future features.
5. Stages 2–6 remain intentionally unstarted until Stage 1 is tested and accepted.
