package ai.terrabite.prism

import android.app.NotificationManager
import com.localsdk.config.Config
import com.localsdk.modules.locationtracking.enums.TrackingMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * The engine's `Config` is one global object, so every test here leaves it as
 * Prism's defaults would, to keep tests order-independent.
 */
class PrismConfigMappingTest {

    @After
    fun restoreDefaults() {
        PrismConfig().toEngine()
    }

    /** Values taken from the engine's Config.kt at 1.0.0. */
    @Test
    fun `defaults are the engine's defaults`() {
        val c = PrismConfig()
        assertEquals(PrismTrackingMode.PRECISE, c.trackingMode)
        assertEquals(false, c.allowMockLocation)
        assertEquals(200, c.horizontalAccuracyThreshold)
        assertEquals(true, c.foregroundServiceEnabled)
        assertEquals("Location Tracking", c.notificationTitle)
        assertEquals("Your location is being tracked", c.notificationBody)
        assertEquals(0, c.notificationIcon)
        assertEquals(NotificationManager.IMPORTANCE_LOW, c.notificationImportance)
        assertEquals(false, c.notificationSound)
        assertEquals(false, c.notificationVibration)
    }

    @Test
    fun `every field reaches the engine config`() {
        PrismConfig(
            trackingMode = PrismTrackingMode.EFFICIENT,
            allowMockLocation = true,
            horizontalAccuracyThreshold = 35,
            foregroundServiceEnabled = false,
            notificationTitle = "Delivering",
            notificationBody = "Route in progress",
            notificationIcon = 42,
            notificationImportance = NotificationManager.IMPORTANCE_HIGH,
            notificationSound = true,
            notificationVibration = true,
        ).toEngine()

        assertEquals(TrackingMode.EFFICIENT, Config.getTrackingMode())
        assertEquals(true, Config.getAllowMockLocation())
        assertEquals(35, Config.getAccuracy())
        assertEquals(false, Config.isForegroundServiceEnabled())
        assertEquals("Delivering", Config.getForegroundServiceNotificationTitle())
        assertEquals("Route in progress", Config.getForegroundServiceNotificationBody())
        assertEquals(42, Config.getForegroundServiceNotificationIcon())
        assertEquals(NotificationManager.IMPORTANCE_HIGH, Config.getNotificationImportance())
        assertEquals(true, Config.getNotificationSound())
        assertEquals(true, Config.getNotificationVibration())
    }

    /** Prism is tracking-only. This is the test that keeps it so. */
    @Test
    fun `publishing is always off`() {
        PrismConfig().toEngine()
        assertFalse(Config.isPublishEnabled())
        PrismConfig(trackingMode = PrismTrackingMode.STANDARD).toEngine()
        assertFalse(Config.isPublishEnabled())
    }

    @Test
    fun `builder produces the same value as the constructor`() {
        val built = PrismConfig.Builder()
            .setTrackingMode(PrismTrackingMode.STANDARD)
            .setAllowMockLocation(true)
            .setHorizontalAccuracyThreshold(50)
            .setForegroundServiceEnabled(false)
            .setNotificationTitle("T")
            .setNotificationBody("B")
            .setNotificationIcon(7)
            .setNotificationImportance(NotificationManager.IMPORTANCE_DEFAULT)
            .setNotificationSound(true)
            .setNotificationVibration(true)
            .build()

        val direct = PrismConfig(
            trackingMode = PrismTrackingMode.STANDARD,
            allowMockLocation = true,
            horizontalAccuracyThreshold = 50,
            foregroundServiceEnabled = false,
            notificationTitle = "T",
            notificationBody = "B",
            notificationIcon = 7,
            notificationImportance = NotificationManager.IMPORTANCE_DEFAULT,
            notificationSound = true,
            notificationVibration = true,
        )

        assertEquals(direct, built)
    }
}
