package org.firstinspires.ftc.teamcode.Main;

import java.util.Locale;

/**
 * Tracks the time between successive "shoot" events to measure scoring cycle times.
 * Gaps >= {@link #OUTLIER_MS} (e.g. idle stretches between cycling) are treated as
 * outliers: they reset the baseline but are NOT recorded. Keeps the last
 * {@code window} cycle times plus a running average of them.
 *
 * Single-threaded: fed and read from the main OpMode loop only.
 */
public class CycleTimer {
    private static final long OUTLIER_MS = 20_000; // gaps >= 20s aren't real cycles

    private final long[] cyclesMs;
    private int idx = 0;     // next write slot (ring buffer)
    private int count = 0;
    private long lastEventMs = -1;

    public CycleTimer(int window) {
        cyclesMs = new long[Math.max(1, window)];
    }

    /**
     * Record a shoot event at {@code nowMs}. Returns the measured cycle time in ms
     * if it counted as a valid (non-outlier) cycle, else -1 (first event / outlier).
     */
    public long recordEvent(long nowMs) {
        long cycle = -1;
        if (lastEventMs >= 0) {
            long delta = nowMs - lastEventMs;
            if (delta < OUTLIER_MS) {
                cyclesMs[idx] = delta;
                idx = (idx + 1) % cyclesMs.length;
                if (count < cyclesMs.length) count++;
                cycle = delta;
            }
            // else: outlier — discard, but still reset the baseline below
        }
        lastEventMs = nowMs;
        return cycle;
    }

    public int getCount() { return count; }

    /** Average of the stored cycle times, in seconds. */
    public double getAvgSec() {
        if (count == 0) return 0.0;
        long sum = 0;
        for (int i = 0; i < count; i++) sum += cyclesMs[i];
        return sum / (double) count / 1000.0;
    }

    /** Stored cycle times (seconds), most-recent first. */
    public double[] getRecentSec() {
        double[] out = new double[count];
        for (int i = 0; i < count; i++) {
            int pos = (idx - 1 - i + cyclesMs.length) % cyclesMs.length;
            out[i] = cyclesMs[pos] / 1000.0;
        }
        return out;
    }

    /** A compact ASCII table of the last cycle times + average, for telemetry / logcat. */
    public String toTable() {
        double[] recent = getRecentSec();
        StringBuilder sb = new StringBuilder();
        sb.append("\n+------+-----------+\n");
        sb.append(String.format(Locale.US, "| %-4s | %-9s |%n", "#", "cycle (s)"));
        sb.append("+------+-----------+\n");
        if (recent.length == 0) {
            sb.append("|  --  |    --     |\n");
        } else {
            for (int i = 0; i < recent.length; i++) {
                sb.append(String.format(Locale.US, "| %-4d | %-9.2f |%n", i + 1, recent[i]));
            }
        }
        sb.append("+------+-----------+\n");
        sb.append(String.format(Locale.US, "avg(last %d): %.2f s", count, getAvgSec()));
        return sb.toString();
    }
}
