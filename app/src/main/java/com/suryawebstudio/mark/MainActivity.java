package com.suryawebstudio.mark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.suryawebstudio.mark.ai.AIProvider;
import com.suryawebstudio.mark.ai.GeminiLiveProvider;
import com.suryawebstudio.mark.ai.LiveTokenClient;
import com.suryawebstudio.mark.voice.AudioCapture;
import com.suryawebstudio.mark.voice.AudioPlayback;
import com.suryawebstudio.mark.voice.VoiceState;
import com.suryawebstudio.mark.voice.VoiceStateController;

public final class MainActivity extends ComponentActivity {
    private static final int RECORD_AUDIO_REQUEST = 100;
    private static final String TOKEN_ENDPOINT = "https://jarvis-ai-backend.suryawebstudio.workers.dev/live-token";
    private final VoiceStateController voiceStateController = new VoiceStateController();
    private final GeminiLiveProvider provider = new GeminiLiveProvider();
    private final AudioPlayback playback = new AudioPlayback();
    private TextView stateText;
    private TextView transcriptText;
    private Button voiceButton;
    private AudioCapture capture;
    private boolean connecting;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildHome();
        voiceStateController.addListener(state -> stateText.setText("State: " + state.name()));
    }

    private void buildHome() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 70, 40, 40);
        root.setBackgroundColor(Color.rgb(16, 17, 26));

        TextView title = new TextView(this);
        title.setText("MARK"); title.setTextColor(Color.WHITE); title.setTextSize(36); title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, 90));

        stateText = new TextView(this);
        stateText.setTextColor(Color.rgb(196, 181, 253)); stateText.setTextSize(20); stateText.setGravity(Gravity.CENTER);
        root.addView(stateText, new LinearLayout.LayoutParams(-1, 70));

        transcriptText = new TextView(this);
        transcriptText.setText("Ready for secure Live voice.\nYour Gemini API key stays in Cloudflare.");
        transcriptText.setTextColor(Color.LTGRAY); transcriptText.setTextSize(16); transcriptText.setGravity(Gravity.CENTER);
        root.addView(transcriptText, new LinearLayout.LayoutParams(-1, 180));

        voiceButton = new Button(this);
        voiceButton.setText("Start voice conversation"); voiceButton.setOnClickListener(v -> startVoice());
        root.addView(voiceButton, new LinearLayout.LayoutParams(-1, 68));

        Button stopButton = new Button(this);
        stopButton.setText("Stop"); stopButton.setOnClickListener(v -> stopVoice("Stopped by user."));
        root.addView(stopButton, new LinearLayout.LayoutParams(-1, 68));
        setContentView(root);
    }

    private void startVoice() {
        if (connecting || capture != null) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST);
            return;
        }
        connecting = true;
        voiceButton.setEnabled(false);
        voiceStateController.setState(VoiceState.THINKING);
        transcriptText.setText("Requesting a short-lived Live token…");
        new LiveTokenClient(TOKEN_ENDPOINT).request(new LiveTokenClient.CallbackResult() {
            @Override public void onSuccess(String token, String model) {
                runOnUiThread(() -> {
                    provider.connect(new AIProvider.ProviderConfig(TOKEN_ENDPOINT, token, model), MainActivity.this::onProviderEvent);
                    transcriptText.setText("Connecting to Gemini Live…");
                });
            }
            @Override public void onFailure(String message) { runOnUiThread(() -> failVoice(message)); }
        });
    }

    private void onProviderEvent(AIProvider.AIEvent event) {
        runOnUiThread(() -> {
            switch (event.type) {
                case CONNECTED:
                    connecting = false;
                    voiceButton.setEnabled(true);
                    voiceStateController.setState(VoiceState.LISTENING);
                    playback.start();
                    capture = new AudioCapture(new AudioCapture.Listener() {
                        @Override public void onAudio(byte[] pcm) { provider.sendAudio(pcm); }
                        @Override public void onError(String message) { runOnUiThread(() -> failVoice(message)); }
                    });
                    if (!capture.start()) failVoice("Microphone could not start.");
                    transcriptText.setText("Listening… speak naturally. Gemini can interrupt and respond.");
                    break;
                case TRANSCRIPT_IN: transcriptText.setText("You: " + event.text); break;
                case TRANSCRIPT_OUT: voiceStateController.setState(VoiceState.SPEAKING); transcriptText.setText("MARK: " + event.text); break;
                case AUDIO_OUT:
                    voiceStateController.setState(VoiceState.SPEAKING);
                    playback.play((byte[]) event.data);
                    break;
                case INTERRUPTED:
                    playback.stopAndFlush();
                    voiceStateController.setState(VoiceState.LISTENING);
                    break;
                case TURN_COMPLETE: voiceStateController.setState(VoiceState.LISTENING); break;
                case ERROR: failVoice(event.text); break;
                case CLOSED: stopVoice("Live session closed."); break;
                default: break;
            }
        });
    }

    private void failVoice(String message) { stopVoice(message); }

    private void stopVoice(String message) {
        if (capture != null) { capture.stop(); capture = null; }
        provider.disconnect();
        playback.stopAndFlush(); playback.release();
        connecting = false;
        if (voiceButton != null) voiceButton.setEnabled(true);
        voiceStateController.setState(VoiceState.IDLE);
        if (transcriptText != null) transcriptText.setText(message);
    }

    @Override protected void onDestroy() { stopVoice("MARK closed."); super.onDestroy(); }
}
