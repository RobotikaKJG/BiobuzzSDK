package org.firstinspires.ftc.teamcode.Main;

/**
 * Tiny rolling-average loop timer: records per-iteration durations and exposes the
 * average of the last {@code window} iterations in milliseconds.
 *
 * {@code avgMs} is {@code volatile} so a worker thread can record while another
 * thread (e.g. the main loop building telemetry) reads — the same lock-free
 * pattern Vilnius Lyceum's GlobalLoopTimeMonitor uses to share per-thread loop times.
 */
public class LoopTimer {
    private final long[] samplesNs;
    private long sumNs;
    private int index = 0;
    private int filled = 0;
    private volatile double avgMs = 0.0;

    public LoopTimer(int window) {
        samplesNs = new long[Math.max(1, window)];
    }

    /** Record one loop iteration's duration (nanoseconds) and update the rolling average. */
    public void record(long durationNs) {
        sumNs += durationNs - samplesNs[index];
        samplesNs[index] = durationNs;
        index = (index + 1) % samplesNs.length;
        if (filled < samplesNs.length) filled++;

        avgMs = (sumNs / (double) filled) / 1_000_000.0;
    }

    /** Average loop time over the last {@code window} iterations, in milliseconds. */
    public double getAvgMs() {
        return avgMs;
    }
}
