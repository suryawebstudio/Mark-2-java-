# MARK Live Worker

The existing Cloudflare Worker `jarvis-ai-backend` is reused for MARK. Its public endpoint is:

```text
https://jarvis-ai-backend.suryawebstudio.workers.dev
```

The Android app requests a short-lived token from:

```text
POST /live-token
```

The Worker reads `GEMINI_API_KEY` from a Cloudflare Worker secret. The Gemini key is never included in the Android project.

The deployed Worker already exposes this route and its existing `/chat` route was preserved. `worker.js` is the repository source for the Live token route; deploying it as-is would need the existing `/chat` implementation merged back in first, so this milestone does not overwrite the working Worker.

Current Gemini Live values verified from the official documentation:

- Model: `models/gemini-3.1-flash-live-preview`
- WebSocket endpoint: `BidiGenerateContentConstrained` with `access_token`
- Input: raw little-endian 16-bit PCM at 16 kHz, `audio/pcm;rate=16000`
- Output: raw little-endian 16-bit PCM at 24 kHz
- Ephemeral token: one use, 30-minute expiry, one-minute new-session window

References:

- https://ai.google.dev/gemini-api/docs/live-api
- https://ai.google.dev/gemini-api/docs/live-api/get-started-websocket
- https://ai.google.dev/gemini-api/docs/live-api/ephemeral-tokens
- https://ai.google.dev/gemini-api/docs/live-guide
