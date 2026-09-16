package ai.terrabite.prism

import ai.terrabite.prism.enrich.EnrichConfig
import ai.terrabite.prism.enrich.EnrichRetention

/** How much history places are inferred from. Older stays are forgotten. */
enum class PrismPlaceRetention {
    ONE_MONTH, THREE_MONTHS, SIX_MONTHS;

    @JvmSynthetic
    internal fun toEnrich(): EnrichRetention = when (this) {
        ONE_MONTH -> EnrichRetention.ONE_MONTH
        THREE_MONTHS -> EnrichRetention.THREE_MONTHS
        SIX_MONTHS -> EnrichRetention.SIX_MONTHS
    }

    internal companion object {
        @JvmSynthetic
        fun from(r: EnrichRetention): PrismPlaceRetention = when (r) {
            EnrichRetention.ONE_MONTH -> ONE_MONTH
            EnrichRetention.SIX_MONTHS -> SIX_MONTHS
            else -> THREE_MONTHS
        }
    }
}

/**
 * Places configuration. Setting it on [PrismConfig.enrich] turns place
 * inference on; everything runs on the device and nothing leaves it.
 */
data class PrismEnrichConfig @JvmOverloads constructor(
    val retention: PrismPlaceRetention = PrismPlaceRetention.THREE_MONTHS,
) {
    @JvmSynthetic
    internal fun toEnrich(): EnrichConfig = EnrichConfig(retention.toEnrich())

    class Builder {
        private var retention = PrismPlaceRetention.THREE_MONTHS
        fun setRetention(retention: PrismPlaceRetention) = apply { this.retention = retention }
        fun build() = PrismEnrichConfig(retention)
    }
}
