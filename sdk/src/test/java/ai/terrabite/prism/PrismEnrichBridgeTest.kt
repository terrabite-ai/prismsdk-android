package ai.terrabite.prism

import androidx.test.core.app.ApplicationProvider
import ai.terrabite.prism.enrich.EnrichConfidence
import ai.terrabite.prism.enrich.EnrichPlace
import ai.terrabite.prism.enrich.EnrichPlaceKind
import ai.terrabite.prism.enrich.EnrichPlaces
import ai.terrabite.prism.enrich.EnrichRetention
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrismEnrichBridgeTest {

    private lateinit var fake: FakePlaceEngine

    @Before
    fun setUp() {
        PrismEnrichBridge.resetForTest()
        fake = FakePlaceEngine()
        PrismEnrichBridge.engine = fake
    }

    @After
    fun tearDown() {
        PrismEnrichBridge.resetForTest()
        PrismDispatcher.enrichSink = null
    }

    private fun enrichPlaces(): EnrichPlaces = EnrichPlaces(
        home = EnrichPlace(EnrichPlaceKind.HOME, 52.2297, 21.0122, 7, 3_600_000L * 70, 7, 1_000L, 2_000L, EnrichConfidence.HIGH),
        frequent = listOf(EnrichPlace(EnrichPlaceKind.FREQUENT, 52.245, 21.04, 5, 3_600_000L * 45, 0, 1_500L, 1_900L, null)),
        computedAtMs = 3_000L, retention = EnrichRetention.SIX_MONTHS, stayCount = 12, clusterCount = 2, observedFromMs = 1_000L,
    )

    @Test
    fun applyingAConfigStartsEnrichAndInstallsTheSink() {
        PrismEnrichBridge.attach(ApplicationProvider.getApplicationContext())
        assertEquals(1, fake.attached)
        assertNull("off by default: no sink", PrismDispatcher.enrichSink)

        PrismEnrichBridge.apply(PrismEnrichConfig(PrismPlaceRetention.ONE_MONTH))
        assertEquals(EnrichRetention.ONE_MONTH, fake.started.single().retention)
        assertNotNull("sink installed", PrismDispatcher.enrichSink)

        PrismDispatcher.deliver(LocationFixtures.prism())
        assertEquals(1, fake.ingested.size)
    }

    @Test
    fun applyingNullStopsButDoesNotClear() {
        PrismEnrichBridge.apply(PrismEnrichConfig())
        PrismEnrichBridge.apply(null)
        assertEquals(1, fake.stops)
        assertEquals(0, fake.clears)
        assertNull(PrismDispatcher.enrichSink)
    }

    @Test
    fun clearRestartsWhenEnabled() {
        PrismEnrichBridge.apply(PrismEnrichConfig(PrismPlaceRetention.SIX_MONTHS))
        PrismEnrichBridge.clear()
        assertEquals(1, fake.clears)
        assertEquals("started again with the same retention", 2, fake.started.size)
        assertEquals(EnrichRetention.SIX_MONTHS, fake.started.last().retention)
    }

    @Test
    fun placesAreMappedFieldForField() {
        fake.current = enrichPlaces()
        val p = PrismEnrichBridge.places()
        val h = p.home!!
        assertEquals(PrismPlaceKind.HOME, h.kind)
        assertEquals(52.2297, h.latitude, 0.0)
        assertEquals(21.0122, h.longitude, 0.0)
        assertEquals(7, h.visitCount)
        assertEquals(3_600_000L * 70, h.totalDwellMs)
        assertEquals(7, h.distinctNights)
        assertEquals(1_000L, h.firstSeenMs)
        assertEquals(2_000L, h.lastSeenMs)
        assertEquals(PrismConfidence.HIGH, h.confidence)
        assertEquals(1, p.frequent.size)
        assertNull(p.frequent[0].confidence)
        assertEquals(PrismPlaceRetention.SIX_MONTHS, p.retention)
        assertEquals(12, p.stayCount)
        assertEquals(1_000L, p.observedFromMs)
        assertEquals(3_000L, p.computedAtMs)
    }

    @Test
    fun flowAndListenerReceivePushedResults() = runTest {
        val seen = ArrayList<PrismPlaces>()
        PrismEnrichBridge.placesListener = PrismPlacesListener { seen.add(it) }
        PrismEnrichBridge.apply(PrismEnrichConfig())
        fake.push(enrichPlaces())
        assertEquals(7, seen.last().home!!.distinctNights)
        assertEquals(7, PrismEnrichBridge.placesUpdates.first().home!!.distinctNights)
    }

    @Test
    fun toMapUsesWireNames() {
        fake.current = enrichPlaces()
        val m = PrismEnrichBridge.places().toMap()
        val home = m["home"] as Map<*, *>
        assertEquals("HOME", home["kind"])
        assertEquals(7, home["distinct_nights"])
        assertEquals("HIGH", home["confidence"])
        assertEquals(setOf("kind", "latitude", "longitude", "visit_count", "total_dwell_ms", "distinct_nights", "first_seen_ms", "last_seen_ms", "confidence"), home.keys)
        assertEquals(setOf("home", "frequent", "computed_at_ms", "retention", "stay_count", "observed_from_ms"), m.keys)
    }

    @Test
    fun placeHasNineFields() {
        assertEquals(9, PrismPlace::class.java.declaredFields.count { !it.isSynthetic && !java.lang.reflect.Modifier.isStatic(it.modifiers) })
    }
}
