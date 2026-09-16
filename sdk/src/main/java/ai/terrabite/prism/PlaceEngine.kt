package ai.terrabite.prism

import android.content.Context
import ai.terrabite.prism.enrich.Enrich
import ai.terrabite.prism.enrich.EnrichConfig
import ai.terrabite.prism.enrich.EnrichFix
import ai.terrabite.prism.enrich.EnrichListener
import ai.terrabite.prism.enrich.EnrichObservation
import ai.terrabite.prism.enrich.EnrichPlaces

/**
 * The seam between Prism and the Enrich binary. `Enrich.*` is called only from
 * [LiveEnrich]; tests install a fake on [PrismEnrichBridge.engine].
 */
internal interface PlaceEngine {
    fun attach(context: Context)
    fun isEnabled(): Boolean
    fun start(config: EnrichConfig)
    fun stop()
    fun ingest(fix: EnrichFix)
    fun catchUp()
    fun flush()
    fun places(): EnrichPlaces
    fun observe(listener: EnrichListener): EnrichObservation
    fun clear()
}

internal object LiveEnrich : PlaceEngine {
    override fun attach(context: Context) = Enrich.attach(context)
    override fun isEnabled() = Enrich.isEnabled()
    override fun start(config: EnrichConfig) = Enrich.start(config)
    override fun stop() = Enrich.stop()
    override fun ingest(fix: EnrichFix) = Enrich.ingest(fix)
    override fun catchUp() = Enrich.catchUp()
    override fun flush() = Enrich.flush()
    override fun places() = Enrich.places()
    override fun observe(listener: EnrichListener) = Enrich.observe(listener)
    override fun clear() = Enrich.clear()
}
