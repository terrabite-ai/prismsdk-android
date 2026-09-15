# Prism SDK for Android

Background location tracking for Android apps. Add Prism, ask for permission
once, and receive your users' locations in the foreground and the background,
with the battery cost you choose.

Requires Android 7.0 (API 24) or later. Kotlin and Java.

## Install

```kotlin
dependencies {
    implementation("ai.terrabite:prismsdk:0.1.0")
}
```

That is the whole install. Prism is on Maven Central, which every Android
project already resolves from, and its own dependencies come with it.

If you need the library before a version reaches Maven Central, each
[release](https://github.com/terrabite-ai/prismsdk-android/releases) also
carries the AAR. Put it in your app module's `libs/` folder and declare it
together with what it links:

```kotlin
dependencies {
    implementation(files("libs/prismsdk-0.1.0.aar"))
    implementation("com.localsdk:core:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
```

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

## Development

```
./gradlew :sdk:testDebugUnitTest    # unit tests
./gradlew :sdk:assembleRelease      # AAR
./gradlew :sdk:publishToMavenLocal  # ai.terrabite:prismsdk in ~/.m2
```

Needs a `local.properties` with `sdk.dir=` pointing at an Android SDK.

## License

MIT. See [LICENSE](LICENSE).
