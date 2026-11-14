package com.suryawebstudio.mark.voice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.Arrays;

public final class AudioCapture {
    public interface Listener { void onAudio(byte[] pcm); void onError(String message); }

    private static final int SAMPLE_RATE = 16_000;
    private final Listener listener;
    private AudioRecord record;
    private Thread worker;
    private volatile boolean running;

    public AudioCapture(Listener listener) { this.listener = listener; }

    public synchronized boolean start() {
        if (running) return true;
        int min = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min <= 0) { listener.onError("Android could not determine a microphone buffer size."); return false; }
        int bufferSize = Math.max(min, SAMPLE_RATE / 10);
        try {
            record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                listener.onError("Android could not initialize the microphone.");
                record.release(); record = null; return false;
            }
            if (android.media.audiofx.AcousticEchoCanceler.isAvailable()) {
                android.media.audiofx.AcousticEchoCanceler.create(record.getAudioSessionId());
            }
            if (android.media.audiofx.NoiseSuppressor.isAvailable()) {
                android.media.audiofx.NoiseSuppressor.create(record.getAudioSessionId());
            }
            running = true;
            record.startRecording();
            worker = new Thread(() -> captureLoop(bufferSize), "MARK-AudioCapture");
            worker.start();
            return true;
        } catch (SecurityException error) {
            listener.onError("Microphone permission is not granted.");
        } catch (Exception error) {
            listener.onError("Microphone could not start: " + error.getClass().getSimpleName());
        }
        if (record != null) { record.release(); record = null; }
        return false;
    }

    private void captureLoop(int bufferSize) {
        byte[] buffer = new byte[Math.max(640, bufferSize / 4)];
        while (running && record != null) {
            int count = record.read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);
            if (count > 0) listener.onAudio(Arrays.copyOf(buffer, count));
            else if (count < 0) listener.onError("Android stopped reading microphone audio: " + count);
        }
    }

    public synchronized void stop() {
        running = false;
        if (record != null) {
            try { record.stop(); } catch (IllegalStateException ignored) { }
            record.release(); record = null;
        }
        worker = null;
    }
}
