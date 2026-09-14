package ai.terrabite.prism

import com.localsdk.modules.locationtracking.enums.TrackingMode

/**
 * How aggressively to track.
 *
 * The mode controls *how often* a fix reaches you; [PrismConfig.horizontalAccuracyThreshold]
 * controls *how good* it has to be. They are independent.
 */
enum class PrismTrackingMode {

    /** Most frequent updates, highest battery cost. */
    PRECISE,

    /** Balanced. The usual choice. */
    STANDARD,

    /** Fewest updates, lowest battery cost. */
    EFFICIENT;

    @JvmSynthetic
    internal fun toEngine(): TrackingMode = when (this) {
        PRECISE -> TrackingMode.PRECISE
        STANDARD -> TrackingMode.STANDARD
        EFFICIENT -> TrackingMode.EFFICIENT
    }

    internal companion object {
        @JvmSynthetic
        fun from(mode: TrackingMode): PrismTrackingMode = when (mode) {
            TrackingMode.PRECISE -> PRECISE
            TrackingMode.STANDARD -> STANDARD
            TrackingMode.EFFICIENT -> EFFICIENT
        }
    }
}
