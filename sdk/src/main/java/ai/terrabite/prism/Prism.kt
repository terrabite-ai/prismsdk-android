package ai.terrabite.prism

import android.app.Activity
import android.content.Context
import com.localsdk.LocalSDK
import kotlinx.coroutines.flow.Flow

/**
 * Prism SDK — background location tracking for Android.
 *
 * ```kotlin
 * Prism.initialize(applicationContext, "your-sdk-key")
 * Prism.setLocationListener { location -> /* ... */ }
 * Prism.requestLocationPermission(activity)   // then, in onRequestPermissionsResult:
 * Prism.startTracking()
 * ```
 *
 * Every value crossing this API is Prism's own — [PrismLocation], [PrismConfig],
 * [PrismPermissionStatus]. The tracking engine underneath is a separate library,
 * and nothing here exposes it.
 *
 * Every member is `@JvmStatic`, so Java calls `Prism.startTracking()` directly.
 */
object Prism {

    /** Request code Prism uses for the foreground location permission prompt. */
    const val LOCATION_PERMISSION_REQUEST_CODE = 1000

    /** Request code Prism uses for the background location permission prompt. */
    const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1001

    init {
        // The engine holds exactly one location listener and one error listener,
        // and replaces them on every call. Prism takes both slots once, here, and
        // fans out to its own listeners and flows from PrismDispatcher.
        LocalSDK.onLocation { location -> PrismDispatcher.deliver(PrismLocation.from(location)) }
        LocalSDK.onError { error -> PrismDispatcher.deliverError(error) }
    }

    // MARK: Lifecycle

    /**
     * Start the SDK. Required before anything else.
     *
     * Pass the application context — it is held for the life of the process.
     */
    @JvmStatic
    fun initialize(context: Context, apiKey: String) {
        LocalSDK.initialize(context.applicationContext, apiKey)
    }

    /** Apply a configuration. Optional; the defaults are sensible. */
    @JvmStatic
    fun setConfig(config: PrismConfig) {
        LocalSDK.setConfig(config.toEngine())
    }

    /**
     * Start tracking. Fails through [PrismErrorListener] rather than throwing if
     * foreground location permission has not been granted.
     */
    @JvmStatic
    fun startTracking() {
        LocalSDK.startTracking()
    }

    /** Apply a configuration and start tracking, in one call. */
    @JvmStatic
    fun startTracking(config: PrismConfig) {
        setConfig(config)
        startTracking()
    }

    @JvmStatic
    fun stopTracking() {
        LocalSDK.stopTracking()
    }

    @JvmStatic
    fun isTracking(): Boolean = LocalSDK.isTracking()

    // MARK: Permissions

    /** Whether foreground location (fine or coarse) is granted, without prompting. */
    @JvmStatic
    fun checkLocationPermission(): Boolean = LocalSDK.checkLocationPermission()

    /** Whether background location is granted, without prompting. */
    @JvmStatic
    fun checkBackgroundLocationPermission(): Boolean = LocalSDK.checkBackgroundLocationPermission()

    /** The current state in full, without prompting. */
    @JvmStatic
    fun locationPermissionStatus(context: Context): PrismPermissionStatus =
        PrismPermissionStatus.of(context)

    /**
     * Ask for foreground location. Presents the system prompt through [activity];
     * the answer arrives in the activity's `onRequestPermissionsResult` with
     * [LOCATION_PERMISSION_REQUEST_CODE].
     */
    @JvmStatic
    fun requestLocationPermission(activity: Activity) {
        LocalSDK.requestLocationPermission(activity)
    }

    /**
     * Ask to extend the grant to the background. On Android 10 and later this
     * prompts for `ACCESS_BACKGROUND_LOCATION`; on Android 11 and later the
     * system sends the user to Settings. Ask for foreground first. The answer
     * arrives with [BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE].
     */
    @JvmStatic
    fun requestBackgroundLocationPermission(activity: Activity) {
        LocalSDK.requestBackgroundLocationPermission(activity)
    }

    // MARK: Receiving locations

    /**
     * Set the receiver for locations. Pass `null` to clear. One at a time; a
     * second call replaces the first.
     *
     * Called on the engine's own thread, not the main thread — hop before
     * touching UI.
     */
    @JvmStatic
    fun setLocationListener(listener: PrismLocationListener?) {
        PrismDispatcher.locationListener = listener
    }

    /** Set the receiver for errors. Pass `null` to clear. Same thread rule. */
    @JvmStatic
    fun setErrorListener(listener: PrismErrorListener?) {
        PrismDispatcher.errorListener = listener
    }

    /**
     * Locations as a [Flow]. Every collector receives every location produced
     * while it is collecting; a collector that falls behind gets the newest value
     * and drops older ones rather than building a queue.
     */
    @JvmStatic
    fun locations(): Flow<PrismLocation> = PrismDispatcher.locations

    /** Errors as a [Flow]. Same semantics as [locations]. */
    @JvmStatic
    fun errors(): Flow<String> = PrismDispatcher.errors

    // MARK: Identity and context

    /** The SDK's own identifier for this install. */
    @JvmStatic
    fun getDeviceId(): String = LocalSDK.getDeviceId()

    /** Attach your own user identifier to every subsequent location. */
    @JvmStatic
    fun setUserId(userId: String) {
        LocalSDK.setUserId(userId)
    }

    /** Attach arbitrary key-value context to every subsequent location. */
    @JvmStatic
    fun setMetadata(metadata: Map<String, String>) {
        LocalSDK.setMetadata(metadata)
    }

    // MARK: Battery

    /**
     * Take the user to the system screen where the app can be excluded from
     * battery optimisation. Without that exclusion some manufacturers stop
     * background tracking after a few minutes.
     */
    @JvmStatic
    fun disableBatteryOptimizations(context: Context) {
        LocalSDK.disableBatteryOptimizations(context)
    }

    @JvmStatic
    fun isBatteryOptimizationDisabled(context: Context): Boolean =
        LocalSDK.isBatteryOptimizationDisabled(context)
}
