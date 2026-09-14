package ai.terrabite.prism

import com.localsdk.modules.locationtracking.enums.LocationType
import com.localsdk.modules.locationtracking.enums.TrackingMode
import org.junit.Assert.assertEquals
import org.junit.Test

class PrismEnumMappingTest {

    @Test
    fun `tracking mode maps case for case in both directions`() {
        for (engine in TrackingMode.entries) {
            val prism = PrismTrackingMode.from(engine)
            assertEquals(engine.name, prism.name)
            assertEquals(engine, prism.toEngine())
        }
        assertEquals(TrackingMode.entries.size, PrismTrackingMode.entries.size)
    }

    @Test
    fun `location type maps case for case`() {
        for (engine in LocationType.entries) {
            assertEquals(engine.name, PrismLocationType.from(engine).name)
        }
        assertEquals(LocationType.entries.size, PrismLocationType.entries.size)
    }
}
