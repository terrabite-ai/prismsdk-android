package ai.terrabite.prism

import com.localsdk.modules.locationtracking.enums.LocationType
import com.localsdk.modules.locationtracking.enums.TrackingMode
import com.localsdk.modules.locationtracking.models.Location

/**
 * Engine values the mapping tests start from. Every field is distinct, so a
 * mapping that copies the wrong field, or forgets one, changes an expected
 * value rather than passing by coincidence.
 */
object LocationFixtures {

    /** The same fixture already mapped, for the Java test, which cannot call the synthetic mapper. */
    @JvmStatic
    fun prism(): PrismLocation = PrismLocation.from(full)

    val full = Location(
        id = "loc-0001",
        timestamp = 1_757_000_000_123L,
        isMock = true,
        speed = 3.5f,
        horizontalAccuracy = 12.25f,
        altitude = 88.5,
        bearing = 271.75f,
        latitude = 52.2297,
        longitude = 21.0122,
        type = LocationType.VISIT,
        batteryLevel = 67,
        deviceId = "device-abc",
        userId = "user-xyz",
        timezoneOffset = "+02:00",
        verticalAccuracy = 4.5f,
        networkStatus = true,
        trackingMode = TrackingMode.EFFICIENT,
        batteryStatus = "CHARGING",
        locationPermission = true,
        brand = "Fruit",
        model = "Pixel 9",
        os = "Android",
        osVersion = "15",
        sdkVersion = "1.0.0",
        appVersionName = "2.3.4",
        appVersionCode = 567L,
        metadata = mapOf("plan" to "gold", "region" to "eu"),
    )
}
