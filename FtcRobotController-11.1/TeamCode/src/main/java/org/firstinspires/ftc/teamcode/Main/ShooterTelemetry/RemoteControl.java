package org.firstinspires.ftc.teamcode.Main.ShooterTelemetry;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.RobotLog;

import org.json.JSONObject;

/**
 * Latest keyboard/remote command from the shooter-viewer WebSocket.
 * Drive sticks override the physical gamepad while commands are fresh; buttons
 * OR with the physical pad so either source can edge-trigger TeleOp actions.
 * Stale commands (&gt;{@link #STALE_MS}) clear sticks once so a dropped viewer
 * cannot leave the robot driving.
 */
public final class RemoteControl {
    private static final String TAG = "RemoteControl";
    // Viewer streams ~20 Hz; allow a few missed frames (Wi‑Fi hiccup) before
    // cutting drive. Too low felt like 5 cm pulses when a single cmd was late.
    private static final long STALE_MS = 750;
    // Existing TeleOp logic is rising-edge based. Convert each remote 0→1 into a
    // short pulse long enough for both drive and control threads to observe.
    private static final long BUTTON_PULSE_MS = 150;

    private static volatile long lastCmdMs = 0;
    private static volatile boolean ownedDrive = false;

    private static volatile float lx = 0;
    private static volatile float ly = 0;
    private static volatile float rx = 0;

    private static boolean rawLeftBumper;
    private static boolean rawRightBumper;
    private static boolean rawLeftTrigger;
    private static boolean rawRightTrigger;
    private static boolean rawSquare;
    private static boolean rawCircle;
    private static boolean rawDpadUp;
    private static boolean rawDpadRight;
    private static boolean rawRightStickButton;
    private static boolean rawShare;

    private static volatile long leftBumperUntil;
    private static volatile long rightBumperUntil;
    private static volatile long leftTriggerUntil;
    private static volatile long rightTriggerUntil;
    private static volatile long squareUntil;
    private static volatile long circleUntil;
    private static volatile long dpadUpUntil;
    private static volatile long dpadRightUntil;
    private static volatile long rightStickButtonUntil;
    private static volatile long shareUntil;

    private RemoteControl() {}

    /** Parse a viewer WS text frame; ignore anything that isn't {@code type:cmd}. */
    public static synchronized void onMessage(String text) {
        if (text == null || text.isEmpty()) return;
        try {
            JSONObject o = new JSONObject(text);
            if (!"cmd".equals(o.optString("type", ""))) return;

            JSONObject drive = o.optJSONObject("drive");
            if (drive != null) {
                lx = clamp((float) drive.optDouble("lx", 0));
                ly = clamp((float) drive.optDouble("ly", 0));
                rx = clamp((float) drive.optDouble("rx", 0));
            } else {
                lx = clamp((float) o.optDouble("lx", 0));
                ly = clamp((float) o.optDouble("ly", 0));
                rx = clamp((float) o.optDouble("rx", 0));
            }

            JSONObject btn = o.optJSONObject("buttons");
            if (btn != null) {
                long now = System.currentTimeMillis();
                boolean next;

                next = btn.optBoolean("leftBumper", false);
                if (next && !rawLeftBumper) leftBumperUntil = now + BUTTON_PULSE_MS;
                rawLeftBumper = next;
                next = btn.optBoolean("rightBumper", false);
                if (next && !rawRightBumper) rightBumperUntil = now + BUTTON_PULSE_MS;
                rawRightBumper = next;
                next = btn.optBoolean("leftTrigger", false);
                if (next && !rawLeftTrigger) leftTriggerUntil = now + BUTTON_PULSE_MS;
                rawLeftTrigger = next;
                next = btn.optBoolean("rightTrigger", false);
                if (next && !rawRightTrigger) rightTriggerUntil = now + BUTTON_PULSE_MS;
                rawRightTrigger = next;
                next = btn.optBoolean("square", false);
                if (next && !rawSquare) squareUntil = now + BUTTON_PULSE_MS;
                rawSquare = next;
                next = btn.optBoolean("circle", false);
                if (next && !rawCircle) circleUntil = now + BUTTON_PULSE_MS;
                rawCircle = next;
                next = btn.optBoolean("dpadUp", false);
                if (next && !rawDpadUp) dpadUpUntil = now + BUTTON_PULSE_MS;
                rawDpadUp = next;
                next = btn.optBoolean("dpadRight", false);
                if (next && !rawDpadRight) dpadRightUntil = now + BUTTON_PULSE_MS;
                rawDpadRight = next;
                next = btn.optBoolean("rightStickButton", false);
                if (next && !rawRightStickButton) rightStickButtonUntil = now + BUTTON_PULSE_MS;
                rawRightStickButton = next;
                next = btn.optBoolean("share", false);
                if (next && !rawShare) shareUntil = now + BUTTON_PULSE_MS;
                rawShare = next;
            }

            lastCmdMs = System.currentTimeMillis();
            ownedDrive = true;
        } catch (Throwable t) {
            RobotLog.ww(TAG, "bad cmd: %s", t.getMessage());
        }
    }

    /**
     * Merge remote state into a gamepad snapshot used for drive / edge detection.
     * Call after {@code copy(physicalGamepad)}.
     */
    public static void mergeInto(Gamepad g) {
        if (g == null) return;
        long age = System.currentTimeMillis() - lastCmdMs;
        if (age > STALE_MS) {
            if (ownedDrive) {
                g.left_stick_x = 0;
                g.left_stick_y = 0;
                g.right_stick_x = 0;
                ownedDrive = false;
            }
            return;
        }

        g.left_stick_x = lx;
        g.left_stick_y = ly;
        g.right_stick_x = rx;

        long now = System.currentTimeMillis();
        if (now <= leftBumperUntil) g.left_bumper = true;
        if (now <= rightBumperUntil) g.right_bumper = true;
        if (now <= leftTriggerUntil) g.left_trigger = Math.max(g.left_trigger, 1f);
        if (now <= rightTriggerUntil) g.right_trigger = Math.max(g.right_trigger, 1f);
        if (now <= squareUntil) g.square = true;
        if (now <= circleUntil) g.circle = true;
        if (now <= dpadUpUntil) g.dpad_up = true;
        if (now <= dpadRightUntil) g.dpad_right = true;
        if (now <= rightStickButtonUntil) g.right_stick_button = true;
        if (now <= shareUntil) g.share = true;
    }

    public static boolean isActive() {
        return ownedDrive && System.currentTimeMillis() - lastCmdMs <= STALE_MS;
    }

    private static float clamp(float v) {
        if (v > 1f) return 1f;
        if (v < -1f) return -1f;
        return v;
    }
}
