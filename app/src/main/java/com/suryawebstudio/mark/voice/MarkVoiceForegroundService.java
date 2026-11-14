package com.suryawebstudio.mark.voice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.suryawebstudio.mark.R;

public final class MarkVoiceForegroundService extends Service {
    private static final String CHANNEL_ID = "mark_voice";

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("MARK is listening")
                .setContentText("Microphone use is active. Stop MARK to release it.")
                .setOngoing(true)
                .build();
        startForeground(1001, notification);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO(MARK): Start AudioRecord only after RECORD_AUDIO is granted and a provider session is connected.
        return START_NOT_STICKY;
    }

    @Override public void onDestroy() { super.onDestroy(); }
    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MARK voice", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
