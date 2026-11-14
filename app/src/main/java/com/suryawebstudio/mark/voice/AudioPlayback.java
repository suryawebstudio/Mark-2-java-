package com.suryawebstudio.mark.voice;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public final class AudioPlayback {
    private static final int SAMPLE_RATE = 24_000;
    private AudioTrack track;

    public synchronized void start() {
        if (track != null) return;
        int min = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = Math.max(min, SAMPLE_RATE / 5);
        track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANT).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
        track.play();
    }

    public synchronized void play(byte[] pcm) {
        if (track == null) start();
        if (track != null && pcm.length > 0) track.write(pcm, 0, pcm.length, AudioTrack.WRITE_BLOCKING);
    }

    public synchronized void stopAndFlush() {
        if (track == null) return;
        try { track.pause(); track.flush(); } catch (IllegalStateException ignored) { }
    }

    public synchronized void release() {
        if (track != null) {
            try { track.stop(); } catch (IllegalStateException ignored) { }
            track.release(); track = null;
        }
    }
}
