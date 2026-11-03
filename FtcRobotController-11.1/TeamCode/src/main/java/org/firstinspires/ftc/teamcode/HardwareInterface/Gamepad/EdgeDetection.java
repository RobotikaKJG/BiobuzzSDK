package org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.EnumMap;

public class EdgeDetection {

    private final GamepadIndex currentGamepad1Index = new GamepadIndex();
    private final GamepadIndex prevGamepad1Index = new GamepadIndex();
    private final GamepadIndex currentGamepad2Index = new GamepadIndex();
    private final GamepadIndex prevGamepad2Index = new GamepadIndex();

    private final EnumMap<GamepadIndexValues, Boolean> risingEdgesG1 = new EnumMap<>(GamepadIndexValues.class);
    private final EnumMap<GamepadIndexValues, Boolean> fallingEdgesG1 = new EnumMap<>(GamepadIndexValues.class);
    private final EnumMap<GamepadIndexValues, Boolean> risingEdgesG2 = new EnumMap<>(GamepadIndexValues.class);
    private final EnumMap<GamepadIndexValues, Boolean> fallingEdgesG2 = new EnumMap<>(GamepadIndexValues.class);

    public void refreshGamepadIndex(Gamepad currentG1, Gamepad prevG1) {
        refreshGamepad1Index(currentG1, prevG1);
    }

    public void refreshGamepad1Index(Gamepad currentG1, Gamepad prevG1) {
        currentGamepad1Index.updateControls(currentG1);
        prevGamepad1Index.updateControls(prevG1);

        for (GamepadIndexValues control : GamepadIndexValues.values()) {
            boolean currentState = currentGamepad1Index.getControl(control);
            boolean prevState = prevGamepad1Index.getControl(control);

            risingEdgesG1.put(control, currentState && !prevState);
            fallingEdgesG1.put(control, !currentState && prevState);
        }
    }

    public void refreshGamepad2Index(Gamepad currentG2, Gamepad prevG2) {
        currentGamepad2Index.updateControls(currentG2);
        prevGamepad2Index.updateControls(prevG2);

        for (GamepadIndexValues control : GamepadIndexValues.values()) {
            boolean currentState = currentGamepad2Index.getControl(control);
            boolean prevState = prevGamepad2Index.getControl(control);

            risingEdgesG2.put(control, currentState && !prevState);
            fallingEdgesG2.put(control, !currentState && prevState);
        }
    }

    public boolean rising(GamepadIndexValues control) {
        return risingG1(control);
    }

    public boolean falling(GamepadIndexValues control) {
        return fallingG1(control);
    }

    public boolean risingG1(GamepadIndexValues control) {
        return Boolean.TRUE.equals(risingEdgesG1.getOrDefault(control, false));
    }

    public boolean fallingG1(GamepadIndexValues control) {
        return Boolean.TRUE.equals(fallingEdgesG1.getOrDefault(control, false));
    }

    public boolean risingG2(GamepadIndexValues control) {
        return Boolean.TRUE.equals(risingEdgesG2.getOrDefault(control, false));
    }

    public boolean fallingG2(GamepadIndexValues control) {
        return Boolean.TRUE.equals(fallingEdgesG2.getOrDefault(control, false));
    }

    /**
     * @noinspection unused
     */
    public boolean isEdge(GamepadIndexValues control) {
        return risingG1(control) || fallingG1(control);
    }

    public boolean isEdgeG2(GamepadIndexValues control) {
        return risingG2(control) || fallingG2(control);
    }
}
