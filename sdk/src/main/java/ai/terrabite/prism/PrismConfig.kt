package ai.terrabite.prism

import android.app.NotificationManager
import androidx.annotation.DrawableRes
import com.localsdk.config.Config

/**
 * Tracking configuration.
 *
 * An immutable value. Build one with the constructor's named arguments from
 * Kotlin, or with [Builder] from Java, and hand it to [Prism.setConfig].
 *
 * ```kotlin
 * Prism.setConfig(
 *     PrismConfig(
 *         trackingMode = PrismTrackingMode.STANDARD,
 *         horizontalAccuracyThreshold = 50,
 *     )
 * )
 * ```
 *
 * Background tracking on Android runs inside a foreground service, which must
 * show a persistent notification. The `notification…` fields control it. Set
 * [notificationIcon] to one of your own drawables; `0` lets the engine fall back
 * to a default.
 */
data class PrismConfig @JvmOverloads constructor(

    /** Default [PrismTrackingMode.PRECISE]. */
    val trackingMode: PrismTrackingMode = PrismTrackingMode.PRECISE,

    /** Whether simulated locations are accepted. Default `false`. */
    val allowMockLocation: Boolean = false,

    /** Fixes with a worse radius of uncertainty, in metres, are discarded. Default `200`. */
    val horizontalAccuracyThreshold: Int = 200,

    /** Run tracking in a foreground service so it survives the app leaving the screen. Default `true`. */
    val foregroundServiceEnabled: Boolean = true,

    val notificationTitle: String = "Location Tracking",
    val notificationBody: String = "Your location is being tracked",
    @DrawableRes val notificationIcon: Int = 0,
    val notificationImportance: Int = NotificationManager.IMPORTANCE_LOW,
    val notificationSound: Boolean = false,
    val notificationVibration: Boolean = false,
) {

    /**
     * Writes this value into the engine's configuration.
     *
     * The engine's `Config` is a single global object whose builder mutates it
     * in place, so applying is a side effect, not a conversion. Every field is
     * written on every apply, so nothing from a previous configuration survives.
     *
     * Publishing is pinned off. Prism is tracking-only: locations go to the host
     * app and nowhere else, and this is where that decision is enforced.
     */
    // `@JvmSynthetic` hides this from Java. Kotlin `internal` is public in the
    // bytecode, only name-mangled, so without it Java autocomplete would offer a
    // method whose signature names an engine type.
    @JvmSynthetic
    internal fun toEngine(): Config = Config.Builder()
        .setTrackingMode(trackingMode.toEngine())
        .setAllowMockLocation(allowMockLocation)
        .setAccuracy(horizontalAccuracyThreshold)
        .setForegroundServiceEnabled(foregroundServiceEnabled)
        .setForegroundServiceNotificationTitle(notificationTitle)
        .setForegroundServiceNotificationBody(notificationBody)
        .setForegroundServiceNotificationIcon(notificationIcon)
        .setNotificationImportance(notificationImportance)
        .setNotificationSound(notificationSound)
        .setNotificationVibration(notificationVibration)
        .setPublishEnabled(false)
        .build()

    /** Builder for Java callers. Kotlin callers use the constructor. */
    class Builder {
        private var value = PrismConfig()

        fun setTrackingMode(mode: PrismTrackingMode) = apply { value = value.copy(trackingMode = mode) }
        fun setAllowMockLocation(allow: Boolean) = apply { value = value.copy(allowMockLocation = allow) }
        fun setHorizontalAccuracyThreshold(metres: Int) = apply { value = value.copy(horizontalAccuracyThreshold = metres) }
        fun setForegroundServiceEnabled(enabled: Boolean) = apply { value = value.copy(foregroundServiceEnabled = enabled) }
        fun setNotificationTitle(title: String) = apply { value = value.copy(notificationTitle = title) }
        fun setNotificationBody(body: String) = apply { value = value.copy(notificationBody = body) }
        fun setNotificationIcon(@DrawableRes icon: Int) = apply { value = value.copy(notificationIcon = icon) }
        fun setNotificationImportance(importance: Int) = apply { value = value.copy(notificationImportance = importance) }
        fun setNotificationSound(enabled: Boolean) = apply { value = value.copy(notificationSound = enabled) }
        fun setNotificationVibration(enabled: Boolean) = apply { value = value.copy(notificationVibration = enabled) }

        fun build(): PrismConfig = value
    }
}
