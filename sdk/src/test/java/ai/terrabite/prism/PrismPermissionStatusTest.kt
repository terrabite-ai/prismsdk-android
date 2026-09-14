package ai.terrabite.prism

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrismPermissionStatusTest {

    private val app: Application get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `nothing granted`() {
        val s = PrismPermissionStatus.of(app)
        assertEquals(PrismPermissionStatus.Level.NOT_GRANTED, s.level)
        assertEquals(false, s.isGranted)
        assertEquals(false, s.isBackgroundGranted)
        assertEquals(false, s.isPrecise)
    }

    @Test
    fun `coarse only is when-in-use and not precise`() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        val s = PrismPermissionStatus.of(app)
        assertEquals(PrismPermissionStatus.Level.WHEN_IN_USE, s.level)
        assertEquals(true, s.isGranted)
        assertEquals(false, s.isPrecise)
    }

    @Test
    fun `fine is when-in-use and precise`() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        val s = PrismPermissionStatus.of(app)
        assertEquals(PrismPermissionStatus.Level.WHEN_IN_USE, s.level)
        assertEquals(true, s.isPrecise)
        assertEquals(false, s.isBackgroundGranted)
    }

    @Test
    fun `fine plus background is always`() {
        shadowOf(app).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
        val s = PrismPermissionStatus.of(app)
        assertEquals(PrismPermissionStatus.Level.ALWAYS, s.level)
        assertEquals(true, s.isBackgroundGranted)
        assertEquals(true, s.isPrecise)
    }

    @Test
    fun `background without foreground is not granted`() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        assertEquals(PrismPermissionStatus.Level.NOT_GRANTED, PrismPermissionStatus.of(app).level)
    }
}
