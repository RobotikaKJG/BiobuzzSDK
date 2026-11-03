package org.firstinspires.ftc.teamcode.Main.ShooterTelemetry;

/** Heuristic load-event detector, not proof that a ball exited the shooter. */
public final class ShotTimingDetector {
    public static final double DROP_RPM = 150;
    public static final long DROP_WINDOW_MS = 200;
    public static final long TARGET_SETTLE_MS = 300;
    public static final long MIN_INTERVAL_MS = 250;
    private double target = Double.NaN;
    private double peak;
    private long targetSince;
    private long peakAt;
    private long lastEvent = -1;
    private boolean armed;

    public boolean update(long ms, double measuredRPM, double targetRPM) {
        if (!Double.isFinite(targetRPM) || targetRPM <= 0 || !Double.isFinite(measuredRPM)) {
            target = Double.NaN;
            armed = false;
            return false;
        }
        if (targetRPM != target) {
            target = targetRPM;
            targetSince = ms;
            armed = false;
        }
        if (ms - targetSince < TARGET_SETTLE_MS) return false;
        if (!armed) {
            if (measuredRPM >= targetRPM * 0.9
                    && (lastEvent < 0 || ms - lastEvent >= MIN_INTERVAL_MS)) {
                armed = true;
                peak = measuredRPM;
                peakAt = ms;
            }
            return false;
        }
        if (measuredRPM >= peak || ms - peakAt > DROP_WINDOW_MS) {
            peak = measuredRPM;
            peakAt = ms;
        }
        if (peak - measuredRPM >= DROP_RPM) {
            armed = false;
            lastEvent = ms;
            return true;
        }
        return false;
    }
}
