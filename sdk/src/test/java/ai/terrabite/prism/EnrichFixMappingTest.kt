package ai.terrabite.prism

import ai.terrabite.prism.enrich.EnrichFixKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

class EnrichFixMappingTest {
    @Test
    fun mapsTheFieldsEnrichNeeds() {
        val l = LocationFixtures.prism()
        val f = l.toEnrichFix()
        assertEquals(l.latitude, f.latitude, 0.0)
        assertEquals(l.longitude, f.longitude, 0.0)
        assertEquals(l.timestamp, f.timestampMs)
        assertEquals(l.horizontalAccuracy.toDouble(), f.horizontalAccuracyMeters, 0.0)
        assertEquals(l.speed.toDouble(), f.speedMps, 0.0)
        assertEquals(EnrichFixKind.valueOf(l.type.name), f.kind)
        assertNull(f.arrivalMs)
        assertNull(f.departureMs)
        assertEquals("offset from the device zone at the fix instant, not the engine string",
            TimeZone.getDefault().getOffset(l.timestamp) / 60_000, f.tzOffsetMinutes)
    }
}
