package com.suryawebstudio.mark.voice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VoiceStateController {
    public interface Listener { void onStateChanged(VoiceState state); }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private VoiceState state = VoiceState.IDLE;

    public synchronized VoiceState getState() { return state; }

    public synchronized void setState(VoiceState next) {
        if (next == state) return;
        state = next;
        for (Listener listener : listeners) listener.onStateChanged(next);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
        listener.onStateChanged(getState());
    }

    public void removeListener(Listener listener) { listeners.remove(listener); }
}
