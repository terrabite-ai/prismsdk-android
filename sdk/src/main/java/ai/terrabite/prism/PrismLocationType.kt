package ai.terrabite.prism

import com.localsdk.modules.locationtracking.enums.LocationType

/** How a location was produced. */
enum class PrismLocationType {

    /** The device was moving when the fix was taken. */
    MOVING,

    /** The device was stationary, or its speed could not be determined. */
    STATIONARY,

    /** A stay at a place. */
    VISIT;

    internal companion object {
        @JvmSynthetic
        fun from(type: LocationType): PrismLocationType = when (type) {
            LocationType.MOVING -> MOVING
            LocationType.STATIONARY -> STATIONARY
            LocationType.VISIT -> VISIT
        }
    }
}
