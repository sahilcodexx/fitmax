Bro, let's approach this as if we're developing a real Android product, not just making a basic step-counter app.

I want to explain the entire system from the phone's physical sensors to the final number displayed on the screen: how data is collected, how we calculate steps, how we distinguish walking from random phone movement, how we support different Samsung, Vivo, Xiaomi and other Android phones, and how we test whether our result is actually accurate.

The most important engineering principle is:

We are building a human walking detection system using smartphone motion sensors. We are not building a movement counter.

That distinction will determine almost every technical decision.

# 1. Understand the complete system first

Our system will have six major parts.

1. Smartphone hardware

Accelerometer · Gyroscope · Hardware step sensor

1. Sensor collection

Collect timestamped motion data

1. Signal processing

Gravity removal · Filtering · Noise reduction

1. Walking recognition

Cadence · Periodicity · Step candidates · Classification

1. Step counting engine

Validate candidates · Reject false steps · Reconcile counts

1. Final daily step count

History · Distance · Goals · Statistics

We will develop these parts independently so that we can identify exactly where inaccuracies happen.

# 2. First understand the phone hardware

Before writing the algorithm, we need to understand what information a phone can actually give us.

A smartphone contains several motion sensors.

## A. Accelerometer

2 Accelerometer Sensor \[8\] | Download Scientific Diagram

[researchgate.net](https://www.researchgate.net/figure/Accelerometer-Sensor-8_fig3_340537078?utm_source=chatgpt.com)

This is our most important raw sensor.

It measures acceleration along three axes:

- X: side-to-side movement.
- Y: forward-and-back movement.
- Z: up-and-down movement.

The units are generally metres per second squared (m/s²).

For example, a sensor reading could be:

text

```text
Time       X       Y       Z
0.000     0.12    0.18    9.72
0.020     0.21    0.33    9.85
0.040     0.48    0.19    9.42
0.060     0.17   -0.12    9.61
0.080    -0.20    0.25    9.91
```

These are illustrative readings, not actual walking measurements.

The phone constantly experiences gravity, approximately 9.81 m/s². Therefore, the accelerometer doesn't simply give us walking movement.

We must separate gravity from movement.

## B. Gyroscope

IMU | autonomousvehicles.github.io

[sallypoon.github.io](https://sallypoon.github.io/autonomousvehicles.github.io/imu/?utm_source=chatgpt.com)

The gyroscope measures rotational velocity:

- Rotation around X.
- Rotation around Y.
- Rotation around Z.

Its units are radians per second.

Why do we need it?

Imagine two situations:

1. You're walking while holding your phone steadily.
2. You're sitting and rotating your phone in your hand.

The accelerometer alone may struggle to distinguish some of these movements.

The gyroscope provides additional context about how the phone is rotating.

However, I would not make it mandatory. Some devices have different sensor configurations, and continuous gyroscope processing can consume additional battery.

## C. Hardware step detector

Android exposes:

- `TYPE_STEP_DETECTOR`
- `TYPE_STEP_COUNTER`

These are special motion sensors, often implemented using low-power hardware.

The step detector produces individual step events.

The step counter provides a cumulative count since its activation or reset reference.

These should be our first choice when available.

## D. Other sensors

| Sensor              | Should we use it?                  |
| ------------------- | ---------------------------------- |
| Accelerometer       | Essential for our custom algorithm |
| Gyroscope           | Useful additional information      |
| Step detector       | Primary counting candidate         |
| Step counter        | Primary counting candidate         |
| Gravity sensor      | Optional                           |
| Linear acceleration | Useful alternative signal          |
| Magnetometer        | Not needed                         |
| GPS                 | Not needed for step detection      |
| Proximity sensor    | Not needed                         |

One important thing: a phone may not have every sensor, and two phones with the same Android version may behave differently.

Our software must inspect sensor availability at runtime rather than assuming that every device has the same hardware.

# 3. Decide our technology stack

I'd use native Android development.

| Component             | Technology                                |
| --------------------- | ----------------------------------------- |
| Programming language  | Kotlin                                    |
| IDE                   | Android Studio                            |
| UI                    | Jetpack Compose                           |
| Sensor access         | Android SensorManager                     |
| Background execution  | Foreground service where appropriate      |
| Local database        | Room                                      |
| Background scheduling | WorkManager                               |
| Graphs and testing    | Custom development tools                  |
| Minimum OS            | Decide after device compatibility testing |

Why Kotlin instead of Flutter or React Native?

Because our most important functionality involves direct access to Android sensors, permissions, lifecycle management and background execution.

Native Android gives us more direct control over those things.

# 4. Build the sensor collection layer

This is where development actually starts.

Our first objective is NOT to count steps.

Our first objective is to collect reliable sensor readings from the phone and understand what they look like.

### Step 1: Detect available sensors

When the application starts, Android should inspect the device.

Conceptually:

kotlin

```kotlin
val sensorManager =
    getSystemService(Context.SENSOR_SERVICE) as SensorManager

val accelerometer =
    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

val gyroscope =
    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

val stepCounter =
    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

val stepDetector =
    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
```

We then create a hardware capability report.

Example:

text

```text
Device: Samsung Galaxy

Accelerometer: Available
Gyroscope: Available
Hardware Step Counter: Available
Hardware Step Detector: Available
Linear Acceleration: Available

Custom Algorithm: Supported
Hardware Counting: Supported
```

If the step counter isn't available, we switch to our custom algorithm.

If the accelerometer isn't available either, we cannot promise independent step detection on that device.

### Step 2: Register the sensors

For our first experiment, collect accelerometer data at approximately 50 Hz.

That means:

- 50 readings per second.
- One reading approximately every 20 milliseconds.
- Each reading contains X, Y and Z.

Why 50 Hz?

It's a practical initial experimental sampling rate for human walking patterns. We can later compare 25, 50 and 100 Hz to see whether additional sampling actually improves accuracy.

Don't assume that Android delivers events at exactly the requested interval. Always use the event timestamps.

Example:

kotlin

```kotlin
sensorManager.registerListener(
    listener,
    accelerometer,
    20_000
)
```

The sampling period here is in microseconds.

For production, sensor registration and processing should be managed by a dedicated component rather than directly inside the screen.

### Step 3: Store raw sensor data

During development, store the following:

| Field         | Example                |
| ------------- | ---------------------- |
| Timestamp     | 1250000000 ns          |
| X             | 0.12                   |
| Y             | 0.35                   |
| Z             | 9.72                   |
| Sensor type   | Accelerometer          |
| Accuracy      | Sensor accuracy status |
| Screen state  | On/off                 |
| Session label | Walking while texting  |

Use monotonic sensor timestamps for calculations, not the wall-clock time.

This data becomes extremely valuable. We can replay the exact same walking session against different algorithm versions.

Do not start tuning thresholds without collecting actual sensor data.

# 5. The actual mathematics: How to process acceleration

Now we get to the core.

## Step 1: Calculate total acceleration

The phone reports three acceleration components.

We combine them:

$$
M(t)=\sqrt{x(t)^2+y(t)^2+z(t)^2}
$$

This is called the magnitude of the acceleration vector.

Why do we do this?

Imagine the user rotates the phone from portrait to landscape.

The X, Y and Z readings change, even if the person's walking motion remains similar.

Magnitude makes our first processing stage less dependent on phone orientation.

It does not solve every orientation problem, but it's a useful starting point.

### Example

Suppose:

$$
x=1.2,\quad y=0.8,\quad z=9.5
$$

Then:

$$
M=\sqrt{1.2^2+0.8^2+9.5^2}
$$

$$
M\approx9.61
$$

This is total acceleration magnitude, including gravity.

## Step 2: Remove gravity

Gravity is always present in accelerometer readings.

We don't want the gravity component to dominate our movement signal.

There are two approaches.

Approach A: Android linear acceleration sensor

kotlin

```kotlin
Sensor.TYPE_LINEAR_ACCELERATION
```

Android provides an estimate of acceleration without gravity.

This is convenient, but the sensor's implementation and quality can vary between devices.

Approach B: Calculate gravity ourselves

Use a low-pass filter:

$$
G_t=\alpha G_{t-1}+(1-\alpha)M_t
$$

Where:

- $G_t$ is estimated gravity.
- $M_t$ is current magnitude.
- $\alpha$ controls smoothing.

Then:

$$
D_t=M_t-G_t
$$

Now we have a movement signal with much of the slowly varying gravity component removed.

For example:

| Time | Magnitude | Gravity estimate | Movement |
| ---- | --------- | ---------------- | -------- |
| 1    | 9.81      | 9.80             | 0.01     |
| 2    | 10.20     | 9.82             | 0.38     |
| 3    | 9.40      | 9.81             | -0.41    |
| 4    | 10.45     | 9.84             | 0.61     |
| 5    | 9.60      | 9.83             | -0.23    |

These numbers are illustrative.

One important detail: magnitude-based gravity removal is an approximation. For more advanced processing, we can estimate gravity as a three-dimensional vector and subtract it from the individual axes.

## Step 3: Apply noise filtering

Our movement signal may contain:

- Sensor noise.
- Small hand movements.
- Vibrations.
- Phone rotation effects.
- Sudden impacts.

We should test a low-pass or band-pass digital filter.

For example, a Butterworth band-pass filter can initially be investigated around the human walking cadence frequency range.

A reasonable research starting point is approximately 0.5–3.5 Hz, but we should not treat this as a universal walking boundary.

Why?

Because:

- Different people walk at different speeds.
- Phone movement can contain harmonics.
- A step may generate more than one acceleration peak.
- Slow walking can produce weaker signals.

We should preserve the raw signal alongside the filtered signal so we can compare both.

Full article: Context-assisted personalized pedestrian dead reckoning localization with a smartphone

[tandfonline.com](https://www.tandfonline.com/doi/full/10.1080/10095020.2024.2338225?utm_source=chatgpt.com)

# 6. The most important part: Recognizing walking

This is where I would spend the most development time.

A movement peak does not equal one step.

Instead, we need to recognize a sequence of movements that resembles human walking.

I would build a detector with four stages.

## Stage A: Find candidate peaks

A candidate peak is a local maximum in our filtered movement signal.

A simple mathematical condition is:

$$
D_{i-1}<D_i>D_{i+1}
$$

This tells us that the current sample is higher than the samples immediately before and after it.

But that alone is insufficient.

A person shaking a phone can produce many peaks.

So we also calculate:

- Peak height.
- Peak prominence.
- Peak-to-peak time.
- Local signal energy.
- Whether the signal has a repeated pattern.

We should use peak prominence rather than relying exclusively on the absolute signal value.

## Stage B: Calculate the interval between peaks

Suppose our candidate peaks occur at:

text

```text
Peak 1: 0.00 seconds
Peak 2: 0.58 seconds
Peak 3: 1.13 seconds
Peak 4: 1.72 seconds
Peak 5: 2.29 seconds
```

The intervals are:

text

```text
0.58
0.55
0.59
0.57
```

This is a relatively consistent pattern.

We calculate cadence:

$$
Cadence=\frac{60}{AverageInterval}
$$

For an average interval of 0.5725 seconds:

$$
Cadence\approx104.8
$$

That's approximately 105 steps per minute, assuming each detected event corresponds to one step.

But be careful: some walking signals have two peaks per complete stride, while others have additional peaks from hand movement.

We need to validate the relationship between peaks and actual steps rather than assuming it.

## Stage C: Use autocorrelation

This is one of my favourite techniques for our particular problem.

Why?

Because the user might be walking while holding the phone relatively steadily.

The signal may have small peaks, but the pattern could still repeat.

Autocorrelation measures how similar a signal is to a time-shifted version of itself.

$$
R(\tau)=\sum_t x(t)x(t-\tau)
$$

Here, $\tau$ is the time shift.

Suppose our signal repeats approximately every 0.6 seconds.

Autocorrelation should show a strong similarity near that time shift.

autocorrelation - Interpreting Auto correlation of Human walk data - Cross Validated

[stats.stackexchange.com](https://stats.stackexchange.com/questions/136008/interpreting-auto-correlation-of-human-walk-data?utm_source=chatgpt.com)

This gives us a way to detect periodic walking even when the acceleration amplitude is relatively small.

We should calculate autocorrelation over a sliding window, not across the entire day's data.

## Stage D: Combine the evidence

We should never classify walking using only one feature.

Our detector should consider:

| Feature            | What it tells us                       |
| ------------------ | -------------------------------------- |
| Peak prominence    | Strength of candidate movement         |
| Cadence            | Possible walking speed                 |
| Autocorrelation    | Repeating movement                     |
| Signal energy      | Overall movement intensity             |
| Peak consistency   | Whether the pattern is stable          |
| Gyroscope activity | Additional rotational context          |
| Stationary history | Whether the phone was previously still |

The output should be a walking confidence score, not simply true or false.

For example:

text

```text
Walking confidence: 0.91
Periodicity: Strong
Cadence: 105 steps/min
Signal strength: Low
Peak consistency: Good

Decision: Walking likely
```

This is an illustrative example, not a measured result.

# 7. How to handle your special case: Walking while using the phone

Now let's design specifically for this.

A person might hold the phone in front of their face, read messages and walk.

The phone can be relatively stable compared with the movements of a phone inside a pocket.

If we use only peak amplitude, we may miss steps.

I would introduce three improvements.

### Improvement 1: Amplitude-independent detection

We shouldn't demand that every step produces a large movement spike.

Instead, evaluate:

- Whether weak peaks repeat.
- Whether their intervals are consistent.
- Whether the signal contains a stable walking frequency.
- Whether the pattern continues across multiple windows.

A small but periodic signal can be more meaningful than a large isolated spike.

### Improvement 2: Separate walking confidence from step confidence

These are two different questions:

1. Is the person probably walking?
2. How many steps can we reliably identify?

For example:

text

```text
Walking confidence: HIGH
Step event confidence: MEDIUM
```

The system may know that someone is walking but not confidently identify every individual step.

This is better than pretending every movement is a confirmed step.

### Improvement 3: Adaptive processing profiles

We can experiment with different signal profiles:

- Low-amplitude hand-held movement.
- Normal hand-held movement.
- Pocket movement.
- Bag movement.

However, we should not automatically assume the phone's position from a single sensor reading. Position classification itself is another imperfect inference problem.

Eventually, we can train a model to recognize these profiles from labelled data.

Important physical limitation: If the phone is held so steadily that the person's steps produce no measurable motion at the phone, accelerometer and gyroscope data cannot reconstruct those missing steps. We can improve detection of subtle signals, but cannot guarantee detection of every step in that situation.

# 8. Hardware counting versus our custom counting

We need to make an important architectural decision here.

I would NOT replace Android's hardware counter immediately.

The hardware counter may already be better than our custom algorithm because it can use low-power sensor processing that isn't exposed to ordinary apps.

Android documents the following behaviour:

| Sensor               | Behaviour                                                          |
| -------------------- | ------------------------------------------------------------------ |
| `TYPE_STEP_COUNTER`  | Cumulative steps, with possible reporting latency up to 10 seconds |
| `TYPE_STEP_DETECTOR` | Individual step events, expected latency below 2 seconds           |
| Accelerometer        | Raw acceleration measurements                                      |
| Gyroscope            | Rotational velocity                                                |

Android Developers

+1

## The strategy I recommend

Develop two independent engines:

Engine AHardware step engine

Reads the Android hardware step counter and detector when available.

Engine BCustom step engine

Uses accelerometer, optional gyroscope, filtering and adaptive walking detection.

Reconciliation engine

Compares their outputs during development, identifies missing or suspicious measurements and chooses the appropriate production source.

For example:

text

```text
Hardware counter:  1,000
Custom algorithm:    970

Difference:            30
```

This doesn't mean we should add 1,000 and 970.

Instead, we investigate the difference using manually counted ground truth.

If hardware consistently performs better for hand-held walking, use it.

If a particular device lacks hardware support, use our custom algorithm.

If hardware has poor performance on a particular model, we can evaluate a device-specific alternative.

I would initially make the production source selection explicit:

text

```text
IF hardware step counter available:
    USE hardware counter
ELSE:
    USE custom algorithm
```

Later, after sufficient testing, we can introduce carefully validated model-specific reconciliation.

Never switch between counters without maintaining a baseline and deduplication state.

# 9. Build the actual custom counting engine

Let's define the software structure.

text

```text

StepCountingSystem/
│
├── sensors/
│   ├── SensorDiscovery.kt
│   ├── AccelerometerCollector.kt
│   ├── GyroscopeCollector.kt
│   └── HardwareStepCollector.kt
│
├── processing/
│   ├── GravityFilter.kt
│   ├── SignalFilter.kt
│   ├── MagnitudeCalculator.kt
│   └── WindowManager.kt
│
├── detection/
│   ├── PeakDetector.kt
│   ├── CadenceAnalyzer.kt
│   ├── Autocorrelation.kt
│   ├── WalkingClassifier.kt
│   └── StepValidator.kt
│
├── counting/
│   ├── CustomStepCounter.kt
│   ├── HardwareStepCounter.kt
│   └── CounterReconciler.kt
│
├── storage/
│   ├── StepDatabase.kt
│   ├── StepRepository.kt
│   └── DailyStepManager.kt
│
├── background/
│   └── StepTrackingService.kt
│
└── ui/
    ├── Dashboard.kt
    └── History.kt
```

Each component should have one responsibility.

For example, `PeakDetector.kt` should not know anything about the database or UI.

It should simply receive a processed signal and return candidate peak events.

## How the custom engine should operate

I would divide the processing into windows.

For the first prototype, try a 3-second window, with a 1-second hop (the amount by which the window advances).

dsp.STFT - Short-time FFT - MATLAB

[in.mathworks.com](https://in.mathworks.com/help/dsp/ref/dsp.stft.html?utm_source=chatgpt.com)

Example:

text

```text
Window 1:  0 - 3 seconds
Window 2:  1 - 4 seconds
Window 3:  2 - 5 seconds
Window 4:  3 - 6 seconds
```

Why overlap?

Because we don't want to miss a step simply because it happens near the boundary between two windows.

We must also preserve detector state between windows, and avoid counting the same peak twice.

### The window processing sequence

text

```text
Receive sensor samples
       |
       v
Check timestamps and missing samples
       |
       v
Calculate acceleration magnitude
       |
       v
Remove gravity
       |
       v
Apply digital filter
       |
       v
Calculate signal features
       |
       v
Find candidate peaks
       |
       v
Calculate cadence and periodicity
       |
       v
Evaluate walking confidence
       |
       v
Validate candidate step events
       |
       v
Update count
```

### How to validate a step

Every candidate should be evaluated using:

1. Is its signal prominence meaningful relative to recent noise?
2. Is its timing compatible with the current estimated cadence?
3. Does the overall movement show walking-like periodicity?
4. Is it a duplicate of an already counted event?
5. Does the candidate fit the current activity context?

Do not hardcode a single minimum interval for all users.

Use a configurable cadence model and validate it against real walking data.

A further improvement is to track alternating gait patterns when the signal makes them observable. Walking often produces a repeating left-right rhythm, although a single phone cannot always identify individual feet reliably.

## 10. How to avoid false steps

This deserves its own subsystem.

Consider five situations:

| Situation              | What can happen          | Our response                             |
| ---------------------- | ------------------------ | ---------------------------------------- |
| Shaking phone          | Repeated peaks           | Check periodicity and context            |
| Typing while sitting   | Small repeated movements | Require convincing walking evidence      |
| Vehicle vibration      | Periodic acceleration    | Examine motion pattern and context       |
| Putting phone on table | Large impact             | Reject isolated transients               |
| Walking while texting  | Weak repeated signal     | Preserve low-amplitude periodic patterns |

There is no perfect accelerometer-only rule that distinguishes every possible vehicle vibration from walking.

That is why we need a labelled test dataset and potentially a lightweight machine-learning classifier.

### Should we use machine learning?

Eventually, yes.

But not immediately.

First build a deterministic baseline.

Then train a model using features such as:

- Signal energy.
- Signal variance.
- Cadence.
- Autocorrelation strength.
- Peak prominence.
- Gyroscope energy.
- Signal regularity.
- Frequency-domain features.

Possible model:

- Random Forest for an initial interpretable baseline.
- Gradient-boosted trees for another comparison.
- Small neural network if the dataset justifies it.

The model could classify:

text

```text
STATIONARY
WALKING
RUNNING
VEHICLE_LIKE_MOTION
UNKNOWN
```

I would keep `UNKNOWN` as a valid result. Forcing every situation into walking or not walking can create false counts.

The classifier should estimate activity, while a separate step detector estimates individual step events.

# 11. Make it work across different Android phones

This is where a lot of apps fail.

A Samsung phone, Vivo phone, Xiaomi phone and OnePlus phone can have:

- Different accelerometer manufacturers.
- Different sensor noise levels.
- Different sensor sampling behaviour.
- Different hardware step-counter implementations.
- Different Android versions.
- Different background execution policies.

We cannot build the entire system around one Samsung device.

Instead, create a device capability and calibration layer.

### Device capability detection

At installation, inspect:

text

```text
Accelerometer available?
Gyroscope available?
Linear acceleration available?
Hardware step counter available?
Hardware step detector available?
Reported sensor resolution?
Reported maximum range?
Minimum supported delay?
```

Use Android's `SensorManager` and `Sensor` metadata.

### Calibration

Do not use calibration to assume that every phone has the same acceleration characteristics.

Instead, maintain:

- Sensor-specific noise estimates.
- Observed sampling intervals.
- Baseline signal characteristics.
- Filter parameters.
- Walking-pattern confidence parameters.

A possible profile:

| Device profile | Hardware counter                    | Custom engine        |
| -------------- | ----------------------------------- | -------------------- |
| Device A       | Available                           | Backup               |
| Device B       | Available                           | Backup               |
| Device C       | Missing                             | Primary              |
| Device D       | Available but unreliable in testing | Evaluate alternative |

These are hypothetical device profiles.

We should never identify a phone model as inaccurate just because it is made by a particular brand.

Our software should adapt to measured sensor behaviour, not brand names.

# 12. Background tracking: Keep counting when the screen is off

This is critical because your original Vivo experience worked without constantly opening the app.

Android has background sensor restrictions.

On Android 9 and later, continuous sensors such as accelerometers and gyroscopes generally cannot deliver events to an ordinary background app. A foreground service is one way to maintain continuous collection where permitted.

Android Developers

+1

I would implement two different tracking modes.

### Mode A: Hardware tracking

- Register the hardware step counter.
- Keep its registration alive.
- Let the low-power sensor hardware accumulate steps.
- Read and persist updated cumulative values.
- Avoid unnecessarily collecting raw accelerometer readings.

This should be our preferred mode.

### Mode B: Custom tracking

- Use an appropriately configured foreground service.
- Collect accelerometer data.
- Process it in batches.
- Save confirmed step events.
- Stop unnecessary sensors when tracking is not required.

The service must comply with the Android version's foreground-service requirements, permissions and battery policies.

Also, Android manufacturers may apply additional battery restrictions.

We must test screen-off behaviour, reboot behaviour and battery-saving modes on actual devices.

# 13. Permissions and Android integration

Our application will need to handle permissions correctly.

For example:

xml

```xml
<uses-permission
    android:name="android.permission.ACTIVITY_RECOGNITION"/>
```

For a suitable foreground health-tracking service, additional manifest declarations and runtime prerequisites may apply depending on Android version.

Don't automatically request every possible permission.

Request only what the selected tracking mode needs.

The important distinction:

- Hardware step counting requires activity recognition permission.
- Raw accelerometer collection has different access requirements.
- Background service execution has separate requirements.

Android's official references explain these differences.

Android Developers

+1

# 14. How do we calculate the daily total?

This is another important engineering detail.

The hardware step counter is cumulative, not a daily counter.

Suppose it reports:

text

```text
Morning:   1,000
Afternoon: 4,000
Evening:   7,000
```

Our application calculates differences:

$$
NewSteps=CurrentTotal-PreviousTotal
$$

So:

text

```text
Morning:   1,000
Afternoon: 3,000 additional
Evening:   3,000 additional

Daily total: 7,000
```

But what happens if the phone reboots?

The sensor's cumulative value resets after reboot.

We must detect that reset and establish a new baseline instead of subtracting the old value and generating negative steps.

We also need to handle:

- App restarts.
- Duplicate sensor events.
- Missing readings.
- Device time changes.
- Midnight boundaries.
- Time-zone changes.
- Database recovery.

I would store sensor checkpoints and confirmed step deltas rather than relying on a single in-memory variable.

### Suggested database structure

| Table            | Information                               |
| ---------------- | ----------------------------------------- |
| DeviceProfile    | Sensor capabilities                       |
| SensorCheckpoint | Last cumulative hardware reading          |
| StepEvents       | Individual confirmed events, if available |
| StepIntervals    | Counts per time interval                  |
| DailySummary     | Daily total                               |
| Calibration      | Algorithm parameters                      |
| TestSessions     | Recorded experimental data                |

For daily summaries, use the user's local calendar date and correctly handle daylight-saving or time-zone changes where applicable.

# 15. Distance and calories

These should be separate calculations, not part of step detection.

### Distance

The simplest estimate:

$$
Distance=Steps\times AverageStepLength
$$

For example:

- Steps: 5,000
- Average step length: 0.70 metres

$$
Distance=3500m=3.5km
$$

But 0.70 metres is only an example.

Eventually, let users enter their height and optionally calibrate their step length using a known walking distance.

### Calories

Calories are substantially harder to estimate.

They depend on:

- Body weight.
- Walking speed.
- Distance.
- Incline.
- Individual physiology.

I would leave calories out of the first version. An inaccurate step counter with an apparently precise calorie figure would be misleading.

# 16. Our actual testing and accuracy strategy

This is how we get closer to Vivo's behaviour.

We need a controlled testing process.

## Phase 1: Build a sensor recorder

Before counting anything, create an internal testing screen.

Developer sensor monitor

Illustrative interfaceAccelerometer

Available

Gyroscope

Available

Hardware counter

Available

Sampling rate

50 Hz target

LIVE SENSOR SIGNAL — EXAMPLE

Illustrative waveform, not a real sensor recording.Detected candidates

0

Confirmed steps

0

Walking confidence

Waiting for data

The recorder should export CSV files so we can investigate the sensor data on a computer.

## Phase 2: Collect labelled datasets

Manually count steps during each experiment.

Record multiple sessions per person and multiple phone placements.

Keep training and testing data separated by person and preferably by device. Otherwise, we risk training the algorithm to recognize one person's movement rather than general walking.

## Phase 3: Compare algorithms

We should implement and compare:

1. Basic peak detection.
2. Filtered peak detection.
3. Adaptive peak detection.
4. Peak detection plus autocorrelation.
5. Feature-based machine-learning classification.

We can then determine which actually performs best.

## Phase 4: Calculate accuracy

Use:

$$
Error=\frac{|Predicted-Actual|}{Actual}\times100
$$

For example:

| Actual | Predicted | Error |
| ------ | --------- | ----- |
| 500    | 490       | 2%    |
| 1,000  | 960       | 4%    |
| 1,000  | 1,040     | 4%    |

But also measure precision and recall for individual step events, where ground-truth timestamps are available.

Why?

Because a system can accidentally produce the correct final total while missing many real steps and counting many false ones.

Our objective is to minimize both missed steps and false steps.

# 17. A potentially useful alternative: Health Connect

There's another Android capability worth investigating.

On supported Android 14+ devices with the required SDK extension, Health Connect can provide on-device step tracking using Android's hardware step counter.

It requires the appropriate step-reading permission, and support must be checked at runtime.

Android Developers

+1

This is worth evaluating as an additional source, particularly for devices where we don't want to maintain our own continuous sensor service.

However, Health Connect is a data integration layer, not a replacement for our custom signal-processing research.

# 18. The development roadmap I would actually follow

Don't attempt all the features simultaneously.

1. Stage 1 — Hardware investigation
   Identify all available sensors on your Samsung. Test hardware counter and detector. Check whether they work with the screen off.
   First priority
2. Stage 2 — Raw sensor recorder
   Build accelerometer and gyroscope collection. Save timestamped readings. Export CSV files.
3. Stage 3 — Custom algorithm V1
   Implement magnitude, gravity removal, filtering, peak detection and basic cadence analysis.
4. Stage 4 — Adaptive algorithm V2
   Add autocorrelation, adaptive thresholds, noise estimation and different phone-position profiles.
5. Stage 5 — Walking classifier V3
   Compare deterministic logic with machine-learning classification.
6. Stage 6 — Hardware comparison
   Compare hardware results and custom results against manually counted walking sessions.
7. Stage 7 — Production tracking
   Implement background tracking, persistent storage, reboot recovery and battery optimization.
8. Stage 8 — Multi-device testing
   Test on Samsung, Vivo, Xiaomi, OnePlus and other available Android devices.

# 19. What I would build first, concretely

I would start with a project called `PocketSteps`.

Not a polished fitness app. An engineering prototype.

The first version would have just four screens:

1. Sensor Inspector: Shows which hardware sensors are available.
2. Live Recorder: Shows raw X/Y/Z data and records sessions.
3. Algorithm Lab: Shows filtered signals, candidate peaks, cadence and walking confidence.
4. Comparison Lab: Shows actual manually counted steps versus hardware and custom algorithm results.

This is much more valuable than starting with a dashboard and a 10,000-step goal.

## 20. Three important decisions before implementation

I want to establish these because they'll affect our architecture.

1. Which phone are we developing and testing on?

Samsung Galaxy S series

Samsung Galaxy A series Samsung Galaxy A series

Samsung Galaxy M series

Other Samsung model

1. What's your development experience?

I can use Android Studio and Kotlin I can use Android Studio and Kotlin

I know basic programming

I'm a complete beginner

1. What should be our main engineering priority?

Build our own algorithm and compare it with hardware Develop our own algorithm and compare it with hardware

Maximum accuracy using all available phone technology

Cross-brand compatibility

Start practical development

My final engineering advice: Don't try to guess the perfect algorithm on day one. Build a sensor recorder, collect real walking data, understand the signals, implement a baseline, measure its mistakes, and improve it systematically.

That's how we have a realistic chance of getting close to the Vivo experience across different phones.
