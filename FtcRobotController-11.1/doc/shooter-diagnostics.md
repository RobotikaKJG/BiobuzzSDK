# Shooter diagnostics

Logging starts automatically after Start in General Red/Blue TeleOp, General Autonomous,
Shooter Distance Tuning TeleOp, and Outtake Manual Control. Stop normally to flush the file.
The logger retains the newest 40 sessions. Outtake Manual Control records motors and
battery only; use normal TeleOp to capture servos, ball sensors and localization too.
The header's sensorsAvailable/servosAvailable flags identify available hardware context;
ignore subsystem states and ball flags in motor-only manual sessions.

## Reproduce and retrieve

1. Install the debug build with `./gradlew :TeamCode:installDebug`.
2. Record a short run at a fixed shooting position: spin up, wait for steady speed,
   then shoot several balls. Include both successful and failed shots. Note their order.
3. Repeat using the same conditions and a charged battery. Stop the OpMode.
4. With the robot connected through ADB, run `adb forward tcp:8765 tcp:8765`.
5. Open `http://localhost:8765/api/sessions` for the list, then download a file from
   `http://localhost:8765/api/sessions/FILE_NAME.jsonl`. The existing telemetry server
   must be running; it is initialized by the Robot Controller app.

## Recorded evidence

The header gives motor/servo array order. All motor speeds and velocity commands are
encoder ticks/second. Outtake RPM = ticks/second * 60 / 28. Other motors have their own
gear ratios. Arrays record all eight encoder positions, velocities, last written power
commands, and velocity targets. Null command entries indicate the other control mode or
an unavailable command, not zero power. Power commands are not measured applied voltage
or measured controller output during velocity control.

Existing fields record all eight currents, shooter state, intake/transfer states,
ball detection, battery voltage, goal distance, turret command-derived angle and pose.
Currents rotate one motor per sample to limit hub traffic. currentMotor identifies the
motor refreshed this sample; currentReadEpochMs records the actual cached ADC read times.
Unmeasured currents are omitted. Battery samples occur at most every 250 ms. These rates
can miss short electrical transients. Servo commands are requested positions, not measured
physical feedback; servo currents and physical shaft positions are not available here.

sampleIntervalMs is elapsed time between recorded samples (including intentional idle
throttling), captureMs measures logging acquisition overhead, and droppedSamples counts
records discarded when the bounded disk queue fills. Active sampling follows the control
loop; idle sampling is approximately 10 Hz. Logging still needs validation on the robot
for its effect on loop timing.

## Interpretation

- Speed drops with a stable target during ball contact: inspect recovery time and whether
  the next ball feeds before recovery. It can indicate feed/control timing or mechanical load.
- One shooter consistently slower, especially with greater current: inspect friction,
  alignment, coupling, wiring, motor and encoder. Logs alone cannot identify the failed part.
- Both slow alongside falling battery voltage: investigate battery, connectors and power load.
- Targets or servo commands jump before bad shots: investigate state transitions and aiming.
- Commands stay consistent but physical servo motion differs: inspect the mechanism and
  servo/power supply; command telemetry cannot verify movement.

Compare good and bad shots under matched conditions before attributing faults to equipment.

## Inferred shot timing

Each record has inferredShot and inferredShotCount. An inferred event's `t` gives
milliseconds after session start; inferredShotIntervalMs gives time since the previous
event (omitted for the first). Detection uses the mean shooter RPM: at least a 150 RPM
drop from a peak within 200 ms, after a shared positive velocity target has been unchanged
for 300 ms and speed has reached 90% of target. It rearms only after recovering to 90%
and at least 250 ms since the previous event. Target changes and stopping disarm it.
This works in Outtake Manual Control without extra sensors and leaves telemetry prints
unchanged. Thresholds are initial estimates requiring validation against observed shots.
Small dips, closely spaced shots, shots before recovery, and power-controlled operation
may not be detected; jams or other loads may produce false events. Timestamps mark the
observed dip at sample resolution, not the exact ball exit time. Intervals include any
undetected shots. No automatic feeding decisions use these inferred events.
