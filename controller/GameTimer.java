package controller;

import javax.swing.Timer;

public class GameTimer {
    private final Timer ticker;
    private long accumulatedNanos;
    private long startedAt;
    private boolean running;

    public GameTimer(Runnable onTick) {
        ticker = new Timer(250, e -> onTick.run());
    }
    public void start() {
        if (running) return;
        startedAt = System.nanoTime();
        running = true;
        ticker.start();
    }
    public void pause() {
        if (running) accumulatedNanos += System.nanoTime() - startedAt;
        running = false;
        ticker.stop();
    }
    public void reset(long milliseconds) {
        pause();
        accumulatedNanos = milliseconds * 1_000_000L;
    }
    public long elapsedMillis() {
        return (accumulatedNanos + (running ? System.nanoTime() - startedAt : 0)) / 1_000_000L;
    }
    public int elapsedSeconds() { return (int) Math.min(Integer.MAX_VALUE, elapsedMillis() / 1000); }
}
