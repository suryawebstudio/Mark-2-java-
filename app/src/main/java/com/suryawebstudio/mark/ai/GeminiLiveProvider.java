package com.suryawebstudio.mark.ai;

import android.util.Base64;

import com.suryawebstudio.mark.tools.ToolTypes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public final class GeminiLiveProvider implements AIProvider {
    private final OkHttpClient client = new OkHttpClient();
    private final Object lock = new Object();
    private WebSocket socket;
    private Consumer<AIEvent> events;

    @Override public String getName() { return "Gemini Live API"; }

    @Override public void connect(ProviderConfig config, Consumer<AIEvent> eventConsumer) {
        synchronized (lock) {
            events = eventConsumer;
            if (socket != null) socket.cancel();
            String url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContentConstrained?access_token=" + config.ephemeralToken;
            Request request = new Request.Builder().url(url).build();
            socket = client.newWebSocket(request, new WebSocketListener() {
                @Override public void onOpen(WebSocket webSocket, Response response) {
                    try {
                        JSONObject setup = new JSONObject();
                        JSONObject setupConfig = new JSONObject();
                        setupConfig.put("model", config.model);
                        setupConfig.put("responseModalities", new JSONArray().put("AUDIO"));
                        setupConfig.put("inputAudioTranscription", new JSONObject());
                        setupConfig.put("outputAudioTranscription", new JSONObject());
                        setupConfig.put("systemInstruction", new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", PromptTemplates.SYSTEM))));
                        setup.put("setup", setupConfig);
                        webSocket.send(setup.toString());
                        emit(new AIEvent(AIEvent.Type.CONNECTED, "Live session connected."));
                    } catch (Exception error) { emitError("Live setup failed: " + error.getMessage()); }
                }

                @Override public void onMessage(WebSocket webSocket, String text) { parseMessage(text); }
                @Override public void onFailure(WebSocket webSocket, Throwable error, Response response) { emitError("Live connection failed: " + error.getMessage()); }
                @Override public void onClosed(WebSocket webSocket, int code, String reason) { emit(new AIEvent(AIEvent.Type.CLOSED, reason)); }
            });
        }
    }

    private void parseMessage(String raw) {
        try {
            JSONObject message = new JSONObject(raw);
            JSONObject serverContent = message.optJSONObject("serverContent");
            if (serverContent == null) return;
            if (serverContent.optBoolean("interrupted", false)) emit(new AIEvent(AIEvent.Type.INTERRUPTED, "Playback interrupted."));
            if (serverContent.has("inputTranscription")) emit(new AIEvent(AIEvent.Type.TRANSCRIPT_IN, serverContent.optJSONObject("inputTranscription").optString("text")));
            if (serverContent.has("outputTranscription")) emit(new AIEvent(AIEvent.Type.TRANSCRIPT_OUT, serverContent.optJSONObject("outputTranscription").optString("text")));
            JSONObject modelTurn = serverContent.optJSONObject("modelTurn");
            if (modelTurn != null) {
                JSONArray parts = modelTurn.optJSONArray("parts");
                if (parts != null) for (int i = 0; i < parts.length(); i++) {
                    JSONObject inlineData = parts.optJSONObject(i).optJSONObject("inlineData");
                    if (inlineData != null && inlineData.has("data")) {
                        byte[] audio = Base64.decode(inlineData.optString("data"), Base64.DEFAULT);
                        emit(new AIEvent(AIEvent.Type.AUDIO_OUT, audio));
                    }
                }
            }
            if (serverContent.optBoolean("turnComplete", false)) emit(new AIEvent(AIEvent.Type.TURN_COMPLETE, "Turn complete."));
        } catch (Exception error) { emitError("Could not parse Live response."); }
    }

    @Override public void disconnect() { synchronized (lock) { if (socket != null) socket.close(1000, "MARK stopped"); socket = null; } }
    @Override public void sendAudio(byte[] chunk) {
        JSONObject audio = new JSONObject();
        try {
            audio.put("realtimeInput", new JSONObject().put("audio", new JSONObject().put("data", Base64.encodeToString(chunk, Base64.NO_WRAP)).put("mimeType", "audio/pcm;rate=16000")));
            synchronized (lock) { if (socket != null) socket.send(audio.toString()); }
        } catch (Exception error) { emitError("Could not send microphone audio."); }
    }
    @Override public void sendText(String text) {
        try { synchronized (lock) { if (socket != null) socket.send(new JSONObject().put("realtimeInput", new JSONObject().put("text", text)).toString()); } }
        catch (Exception error) { emitError("Could not send text to Live API."); }
    }
    @Override public void interrupt() { synchronized (lock) { if (socket != null) socket.send("{\"realtimeInput\":{\"audioStreamEnd\":true}}"); } }
    @Override public void sendToolResult(String callId, ToolTypes.ToolResult result) { emitError("Tool calls are not enabled in this first Live voice milestone."); }

    private void emit(AIEvent event) { Consumer<AIEvent> callback = events; if (callback != null) callback.accept(event); }
    private void emitError(String message) { emit(new AIEvent(AIEvent.Type.ERROR, message)); }
}
