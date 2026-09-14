package ai.terrabite.prism

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [PrismLocation] and [PrismLocation.toMap] are two hand-written copies of the
 * engine's 27 fields. These pin each against the engine value it came from.
 */
class PrismLocationMappingTest {

    @Test
    fun `every engine field reaches PrismLocation unchanged`() {
        val l = PrismLocation.from(LocationFixtures.full)

        assertEquals("loc-0001", l.id)
        assertEquals(1_757_000_000_123L, l.timestamp)
        assertEquals("+02:00", l.timezoneOffset)
        assertEquals(52.2297, l.latitude, 0.0)
        assertEquals(21.0122, l.longitude, 0.0)
        assertEquals(88.5, l.altitude, 0.0)
        assertEquals(12.25f, l.horizontalAccuracy)
        assertEquals(4.5f, l.verticalAccuracy)
        assertEquals(3.5f, l.speed)
        assertEquals(271.75f, l.bearing)
        assertEquals(PrismLocationType.VISIT, l.type)
        assertEquals(true, l.isMock)
        assertEquals("device-abc", l.deviceId)
        assertEquals("user-xyz", l.userId)
        assertEquals(mapOf("plan" to "gold", "region" to "eu"), l.metadata)
        assertEquals(67, l.batteryLevel)
        assertEquals("CHARGING", l.batteryStatus)
        assertEquals(true, l.networkStatus)
        assertEquals(true, l.locationPermission)
        assertEquals(PrismTrackingMode.EFFICIENT, l.trackingMode)
        assertEquals("Fruit", l.brand)
        assertEquals("Pixel 9", l.model)
        assertEquals("Android", l.os)
        assertEquals("15", l.osVersion)
        assertEquals("1.0.0", l.sdkVersion)
        assertEquals("2.3.4", l.appVersionName)
        assertEquals(567L, l.appVersionCode)
    }

    /** If the engine model grows, this fails first, before a field is silently dropped. */
    @Test
    fun `PrismLocation carries exactly the engine's 27 fields`() {
        val engineFields = com.localsdk.modules.locationtracking.models.Location::class.java.declaredFields
            .count { !it.isSynthetic && !java.lang.reflect.Modifier.isStatic(it.modifiers) }
        val prismFields = PrismLocation::class.java.declaredFields
            .count { !it.isSynthetic && !java.lang.reflect.Modifier.isStatic(it.modifiers) }

        assertEquals(27, engineFields)
        assertEquals(27, prismFields)
    }

    /** The keys are wire names, so they are pinned by name. */
    @Test
    fun `toMap uses wire names and carries every field`() {
        val m = PrismLocation.from(LocationFixtures.full).toMap()

        assertEquals(27, m.size)
        assertEquals("loc-0001", m["id"])
        assertEquals(1_757_000_000_123L, m["timestamp"])
        assertEquals("+02:00", m["tz_offset"])
        assertEquals(52.2297, m["latitude"])
        assertEquals(21.0122, m["longitude"])
        assertEquals(88.5, m["altitude"])
        assertEquals(12.25f, m["horizontal_accuracy"])
        assertEquals(4.5f, m["vertical_accuracy"])
        assertEquals(3.5f, m["speed"])
        assertEquals(271.75f, m["bearing"])
        assertEquals("VISIT", m["type"])
        assertEquals(true, m["is_mock"])
        assertEquals("device-abc", m["device_id"])
        assertEquals("user-xyz", m["user_id"])
        assertEquals(mapOf("plan" to "gold", "region" to "eu"), m["metadata"])
        assertEquals(67, m["battery_level"])
        assertEquals("CHARGING", m["battery_status"])
        assertEquals(true, m["network_status"])
        assertEquals(true, m["location_permission"])
        assertEquals("EFFICIENT", m["tracking_mode"])
        assertEquals("Fruit", m["brand"])
        assertEquals("Pixel 9", m["model"])
        assertEquals("Android", m["os"])
        assertEquals("15", m["os_version"])
        assertEquals("1.0.0", m["sdk_version"])
        assertEquals("2.3.4", m["app_version_name"])
        assertEquals(567L, m["app_version_code"])
    }
}
