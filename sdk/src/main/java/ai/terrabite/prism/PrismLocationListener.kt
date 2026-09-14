package ai.terrabite.prism

/**
 * Receives locations. A `fun interface`, so Kotlin passes a lambda and Java a
 * lambda or an anonymous class.
 *
 * Called on the engine's thread. Hop to the main thread before touching UI.
 */
fun interface PrismLocationListener {
    fun onLocation(location: PrismLocation)
}

/**
 * Receives errors. The SDK keeps tracking unless the message says otherwise.
 * Same thread rule as [PrismLocationListener].
 */
fun interface PrismErrorListener {
    fun onError(error: String)
}
