package org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad;


import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.EnumMap;

public class GamepadIndex {

    // EnumMap to store boolean values for each control
    private final EnumMap<GamepadIndexValues, Boolean> controls = new EnumMap<>(GamepadIndexValues.class);

    // Update the boolean values based on the current state of the controls
    public void updateControls(Gamepad gamepad) {
        if (gamepad == null) {
            for (GamepadIndexValues control : GamepadIndexValues.values()) {
                controls.put(control, false);
            }
            return;
        }
        controls.put(GamepadIndexValues.cross, gamepad.cross);
        controls.put(GamepadIndexValues.circle, gamepad.circle);
        controls.put(GamepadIndexValues.dpadDown, gamepad.dpad_down);
        controls.put(GamepadIndexValues.dpadLeft, gamepad.dpad_left);
        controls.put(GamepadIndexValues.dpadRight, gamepad.dpad_right);
        controls.put(GamepadIndexValues.dpadUp, gamepad.dpad_up);
        controls.put(GamepadIndexValues.guide, gamepad.guide);
        controls.put(GamepadIndexValues.leftBumper, gamepad.left_bumper);
        controls.put(GamepadIndexValues.leftStickButton, gamepad.left_stick_button);
        controls.put(GamepadIndexValues.leftStickX, gamepad.left_stick_x > 0);
        controls.put(GamepadIndexValues.leftStickY, gamepad.left_stick_y > 0);
        controls.put(GamepadIndexValues.leftTrigger, gamepad.left_trigger > 0);
        controls.put(GamepadIndexValues.rightBumper, gamepad.right_bumper);
        controls.put(GamepadIndexValues.rightStickButton, gamepad.right_stick_button);
        controls.put(GamepadIndexValues.rightStickX, gamepad.right_stick_x > 0);
        controls.put(GamepadIndexValues.rightStickY, gamepad.right_stick_y > 0);
        controls.put(GamepadIndexValues.rightTrigger, gamepad.right_trigger > 0);
        controls.put(GamepadIndexValues.square, gamepad.square);
        controls.put(GamepadIndexValues.triangle, gamepad.triangle);
        controls.put(GamepadIndexValues.options, gamepad.options);
        controls.put(GamepadIndexValues.share, gamepad.share);
    }

    // Get the boolean value for a specific control
    public boolean getControl(GamepadIndexValues control) {
        //this syntax to handle possible null values
        return Boolean.TRUE.equals(controls.getOrDefault(control, false));
    }
}
