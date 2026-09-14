package ai.terrabite.prism

import com.localsdk.modules.locationtracking.models.Location

/**
 * A single location produced by the SDK.
 *
 * Delivered through [PrismLocationListener] or [Prism.locations]. Values only —
 * the SDK produces these; you do not construct them. `@ConsistentCopyVisibility`
 * makes the generated `copy()` as internal as the constructor, so that holds.
 */
@ConsistentCopyVisibility
data class PrismLocation internal constructor(

    /** Unique identifier for this location. */
    val id: String,

    /** Epoch milliseconds, UTC — the instant the position was fixed. */
    val timestamp: Long,

    /** UTC offset in force at [timestamp], as `±HH:MM`. */
    val timezoneOffset: String,

    val latitude: Double,
    val longitude: Double,
    val altitude: Double,

    /** Radius of uncertainty in metres. */
    val horizontalAccuracy: Float,
    val verticalAccuracy: Float,

    /** Metres per second. `0` means either stationary or unknown. */
    val speed: Float,

    /** Degrees clockwise from true north. `0` when unknown. */
    val bearing: Float,

    val type: PrismLocationType,

    /** `true` when the platform reported the location as simulated. */
    val isMock: Boolean,

    /** The SDK's own identifier for this install. */
    val deviceId: String,

    /** Whatever you last passed to [Prism.setUserId]; empty if you have not. */
    val userId: String,

    /** Whatever you last passed to [Prism.setMetadata]; empty if you have not. */
    val metadata: Map<String, String>,

    val batteryLevel: Int,
    val batteryStatus: String,
    val networkStatus: Boolean,
    val locationPermission: Boolean,
    val trackingMode: PrismTrackingMode,
    val brand: String,
    val model: String,
    val os: String,
    val osVersion: String,
    val sdkVersion: String,
    val appVersionName: String,
    val appVersionCode: Long,
) {

    /**
     * This location as plain values keyed by snake_case wire names, for hosts
     * that cannot see Kotlin classes — React Native's bridge, for one. The keys
     * match the columns of an exported CSV.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "timestamp" to timestamp,
        "tz_offset" to timezoneOffset,
        "latitude" to latitude,
        "longitude" to longitude,
        "altitude" to altitude,
        "horizontal_accuracy" to horizontalAccuracy,
        "vertical_accuracy" to verticalAccuracy,
        "speed" to speed,
        "bearing" to bearing,
        "type" to type.name,
        "is_mock" to isMock,
        "device_id" to deviceId,
        "user_id" to userId,
        "metadata" to metadata,
        "battery_level" to batteryLevel,
        "battery_status" to batteryStatus,
        "network_status" to networkStatus,
        "location_permission" to locationPermission,
        "tracking_mode" to trackingMode.name,
        "brand" to brand,
        "model" to model,
        "os" to os,
        "os_version" to osVersion,
        "sdk_version" to sdkVersion,
        "app_version_name" to appVersionName,
        "app_version_code" to appVersionCode,
    )

    internal companion object {

        /**
         * Maps the engine's model onto Prism's. The only place the two meet;
         * everything Prism exposes is declared here.
         */
        @JvmSynthetic
        fun from(location: Location): PrismLocation = PrismLocation(
            id = location.id,
            timestamp = location.timestamp,
            timezoneOffset = location.timezoneOffset,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            horizontalAccuracy = location.horizontalAccuracy,
            verticalAccuracy = location.verticalAccuracy,
            speed = location.speed,
            bearing = location.bearing,
            type = PrismLocationType.from(location.type),
            isMock = location.isMock,
            deviceId = location.deviceId,
            userId = location.userId,
            metadata = location.metadata,
            batteryLevel = location.batteryLevel,
            batteryStatus = location.batteryStatus,
            networkStatus = location.networkStatus,
            locationPermission = location.locationPermission,
            trackingMode = PrismTrackingMode.from(location.trackingMode),
            brand = location.brand,
            model = location.model,
            os = location.os,
            osVersion = location.osVersion,
            sdkVersion = location.sdkVersion,
            appVersionName = location.appVersionName,
            appVersionCode = location.appVersionCode,
        )
    }
}
