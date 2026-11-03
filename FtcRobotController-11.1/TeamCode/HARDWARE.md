# TeamCode Robot Hardware & Subsystem Reference

Concise map of every actuator, sensor, constraint, and control protocol in the
`teamcode` extension of the Decode SDK (FTC, SDK 11.1).

Source root: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`

## Architecture in one paragraph

Hardware is accessed only through the **HardwareInterface** layer
(`Motor/MotorControl`, `Servo/ServoControl`, `Sensor/SensorControl`). Each
**Subsystem** (Drivebase, Intake, Outtake) is split into small per-device
`*Control` classes plus a static `*States` holder that stores the current enum
state for every device. Operator input flows: gamepad → `Control/Buttons/*Logic`
→ `*Control` → sets a state in `IntakeStates` / `OuttakeStates` → the subsystem
`*Control.update()` reads that state and writes hardware. The main loop is
`Main/IterativeController.TeleOp()`, which calls `buttonControl.update()`,
`intakeControl.update()`, `outtakeControl.update()`, then writes **all** motor
powers once via `motorControl.setMotors(MotorConstants.all)`.

---

## 1. Hardware device inventory (config names)

These are the exact strings that must match the Robot Controller configuration.

### Motors — 8× `DcMotorEx` (`HardwareInterface/Motor/MotorControl.java`)

| Index | Config name | Subsystem | Direction | Zero-power | Run mode |
|------|----------------------|-----------|-----------|------------|----------|
| 0 | `frontLeftMotor`  | Drivebase | REVERSE | BRAKE | RUN_WITHOUT_ENCODER |
| 1 | `backLeftMotor`   | Drivebase | REVERSE | BRAKE | RUN_WITHOUT_ENCODER |
| 2 | `frontRightMotor` | Drivebase | FORWARD | BRAKE | RUN_WITHOUT_ENCODER |
| 3 | `backRightMotor`  | Drivebase | FORWARD | BRAKE | RUN_WITHOUT_ENCODER |
| 4 | `outtake1Motor`   | Outtake flywheel | REVERSE | FLOAT | RUN_USING_ENCODER |
| 5 | `intakeMotor`     | Intake | FORWARD | BRAKE | RUN_WITHOUT_ENCODER |
| 6 | `transferMotor`   | Intake/Transfer | FORWARD | BRAKE | RUN_WITHOUT_ENCODER |
| 7 | `outtake2Motor`   | Outtake flywheel | FORWARD | FLOAT | RUN_USING_ENCODER |

- The **outtake flywheel is 2 motors** (`outtake1Motor` + `outtake2Motor`),
  driven as one logical unit via the combined index `MotorConstants.outtake`
  (= `{outtake1, outtake2}`). They run opposed directions (REVERSE/FORWARD) so
  the wheel spins one way, use velocity (encoder) control, and FLOAT on stop so
  the wheel coasts down.
- The **intake is 1 motor** (`intakeMotor`); the **transfer is 1 motor**
  (`transferMotor`).
- Combined-group indexes are defined in `MotorConstants.motorConfig` (e.g.
  `allDrive`=8, `leftDrive`=9, `rightDrive`=10, `frontLeftBackRight`=11,
  `frontRightBackLeft`=12, `all`=13, `notDrive`=14, `notOuttake`=15,
  `outtake`=16). `MotorControl` accumulates per-motor power in `motorSpeeds[]`
  and flushes once per loop; velocity-controlled motors are skipped by
  `setMotors()` so a stray `setPower` can't clobber the flywheel velocity target.

### Servos — 3× `Servo` (`HardwareInterface/Servo/ServoControl.java`, indexes in `ServoConstants.java`)

| Index | Config name | Subsystem | Role |
|------|---------------|-----------|------|
| 0 | `lockServo`    | Intake/Transfer | Ball lock / release gate |
| 1 | `turretServo1` | Outtake turret | Turret rotation (servo 1) |
| 2 | `turretServo2` | Outtake turret | Turret rotation (servo 2) |

- The **turret is actuated by 2 servos** (`turretServo1/2`) driven in unison
  via `ServoControl.setTurretServosPos(pos)`. Each servo gets `pos * multN` so
  mechanical mismatch can be trimmed (multipliers below).
- A commented-out alternative exists for a single `CRServo "turretServo"` with
  feedback from `AnalogInput "turretAnalog"` — not currently wired.
- Write-gating: turret writes skip if base position changed by
  `< TURRET_POS_EPSILON (0.001)`; every servo also has a per-servo gate
  `SERVO_POS_EPSILON (0.001)` to avoid redundant ~1.5 ms serial writes.

### Sensors (`HardwareInterface/Sensor/SensorControl.java`)

| Config name | Type | Purpose |
|-------------|------|---------|
| `pinpointIMU` | Two-wheel odometry localizer | Pose (x, y, heading), velocity |
| `limelight`   | `Limelight3A` | AprilTag vision, pose reset & fusion |
| `infraTransfer1` | `InfraRedSensor` (digital port 3) | Transfer artifact detection |
| `infraTransfer2` | `InfraRedSensor` (digital port 5) | Transfer artifact detection |
| `infraIntake1`   | `InfraRedSensor` (digital port 6) | Intake artifact detection |
| `infraIntake2`   | `InfraRedSensor` (digital port 7) | Intake artifact detection |
| (voltage)     | `VoltageSensor` (hardwareMap) | Battery voltage |
| `led`         | `RevBlinkinLedDriver` | Status LED (driven in `IterativeController`) |

- **Artifact detection is four digital IR sensors**, in two pairs, each pair
  OR'd together so either sensor firing is enough:
  - **Transfer pair** — `infraTransfer1` (port 3) + `infraTransfer2` (port 5),
    exposed as `SensorControl.isTransferBall()`. Stops the **transfer**.
  - **Intake pair** — `infraIntake1` (port 6) + `infraIntake2` (port 7),
    exposed as `SensorControl.isIntakeBall()`. Stops the **intake**.
- All four are `InfraRedSensor` (a custom `@DigitalIoDeviceType`, xmlTag
  `InfraRedSensor`) configured `SwitchConfig.NC`; a sensor reads "artifact
  present" when `isObstructed()` is true. The `infraRedSensors[]` array in
  `SensorControl` is indexed by `InfraRedSensors` ordinal — keep the two in the
  same order.
- Reads are digital (cheap), so unlike the previous I2C color/range sensors they
  are **not** cached or throttled.
- Encoders: only `outtake1Motor` / `outtake2Motor` use encoders (velocity
  control). Drive uses the external `pinpointIMU` two-wheel localizer, not motor
  encoders.

---

## 2. Drivebase

Files: `Subsystems/Drivebase/Drivebase.java`, `DrivebaseController.java`,
`DrivebaseConstants.java`, `DrivebaseTrigger.java`.

- Mecanum, 4 motors (`allDrive`). Two modes toggled by `switchDrivingMode()`:
  - **Driver-oriented** (field-centric): rotates stick vector by
    `sensorControl.getLocalizerAngle()`.
  - **Robot-oriented**.
- Speed scalar `DrivebaseConstants.getDriveSpeed()` = `0.5` when
  `GlobalVariables.slowMode`, else `1.0`.
- `slowMode` also switches the active gamepad (`gamepad2` when slow, else
  `gamepad1`) in `Drivebase.selectGamepad()`.
- Power normalization: divides by `max(|y|+|x|+|rot|, 1)` then multiplies by
  `maxSpeed`.

---

## 3. Intake subsystem

Files: `Subsystems/Intake/` (`IntakeControl`, `IntakeConstants`, `IntakeStates`,
`IntakeMotor/`, `TransferMotor/`, `LockServo/`, `AutoIntakeTransfer/`,
`AutoIntakeMovement/`).

`IntakeControl.update()` ticks, in order: AutoIntakeMovement, AutoIntakeTransfer
(control + logic), IntakeMotor, TransferMotor, LockServo.

### 3.1 Intake motor (`IntakeMotor/IntakeMotorControl.java`)

State enum `IntakeMotorStates`: `forward`, `backward`, `idle`.

| State | Action |
|-------|--------|
| `forward`  | `intakeMotor` power **+1.0** |
| `backward` | `intakeMotor` power **−1.0** |
| `idle`     | power 0 |

### 3.2 Transfer motor (`TransferMotor/TransferMotorControl.java`)

State enum `TransferMotorStates`: `forward`, `backward`, `idle`.

| State | Action |
|-------|--------|
| `forward`  | `transferMotor` power **+0.7** |
| `backward` | `transferMotor` power **−1.0** |
| `idle`     | power 0 |

### 3.3 Lock servo / release (`LockServo/LockServoControl.java`)

State enum `LockServoStates`: `lock`, `unlock`, `idle`. Positions from
`IntakeConstants`:

- `lockServoMinPos = 0.044` → **lock** (holds balls)
- `lockServoMaxPos = 0.3`   → **unlock** (release into flywheel)
- Initial state set to `lock` in `IntakeStates.setInitialStates()`; start
  position written by `ServoControl.setServoStartPos()` to `lockServoMinPos`.

### 3.4 AutoIntakeTransfer — index-and-stop ball loading protocol

Files: `AutoIntakeTransfer/AutoIntakeTransferControl.java` (writes states) +
`AutoIntakeTransferLogic.java` (sensor decisions). State enum
`AutoIntakeTransferStates`: `activate`, `checkAgainMid`, `stopTransfer`,
`checkAgainFront`, `stop`, `idle`.

Purpose: pull balls in and stage them without jamming. Debounce wait
`checkAgainAfter = 0.2 s`.

Protocol (Logic drives transitions, Control sets hardware). Note the state names
still say Mid/Front for historical reasons; the **transfer** pair drives the
`Mid` states and the **intake** pair drives the `Front` states:
1. `activate` → intake **forward** + transfer **forward**. When the **transfer**
   pair (`isTransferBall()`, port 3 or 5) sees an artifact → go to
   `checkAgainMid` (start 0.2 s debounce).
2. `checkAgainMid` → after debounce, re-check the transfer pair. Still seen →
   `stopTransfer`; otherwise → back to `activate`.
3. `stopTransfer` → transfer **idle** (intake keeps running). When the **intake**
   pair (`isIntakeBall()`, port 6 or 7) sees an artifact → `checkAgainFront`
   (0.2 s debounce).
4. `checkAgainFront` → after debounce, re-check the intake pair. Still seen →
   `stop`; otherwise → back to `stopTransfer`.
5. `stop` → intake **idle** + transfer **idle**, `gamepad1.rumble(300)` to
   signal "loaded", then → `idle`.

### 3.5 AutoIntakeMovement — drive-direction-gated intake

File: `AutoIntakeMovement/AutoIntakeMovementControl.java`. State enum
`AutoIntakeMovementStates`: `activate`, `idle`.

- `activate` → if `sensorControl.isDrivingForward()`: intake+transfer
  **forward**; if `isDrivingBackward()`: intake+transfer **idle**.
- Driving direction comes from localizer velocity; thresholds in `SensorControl`
  (`MIN_DRIVE_DIRECTION_VELOCITY_IN_PER_SEC = 6.9`, separation 90°).

---

## 4. Outtake subsystem (flywheel + turret)

Files: `Subsystems/Outtake/` (`OuttakeControl`, `OuttakeConstants`,
`OuttakeStates`, `OuttakeMotor/`, `TurretServo/`, `AutoCycleShoot/`,
`AutoOuttakeFarClose/`, `AutoResetPos/`).

`OuttakeControl.update()` ticks: OuttakeMotor, AutoCycleShoot (control + logic),
TurretServo, AutoOuttakeFarClose, AutoResetPos.

### 4.1 Flywheel motors (`OuttakeMotor/OuttakeMotorControl.java`)

State enum `OuttakeMotorStates`: `autonomous`, `forwardStart`, `forwardFar`,
`forwardClose`, `backward`, `idle`. Both flywheel motors driven via
`MotorConstants.outtake`.

| State | Action |
|-------|--------|
| `autonomous`  | power `0.64` if near goal, else `outtakeSpeedFar − 0.03` (= 0.86) |
| `forwardFar`  | power `outtakeSpeedFar = 0.89` |
| `forwardClose`| `setMotorRPM(outtake, 1900)` (velocity/encoder control); also computes a distance-interpolated power into `GlobalVariables.outtakeTargetSpeed` |
| `backward`    | power `−0.5` |
| `idle`        | `setMotorRPM(outtake, 0)` |

Distance-based close speed (`calculateSpeed()`), distance from
`SensorControl.getDistanceFromLocalizer()`:
- `≤ minDistance (53.94)` → `outtakeSpeedCloseClose = 0.62`
- `≥ maxDistance (98.43)` → `outtakeSpeedCloseFar = 0.78`
- between → linear interpolation between those two.

Velocity PIDF (default in `MotorControl.setMotorRPM`):
`PIDFCoefficients(p=60, i=0, d=0, f=11.75)`.

### 4.2 Turret — 2-servo rotation (`TurretServo/TurretServoControl.java`)

State enum `TurretServoStates`: `adjust`, `idle`. The turret continuously aims
at the goal whenever `OuttakeStates.isTurretTrackingEnabled()` is true (toggled
by Right Bumper).

Geometry / constraints:
- Angle limits: `turretLimitRight = -100.0°`, `turretLimitLeft = 120.0°` (defined in
  `OuttakeConstants.java`). With `turretDirection = -1`, the right limit is the one
  near `turretServoMax` (`-100° → 0.81`); pushing it further stalls the turret
  against its hard stop.
- `turretGearRatio = 1.0`, `turretServoTravel = 323.0°`, `turretDirection = -1.0`.
- Mapping: `servoPos = (targetAngleDeg * turretGearRatio * turretDirection) / turretServoTravel + 0.5`,
  then clamped to `[turretServoMin, turretServoMax]`. Flip `turretDirection` if the
  turret ever aims to the side opposite the target.
- Dead-zone hold: when the aim target is outside the reachable window, the turret
  parks at the limit the target left from and holds it until the target re-enters
  the window (prevents the ±180° wrap from flipping the clamp between the two
  limits during fast drivetrain rotation).
- Slew limit: the commanded position is rate-limited to
  `turretServoSlewPerSec = 2.0` (full travel per 0.5 s) so target jumps become
  controlled sweeps instead of full-speed slams.
- Per-servo multipliers (`OuttakeConstants`): `turretServo1Mult = 1.0`,
  `turretServo2Mult = 0.998`.
- Servo travel range (`OuttakeConstants`): `turretServoMin = 0.0`,
  `turretServoMax = 0.78` (per-servo max/min are these × the multiplier).
- `ServoConstants.servoMinPos/servoMaxPos` enforce bounds at the
  `ServoControl.setServoPos` level (turret bounds are the per-servo Max/Min
  values). `setTurretServosPos` clamps its base position to
  `[turretServoMin, turretServoMax]` before applying the per-servo multipliers.

Aiming math (`SensorControl.getTurretTargetAngleDegrees()`):
- Goal position depends on alliance: Red `(62, 62)`, Blue `(-62, 62)` inches.
- `targetAngle = angleToGoal − robotHeading + velocityModifier`, normalized to
  ±180°, then clamped to `[turretLimitRight, turretLimitLeft]`.
- `velocityModifier` = lead compensation from localizer velocity
  (`±0.005 × velocity` per axis, alliance-dependent sign).
- Far mode and autonomous use fixed heading targets instead of live atan2 (see
  the `GlobalVariables.far` / `isAutonomous` branches).

### 4.3 AutoOuttakeFarClose — spin-up selector (`AutoOuttakeFarClose/`)

State enum `AutoOuttakeFarCloseStates`: `cycle`, `idle`.
- `cycle` → if `GlobalVariables.far`: flywheel → `forwardFar`; else →
  `forwardClose`. This is how Left Trigger spins the wheel up.

### 4.4 AutoCycleShoot — feed-and-shoot protocol (`AutoCycleShoot/`)

Files: `AutoCycleShootControl.java` (writes intake/transfer/lock states) +
`AutoCycleShootLogic.java` (timing & sensor gating). State enum
`AutoCycleShootStates`: `activate`, `turnBack`, `turnTransfer`, `stop`,
`turnTransferBack`, `deactivate`, `idle`.

Relevant constants: `oneBallWait = 0.1`, `servoOpenWait = 0.1`,
`deactivateAfter = 0.3`, `FAR_MIN_OUTTAKE_VELOCITY = 2080.0` ticks/s,
ball check throttled to 50 ms.

Protocol:
1. `activate` → **unlock** lock servo. Logic: if no ball seen at sensors →
   `turnBack` (+0.1 s); else wait `servoOpenWait` then → `turnTransfer`.
2. `turnBack` → transfer **backward** (nudge ball back off the wheel); after
   wait → `turnTransfer`.
3. `turnTransfer` → feed: intake **forward** + transfer **forward**. In **far +
   teleop** it just feeds; in **far + autonomous** it only feeds while flywheel
   velocity is in the `[FAR_MIN_OUTTAKE_VELOCITY−200, FAR_MIN_OUTTAKE_VELOCITY]`
   window (waits for spin-up); in **close** it always feeds.
4. `stop` → Logic goes to `turnTransferBack` (in teleop adds `deactivateAfter`
   0.3 s).
5. `turnTransferBack` → transfer **backward**; after wait → `deactivate`.
6. `deactivate` → intake **idle**, transfer **idle**, lock servo **lock** →
   `idle`.

### 4.5 AutoResetPos — Limelight pose reset (`AutoResetPos/`)

State enum `AutoResetPosStates`: `resetPos`, `waitForReset`, `idle`.
- `resetPos` → repeatedly calls `SensorControl.resetLocalizerWithLimelight()`
  until it returns true (averages `LimelightFrames = 7` valid AprilTag readings,
  or times out after `LIMELIGHT_RESET_TIMEOUT_MS = 3000`), then → `waitForReset`.

---

## 5. Sensor / vision details (`SensorControl.java`)

- **Localizer**: `TwoWheelTrackingLocalizer` (`pinpointIMU`). Provides pose &
  velocity; `getDistanceFromLocalizer()` returns range to the alliance goal.
- **Goal coordinates (in)**: Red `(62, 62)`, Blue `(−62, 62)`; field half =
  `66.93 in`. AprilTag target ID: Red `24`, Blue `20`.
- **Vision fusion** (`applyContinuousVisionFusion`, currently commented out in
  `IterativeController`): gated by speed
  (`LINEAR_VELOCITY_THRESHOLD = 6.0 in/s`,
  `ANGULAR_VELOCITY_THRESHOLD = 10°/s`), distance window
  `[MIN 10 in, MAX 120 in]`, 7-frame trimmed-average sliding window, blended at
  `CONTINUOUS_FUSION_ALPHA = 0.1` (2–10% vision per frame).
- **Artifact sensors**: `isTransferBall()` (IR ports 3 ∥ 5) and `isIntakeBall()`
  (IR ports 6 ∥ 7); both delegate to `isInfraRedObstructed(InfraRedSensors)`.
- **Shooting helpers**: `getHoodTicksFromDegrees(d) = 0.02·d − 0.7`,
  `getFlywheelTicksFromVelocity(v) = 94.501·v/12 − 187.96 + flywheelOffset`.

---

## 6. Operator controls (gamepad → action)

Mapped in `Subsystems/Control/ButtonControl.java` (edge-triggered), then each
`*Logic`/`*Control` sets subsystem states.

| Input | Action |
|-------|--------|
| Right Trigger | Toggle AutoIntakeTransfer: start ball-loading, or stop (also locks servo, cancels shoot) |
| Right Bumper  | Toggle turret tracking on/off |
| Left Trigger  | Toggle flywheel spin-up (AutoOuttakeFarClose `cycle`) / stop shooter |
| Left Bumper   | Shoot (AutoCycleShoot `activate`) / stop |
| Circle        | Auto intake-movement on/off (drive-direction gated) |
| Square        | Reset pose from Limelight (AutoResetPos) |
| Dpad Up       | Toggle transfer/intake reverse (eject) |
| Dpad Right    | Toggle lock servo lock/unlock |
| Right Stick Button (in TeleOp) | Toggle `GlobalVariables.far` (LED: far = SKY_BLUE, close = HOT_PINK) |

`GlobalVariables` flags: `far` (far/close shot mode), `slowMode` (half speed +
gamepad2 drive), `isAutonomous`, `wasAutonomous`, `alliance`,
`outtakeTargetSpeed`, `lastTurretAngle`.

---

## 7. Quick constants reference

`OuttakeConstants`: `turretServo1Mult 1.0`, `turretServo2Mult 0.998`,
`turretServoMax 0.78`, `turretServoMin 0.0`,
`maxDistance 98.43`, `minDistance 53.94`, `oneBallWait 0.1`, `servoOpenWait 0.1`,
`deactivateAfter 0.3`, `resetWait 100.0`, `outtakeSpeedCloseClose 0.62`,
`outtakeSpeedFar 0.89`, `outtakeSpeedCloseFar 0.78`, `farShootingThreshold 2300`,
`targetSpeedThreshold 0.02`.

`IntakeConstants`: `stopFeederAfter 0.1`, `lockServoMinPos 0.044`,
`lockServoMaxPos 0.3`, `checkAgainAfter 0.2`.

`TurretServoControl`: (uses `OuttakeConstants`: `turretLimitRight −100.0`, `turretLimitLeft 120.0`,
`turretGearRatio 1`, `turretServoTravel 323`, `turretDirection −1.0`,
`turretServoSlewPerSec 2.0`).

`SensorControl`: goal Red `(62,62)` / Blue `(−62,62)`,
`FieldHalfInches 66.93`, `LimelightFrames 7`,
`LIMELIGHT_RESET_TIMEOUT_MS 3000`.
