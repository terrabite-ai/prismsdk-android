package ai.terrabite.prism

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fans one engine callback out to Prism's listener and flow.
 *
 * The engine holds a single listener slot, so Prism owns that slot (see
 * [Prism]'s initialiser) and does its own distribution here. Everything runs on
 * whatever thread the engine calls from.
 *
 * `internal` so tests can feed it directly without an engine.
 */
internal object PrismDispatcher {

    @Volatile var locationListener: PrismLocationListener? = null
    @Volatile var errorListener: PrismErrorListener? = null

    // `extraBufferCapacity = 1` + `DROP_OLDEST` gives each slow collector the
    // newest value rather than an unbounded queue, and lets `tryEmit` succeed
    // without suspending on the engine's thread.
    private val locationFlow = MutableSharedFlow<PrismLocation>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val errorFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val locations: Flow<PrismLocation> = locationFlow.asSharedFlow()
    val errors: Flow<String> = errorFlow.asSharedFlow()

    fun deliver(location: PrismLocation) {
        locationListener?.onLocation(location)
        locationFlow.tryEmit(location)
    }

    fun deliverError(error: String) {
        errorListener?.onError(error)
        errorFlow.tryEmit(error)
    }
}
