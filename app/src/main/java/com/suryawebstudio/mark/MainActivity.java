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

import com.suryawebstudio.mark.voice.VoiceState;
import com.suryawebstudio.mark.voice.VoiceStateController;

public final class MainActivity extends ComponentActivity {
    private static final int RECORD_AUDIO_REQUEST = 100;
    private final VoiceStateController voiceStateController = new VoiceStateController();
    private TextView stateText;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildHome();
        voiceStateController.addListener(state -> stateText.setText("State: " + state.name()));
    }

    private void buildHome() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 80, 40, 40);
        root.setBackgroundColor(Color.rgb(16, 17, 26));

        TextView title = new TextView(this);
        title.setText("MARK"); title.setTextColor(Color.WHITE); title.setTextSize(36); title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, 100));

        stateText = new TextView(this);
        stateText.setTextColor(Color.rgb(196, 181, 253)); stateText.setTextSize(20); stateText.setGravity(Gravity.CENTER);
        root.addView(stateText, new LinearLayout.LayoutParams(-1, 80));

        TextView status = new TextView(this);
        status.setText("Stage 1 foundation\nProvider not connected. No microphone is recording.\nConnect a secure Worker session before enabling voice.");
        status.setTextColor(Color.LTGRAY); status.setTextSize(16); status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1, 180));

        Button permission = new Button(this);
        permission.setText("Grant microphone permission");
        permission.setOnClickListener(v -> requestMicrophonePermission());
        root.addView(permission, new LinearLayout.LayoutParams(-1, 64));

        Button tools = new Button(this);
        tools.setText("Registered safe tools: battery_info, flashlight");
        tools.setOnClickListener(v -> status.setText("Tools are allowlisted and validated in-app.\nNo action runs without validation and verification."));
        root.addView(tools, new LinearLayout.LayoutParams(-1, 64));

        setContentView(root);
    }

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            stateText.setText("State: IDLE (permission granted; provider not connected)");
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST);
    }
}
