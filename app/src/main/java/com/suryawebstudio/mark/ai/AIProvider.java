package com.suryawebstudio.mark.ai;

import com.suryawebstudio.mark.tools.ToolTypes;

import java.util.function.Consumer;

public interface AIProvider {
    String getName();
    void connect(ProviderConfig config, Consumer<AIEvent> eventConsumer);
    void disconnect();
    void sendAudio(byte[] chunk);
    void sendText(String text);
    void interrupt();
    void sendToolResult(String callId, ToolTypes.ToolResult result);

    final class ProviderConfig {
        public final String sessionEndpoint;
        public final String ephemeralToken;
        public final String model;

        public ProviderConfig(String sessionEndpoint, String ephemeralToken, String model) {
            this.sessionEndpoint = sessionEndpoint;
            this.ephemeralToken = ephemeralToken;
            this.model = model;
        }
    }

    final class AIEvent {
        public enum Type { CONNECTED, CLOSED, AUDIO_OUT, TRANSCRIPT_IN, TRANSCRIPT_OUT, TOOL_CALL, TURN_COMPLETE, INTERRUPTED, ERROR }
        public final Type type;
        public final String text;
        public final Object data;

        public AIEvent(Type type, String text) { this(type, text, null); }
        public AIEvent(Type type, Object data) { this(type, null, data); }
        private AIEvent(Type type, String text, Object data) { this.type = type; this.text = text; this.data = data; }
    }
}
