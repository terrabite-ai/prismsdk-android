package ai.terrabite.prism

import android.content.Context
import ai.terrabite.prism.enrich.EnrichConfig
import ai.terrabite.prism.enrich.EnrichFix
import ai.terrabite.prism.enrich.EnrichListener
import ai.terrabite.prism.enrich.EnrichObservation
import ai.terrabite.prism.enrich.EnrichPlaces
import ai.terrabite.prism.enrich.EnrichRetention

/** Records every call and lets a test push results, so the bridge is tested without the binary. */
class FakePlaceEngine : PlaceEngine {
    val ingested = ArrayList<EnrichFix>()
    val started = ArrayList<EnrichConfig>()
    var stops = 0; var catchUps = 0; var flushes = 0; var clears = 0; var attached = 0
    var enabled = false
    var current: EnrichPlaces = EnrichPlaces.empty(EnrichRetention.THREE_MONTHS, 0)
    private val listeners = ArrayList<EnrichListener>()

    override fun attach(context: Context) { attached++ }
    override fun isEnabled() = enabled
    override fun start(config: EnrichConfig) { started.add(config); enabled = true }
    override fun stop() { stops++; enabled = false }
    override fun ingest(fix: EnrichFix) { ingested.add(fix) }
    override fun catchUp() { catchUps++ }
    override fun flush() { flushes++ }
    override fun places() = current
    override fun observe(listener: EnrichListener): EnrichObservation {
        listeners.add(listener); listener.onPlaces(current)
        return object : EnrichObservation { override fun cancel() { listeners.remove(listener) } }
    }
    override fun clear() { clears++; enabled = false; current = EnrichPlaces.empty(current.retention, 0); push(current) }
    fun push(p: EnrichPlaces) { current = p; listeners.toList().forEach { it.onPlaces(p) } }
}
