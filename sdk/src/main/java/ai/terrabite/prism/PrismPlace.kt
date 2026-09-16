package ai.terrabite.prism

import ai.terrabite.prism.enrich.EnrichConfidence
import ai.terrabite.prism.enrich.EnrichPlace
import ai.terrabite.prism.enrich.EnrichPlaceKind
import ai.terrabite.prism.enrich.EnrichPlaces

/** What a place is to the user. More kinds may be added; branch with `else`. */
enum class PrismPlaceKind {
    HOME, FREQUENT;

    internal companion object {
        @JvmSynthetic
        fun from(kind: EnrichPlaceKind): PrismPlaceKind = when (kind) {
            EnrichPlaceKind.HOME -> HOME
            else -> FREQUENT
        }
    }
}

/** How much evidence stands behind the home label. */
enum class PrismConfidence {
    PROVISIONAL, LOW, MODERATE, HIGH, CONFIRMED;

    internal companion object {
        @JvmSynthetic
        fun from(c: EnrichConfidence): PrismConfidence = when (c) {
            EnrichConfidence.PROVISIONAL -> PROVISIONAL
            EnrichConfidence.LOW -> LOW
            EnrichConfidence.MODERATE -> MODERATE
            EnrichConfidence.HIGH -> HIGH
            else -> CONFIRMED
        }
    }
}

/**
 * A place the user returns to. Values only — the SDK produces these.
 * Coordinates are the centre of the stays that make up the place.
 */
@ConsistentCopyVisibility
data class PrismPlace internal constructor(
    val kind: PrismPlaceKind,
    val latitude: Double,
    val longitude: Double,
    /** Separate stays observed at this place. */
    val visitCount: Int,
    /** Total time spent here, in milliseconds. */
    val totalDwellMs: Long,
    /** Distinct nights spent here. What the home label is based on. */
    val distinctNights: Int,
    val firstSeenMs: Long,
    val lastSeenMs: Long,
    /** Set for [PrismPlaceKind.HOME]; null for other kinds. */
    val confidence: PrismConfidence?,
) {
    /** Snake_case wire form for bridged hosts (React Native and similar). */
    fun toMap(): Map<String, Any?> = mapOf(
        "kind" to kind.name,
        "latitude" to latitude,
        "longitude" to longitude,
        "visit_count" to visitCount,
        "total_dwell_ms" to totalDwellMs,
        "distinct_nights" to distinctNights,
        "first_seen_ms" to firstSeenMs,
        "last_seen_ms" to lastSeenMs,
        "confidence" to confidence?.name,
    )

    internal companion object {
        @JvmSynthetic
        fun from(p: EnrichPlace) = PrismPlace(
            kind = PrismPlaceKind.from(p.kind),
            latitude = p.latitude,
            longitude = p.longitude,
            visitCount = p.visitCount,
            totalDwellMs = p.totalDwellMs,
            distinctNights = p.distinctNights,
            firstSeenMs = p.firstSeenMs,
            lastSeenMs = p.lastSeenMs,
            confidence = p.confidence?.let { PrismConfidence.from(it) },
        )
    }
}

/** The current inference. [home] is null until at least one stay exists. */
@ConsistentCopyVisibility
data class PrismPlaces internal constructor(
    val home: PrismPlace?,
    /** Other places, most time spent first. */
    val frequent: List<PrismPlace>,
    /** When this result was computed, epoch milliseconds; 0 before the first computation. */
    val computedAtMs: Long,
    val retention: PrismPlaceRetention,
    /** Stays inside the retention window. */
    val stayCount: Int,
    /** Earliest stay inside the window, or null. Says how much history the result rests on. */
    val observedFromMs: Long?,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "home" to home?.toMap(),
        "frequent" to frequent.map { it.toMap() },
        "computed_at_ms" to computedAtMs,
        "retention" to retention.name,
        "stay_count" to stayCount,
        "observed_from_ms" to observedFromMs,
    )

    internal companion object {
        @JvmSynthetic
        fun from(p: EnrichPlaces) = PrismPlaces(
            home = p.home?.let { PrismPlace.from(it) },
            frequent = p.frequent.map { PrismPlace.from(it) },
            computedAtMs = p.computedAtMs,
            retention = PrismPlaceRetention.from(p.retention),
            stayCount = p.stayCount,
            observedFromMs = p.observedFromMs,
        )

        val EMPTY = PrismPlaces(null, emptyList(), 0, PrismPlaceRetention.THREE_MONTHS, 0, null)
    }
}

/** Receives place changes. Called on a background thread. */
fun interface PrismPlacesListener {
    fun onPlaces(places: PrismPlaces)
}
