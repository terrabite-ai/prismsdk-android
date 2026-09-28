# Prism SDK for Android

Background location tracking for Android apps. Add Prism, ask for permission
once, and receive your users' locations in the foreground and the background,
with the battery cost you choose.

Requires Android 7.0 (API 24) or later. Kotlin and Java.

## Install

<!-- Remove this block when 0.2.0 is released. -->
> **0.2.0 is not released yet.** It is on the `release/0.2.0` branch for
> testing and is not on Maven Central. Until it is, install it as shown under
> [Testing 0.2.0 before release](#testing-020-before-release). The latest
> released version is 0.1.0, which has no Places API.

```kotlin
dependencies {
    implementation("ai.terrabite:prismsdk:0.2.0")
}
```

That is the whole install. Prism is on Maven Central, which every Android
project already resolves from, and its own dependencies come with it,
including place inference (`ai.terrabite:prism-enrich`).

If you need the library before a version reaches Maven Central, each
[release](https://github.com/terrabite-ai/prismsdk-android/releases) also
carries the AAR. Put it in your app module's `libs/` folder and declare it
together with what it links:

```kotlin
dependencies {
    implementation(files("libs/prismsdk-0.2.0.aar"))
    implementation("com.localsdk:core:1.0.0")
    implementation("ai.terrabite:prism-enrich:0.0.1-beta")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
```

<!-- Remove this section when 0.2.0 is released. -->
### Testing 0.2.0 before release

Build the branch into your local Maven repository:

```
git clone -b release/0.2.0 https://github.com/terrabite-ai/prismsdk-android
cd prismsdk-android
echo "sdk.dir=/path/to/Android/sdk" > local.properties
./gradlew :sdk:publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false
```

Then let your app resolve from it. In the app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

and depend on `ai.terrabite:prismsdk:0.2.0` as above. Place inference comes
from Maven Central as `ai.terrabite:prism-enrich:0.0.1-beta`; nothing to build
for it.

After the branch is updated, pull and publish to local Maven again. The API
can still change before the release.

## Set up your app

Tracking runs in a foreground service, which Android requires to show a
persistent notification. Prism's defaults give it a title and body; set your
own, and an icon, through `PrismConfig`.

The engine's manifest declares the location and foreground-service permissions
and they merge into your app. You still request the runtime permissions from
the user, below.

## Quick start

### Kotlin

```kotlin
import ai.terrabite.prism.Prism
import ai.terrabite.prism.PrismLocation

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prism.initialize(applicationContext, "your-sdk-key")
        Prism.setLocationListener { location ->
            Log.d("Prism", "${location.latitude}, ${location.longitude}")
        }
        Prism.setErrorListener { error -> Log.w("Prism", error) }

        if (Prism.checkLocationPermission()) {
            Prism.startTracking()
        } else {
            Prism.requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Prism.LOCATION_PERMISSION_REQUEST_CODE && Prism.checkLocationPermission()) {
            Prism.startTracking()
        }
    }
}
```

Listeners are called on the engine's thread, not the main thread. Hop before
touching UI.

If you prefer a `Flow`:

```kotlin
lifecycleScope.launch {
    Prism.locations().collect { location ->
        Log.d("Prism", "${location.latitude}, ${location.longitude}")
    }
}
```

### Java

```java
import ai.terrabite.prism.Prism;
import ai.terrabite.prism.PrismConfig;

Prism.initialize(getApplicationContext(), "your-sdk-key");
Prism.setLocationListener(location ->
        Log.d("Prism", location.getLatitude() + ", " + location.getLongitude()));
Prism.setErrorListener(error -> Log.w("Prism", error));
Prism.requestLocationPermission(this);
// then, in onRequestPermissionsResult, when Prism.checkLocationPermission() is true:
Prism.startTracking();
```

## Configure

Optional. The defaults are sensible.

```kotlin
Prism.setConfig(
    PrismConfig(
        trackingMode = PrismTrackingMode.STANDARD,   // PRECISE, STANDARD or EFFICIENT
        horizontalAccuracyThreshold = 50,            // discard fixes worse than 50 m
        notificationTitle = "Delivering",
        notificationBody = "Your route is being recorded",
        notificationIcon = R.drawable.ic_stat_prism,
    )
)
```

From Java, use `new PrismConfig.Builder()…build()`.

`PRECISE` updates most often and costs the most battery. `EFFICIENT` is the
opposite. `STANDARD` is the usual choice.

## Background permission

Android grants background location as a separate step. Ask for foreground
first, start tracking, then ask to extend it:

```kotlin
Prism.requestBackgroundLocationPermission(this)
// answer arrives in onRequestPermissionsResult with Prism.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
```

`Prism.locationPermissionStatus(context)` tells you the current state without
prompting: not granted, when in use, or always, and whether the grant is
precise.

Some manufacturers stop background work unless the app is excluded from
battery optimisation. `Prism.disableBatteryOptimizations(context)` takes the
user to the right screen.

## Identify the user

```kotlin
Prism.setUserId("user-123")
Prism.setMetadata(mapOf("plan" to "gold"))
```

Both are attached to every location that follows.

## Places

New in 0.2.0. Prism can infer where the user lives and which places they
return to, from the locations it already delivers. Everything runs on the
device; Prism never sends places anywhere. It is off by default.

### Turn it on

Add an `enrich` block to the configuration:

```kotlin
Prism.initialize(applicationContext, "your-sdk-key")
Prism.setConfig(
    PrismConfig(
        enrich = PrismEnrichConfig(retention = PrismPlaceRetention.THREE_MONTHS),
    )
)
Prism.startTracking()
```

- Places are built from the locations Prism delivers, so they only grow
  while tracking is running.
- Include the `enrich` block **every time** you call `setConfig` or
  `startTracking(config)`. A configuration without it turns places off.
- The choice is remembered across launches.

Retention is how much history the result rests on. Older stays are forgotten.

| Retention | Use it when |
|---|---|
| `ONE_MONTH` | The user's routine changes often, or you want to keep the least |
| `THREE_MONTHS` | Default |
| `SIX_MONTHS` | You want the steadiest result |

Changing retention takes effect straight away; shortening it drops the older
history.

### Read the places

```kotlin
val places = Prism.places()          // synchronous, any thread, never null

places.home?.let { home ->
    Log.d("Prism", "home near ${home.latitude}, ${home.longitude} (${home.confidence})")
}
places.frequent.forEach {
    Log.d("Prism", "frequent: ${it.latitude}, ${it.longitude}, ${it.visitCount} visits")
}
```

Before places are turned on, and until the first stay has been observed,
`home` is null and `frequent` is empty.

### Follow changes

As a `Flow`:

```kotlin
lifecycleScope.launch {
    Prism.placesUpdates().collect { places ->
        // the latest value first, then each change
    }
}
```

Or with a listener:

```kotlin
Prism.setPlacesListener { places -> /* background thread */ }
Prism.setPlacesListener(null)        // stop
```

An update arrives when a place appears or disappears or when its counts
change, not on every location. Results also refresh when the app returns to
the foreground, and from time to time in the background.

- The flow emits nothing until places have been turned on.
- The listener receives changes from the moment it is set. Read
  `Prism.places()` for the value at that moment.
- The listener is called on a background thread. Hop before touching UI.

### Turn it off, delete

```kotlin
Prism.setConfig(PrismConfig())   // off. What was learned is kept.
Prism.clearPlaces()              // deletes everything. Stays on if it was on.
```

Give your users a way to reach `clearPlaces()`.

### What you get

`PrismPlaces`:

| Field | Type | Meaning |
|---|---|---|
| `home` | `PrismPlace?` | Where the user spends their nights, or null |
| `frequent` | `List<PrismPlace>` | Other places they return to, most time spent first |
| `computedAtMs` | `Long` | When this result was computed, epoch milliseconds. 0 before the first computation |
| `retention` | `PrismPlaceRetention` | The retention in force |
| `stayCount` | `Int` | Stays inside the retention window |
| `observedFromMs` | `Long?` | Earliest stay inside the window. Tells you how much history the result rests on |

`PrismPlace`:

| Field | Type | Meaning |
|---|---|---|
| `kind` | `PrismPlaceKind` | `HOME` or `FREQUENT`. More kinds may be added, so give every `when` an `else` |
| `latitude`, `longitude` | `Double` | Centre of the stays that make up the place |
| `visitCount` | `Int` | Separate stays observed here |
| `totalDwellMs` | `Long` | Total time spent here, milliseconds |
| `distinctNights` | `Int` | Distinct nights spent here |
| `firstSeenMs`, `lastSeenMs` | `Long` | First and latest stay, epoch milliseconds |
| `confidence` | `PrismConfidence?` | Set for home only |

Confidence says how much evidence stands behind the home label. It grows with
the number of distinct nights: `PROVISIONAL`, `LOW`, `MODERATE`, `HIGH`,
`CONFIRMED`. A home appears early and starts as `PROVISIONAL`; treat the first
two levels as a guess and decide in your app which level is good enough to
show.

### As a map

`places.toMap()` and `place.toMap()` return plain maps for bridged hosts such
as React Native. The keys are snake_case and identical to the iOS SDK's JSON.

```json
{
  "home": {
    "kind": "HOME", "latitude": 52.2297, "longitude": 21.0122,
    "visit_count": 58, "total_dwell_ms": 2217600000, "distinct_nights": 44,
    "first_seen_ms": 1751328000000, "last_seen_ms": 1758931200000,
    "confidence": "CONFIRMED"
  },
  "frequent": [
    {
      "kind": "FREQUENT", "latitude": 52.2319, "longitude": 20.9841,
      "visit_count": 31, "total_dwell_ms": 892800000, "distinct_nights": 0,
      "first_seen_ms": 1751360400000, "last_seen_ms": 1758880800000,
      "confidence": null
    }
  ],
  "computed_at_ms": 1758931500000,
  "retention": "THREE_MONTHS",
  "stay_count": 164,
  "observed_from_ms": 1751328000000
}
```

Every key is always present; `home`, `confidence` and `observed_from_ms` are
null when they have no value.

### Java

```java
Prism.setConfig(new PrismConfig.Builder()
        .setEnrich(new PrismEnrichConfig.Builder()
                .setRetention(PrismPlaceRetention.THREE_MONTHS)
                .build())
        .build());

PrismPlaces places = Prism.places();                 // never null
PrismPlace home = places.getHome();                  // may be null
if (home != null) {
    Log.d("Prism", "home near " + home.getLatitude() + ", " + home.getLongitude()
            + " (" + home.getConfidence() + ")");
}

Prism.setPlacesListener(updated -> { /* background thread */ });
Prism.setPlacesListener(null);                       // stop

Prism.clearPlaces();
```

### Storage and privacy

- Stays and places are kept in the app's private storage, excluded from
  backups, and removed when the app is uninstalled.
- Nothing is sent over the network. Place inference has no network code.
- The background refresh runs through WorkManager. If your app has removed
  WorkManager's default initializer and supplies no configuration of its own,
  that refresh is skipped and places update while the app is in use.
- Inferring a home address is sensitive. Ask for consent and disclose it in
  your privacy policy and Play Data safety form. Prism only computes; your app
  decides what to do with the result.

## API summary

Everything is a static call on `Prism`, from Kotlin and Java alike.

| Call | What it does |
|---|---|
| `initialize(context, apiKey)` | Set up the SDK. Call first, once per launch |
| `setConfig(config)` | Apply a `PrismConfig`, including the `enrich` block for places |
| `startTracking()`, `startTracking(config)` | Start delivering locations |
| `stopTracking()`, `isTracking()` | Stop, and ask whether tracking is running |
| `requestLocationPermission(activity)`, `requestBackgroundLocationPermission(activity)` | Prompt the user |
| `checkLocationPermission()`, `checkBackgroundLocationPermission()`, `locationPermissionStatus(context)` | Read permission state without prompting |
| `setLocationListener(listener)`, `setErrorListener(listener)` | Receive locations and errors by listener |
| `locations()`, `errors()` | The same as `Flow`s |
| `places()` | **New in 0.2.0.** The current home and frequent places |
| `placesUpdates()` | **New in 0.2.0.** Places as a `Flow` |
| `setPlacesListener(listener)` | **New in 0.2.0.** Places by listener |
| `clearPlaces()` | **New in 0.2.0.** Delete every stored stay and place |
| `setUserId(userId)`, `setMetadata(metadata)`, `getDeviceId()` | Identify the user and the device |
| `disableBatteryOptimizations(context)`, `isBatteryOptimizationDisabled(context)` | Battery optimisation exemption |

## Development

```
./gradlew :sdk:testDebugUnitTest    # unit tests
./gradlew :sdk:assembleRelease      # AAR
./gradlew :sdk:publishToMavenLocal  # ai.terrabite:prismsdk in ~/.m2
```

Needs a `local.properties` with `sdk.dir=` pointing at an Android SDK.

## License

MIT. See [LICENSE](LICENSE).
