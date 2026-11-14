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

        public ProviderConfig(String sessionEndpoint, String ephemeralToken) {
            this.sessionEndpoint = sessionEndpoint;
            this.ephemeralToken = ephemeralToken;
        }
    }

    final class AIEvent {
        public enum Type { AUDIO_OUT, TRANSCRIPT_IN, TRANSCRIPT_OUT, TOOL_CALL, TURN_COMPLETE, ERROR }
        public final Type type;
        public final String text;

        public AIEvent(Type type, String text) {
            this.type = type;
            this.text = text;
        }
    }
}
