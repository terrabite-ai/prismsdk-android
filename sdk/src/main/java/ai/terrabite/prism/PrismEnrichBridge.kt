package ai.terrabite.prism

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import ai.terrabite.prism.enrich.EnrichFix
import ai.terrabite.prism.enrich.EnrichFixKind
import ai.terrabite.prism.enrich.EnrichObservation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.TimeZone

/**
 * Wires the Enrich binary into Prism: feeds it every location while enabled,
 * runs a catch-up when the app returns to the foreground and flushes when it
 * leaves, and maps its results into Prism's own types.
 */
internal object PrismEnrichBridge {

    @Volatile var engine: PlaceEngine = LiveEnrich

    @Volatile var placesListener: PrismPlacesListener? = null
    @Volatile private var lastConfig: PrismEnrichConfig? = null
    @Volatile private var observation: EnrichObservation? = null
    @Volatile private var lifecycleInstalled = false

    private val placesFlow = MutableSharedFlow<PrismPlaces>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val placesUpdates: Flow<PrismPlaces> = placesFlow.asSharedFlow()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) { engine.catchUp() }
        override fun onStop(owner: LifecycleOwner) { engine.flush() }
    }

    fun attach(app: Context) {
        engine.attach(app)
        if (engine.isEnabled()) install()
    }

    fun apply(config: PrismEnrichConfig?) {
        lastConfig = config
        if (config == null) {
            uninstall()
            engine.stop()
        } else {
            engine.start(config.toEnrich())
            install()
        }
    }

    fun places(): PrismPlaces = PrismPlaces.from(engine.places())

    fun clear() {
        engine.clear()
        lastConfig?.let { engine.start(it.toEnrich()) }
    }

    /** Only tests call this; it returns the bridge to its initial state. */
    fun resetForTest() {
        uninstall()
        placesListener = null
        lastConfig = null
        engine = LiveEnrich
    }

    private fun install() {
        if (PrismDispatcher.enrichSink == null) PrismDispatcher.enrichSink = { engine.ingest(it.toEnrichFix()) }
        if (observation == null) {
            observation = engine.observe { places ->
                val mapped = PrismPlaces.from(places)
                placesFlow.tryEmit(mapped)
                placesListener?.onPlaces(mapped)
            }
        }
        if (!lifecycleInstalled) {
            lifecycleInstalled = true
            onMain { ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver) }
        }
    }

    private fun uninstall() {
        PrismDispatcher.enrichSink = null
        observation?.cancel()
        observation = null
        if (lifecycleInstalled) {
            lifecycleInstalled = false
            onMain { ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver) }
        }
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else Handler(Looper.getMainLooper()).post(block)
    }
}

/**
 * Maps a Prism location to Enrich's input. The engine's timezone string is
 * DST-insensitive, so the offset comes from the device's zone at the fix
 * instant instead.
 */
@JvmSynthetic
internal fun PrismLocation.toEnrichFix(): EnrichFix = EnrichFix(
    latitude = latitude,
    longitude = longitude,
    timestampMs = timestamp,
    tzOffsetMinutes = TimeZone.getDefault().getOffset(timestamp) / 60_000,
    horizontalAccuracyMeters = horizontalAccuracy.toDouble(),
    speedMps = speed.toDouble(),
    kind = when (type) {
        PrismLocationType.MOVING -> EnrichFixKind.MOVING
        PrismLocationType.STATIONARY -> EnrichFixKind.STATIONARY
        PrismLocationType.VISIT -> EnrichFixKind.VISIT
    },
)
