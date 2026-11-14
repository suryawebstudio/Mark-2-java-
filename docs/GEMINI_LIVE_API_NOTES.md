# Gemini Live API notes

Verified on 2026-11-14 before implementing MARK Live voice.

The official Live API overview documents stateful WebSocket connections, client-to-server connections with ephemeral tokens, raw 16-bit PCM input at 16 kHz little-endian, and raw 16-bit PCM output at 24 kHz little-endian. The WebSocket quickstart documents the `BidiGenerateContentConstrained` endpoint with an `access_token` query parameter for ephemeral tokens, the initial `setup` message, and audio messages using `audio/pcm;rate=16000`. The ephemeral token guide documents one-use tokens, a default 30-minute expiry, and a one-minute new-session window. The live guide documents server-side VAD interruption and the need to flush local playback when an interruption is received.

MARK uses the existing Cloudflare Worker `jarvis-ai-backend` and its `GEMINI_API_KEY` secret. The public token endpoint was tested without printing the token value and returned `model=models/gemini-3.1-flash-live-preview` and `expiresInSeconds=1800`.

Sources:

- https://ai.google.dev/gemini-api/docs/live-api
- https://ai.google.dev/gemini-api/docs/live-api/get-started-websocket
- https://ai.google.dev/gemini-api/docs/live-api/ephemeral-tokens
- https://ai.google.dev/gemini-api/docs/live-guide
- https://developers.cloudflare.com/workers/configuration/secrets/
