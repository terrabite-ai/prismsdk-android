package ai.terrabite.prism

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * What the user has granted.
 *
 * Android's runtime permissions do not distinguish "never asked" from "denied"
 * without an `Activity`, so this reports three levels, plus whether the grant
 * is precise (fine) or approximate (coarse only).
 */
data class PrismPermissionStatus(
    val level: Level,

    /** `true` when fine location is granted; `false` for coarse-only or nothing. */
    val precise: Boolean,
) {

    enum class Level {
        /** Neither fine nor coarse location is granted. */
        NOT_GRANTED,

        /** Location is granted while the app is in use. */
        WHEN_IN_USE,

        /** Location is granted in the foreground and the background. */
        ALWAYS,
    }

    /** Granted at any level. */
    val isGranted: Boolean get() = level != Level.NOT_GRANTED

    /** Granted for background use specifically. */
    val isBackgroundGranted: Boolean get() = level == Level.ALWAYS

    /** Full-accuracy location is available. `false` both for approximate and for nothing. */
    val isPrecise: Boolean get() = precise && isGranted

    companion object {

        /** The current state, read from the system without prompting. */
        @JvmStatic
        fun of(context: Context): PrismPermissionStatus {
            val fine = context.has(Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = context.has(Manifest.permission.ACCESS_COARSE_LOCATION)
            val foreground = fine || coarse

            // Before Android 10 there was no separate background permission: a
            // foreground grant covered the background too.
            val background = when {
                !foreground -> false
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> true
                else -> context.has(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            val level = when {
                !foreground -> Level.NOT_GRANTED
                background -> Level.ALWAYS
                else -> Level.WHEN_IN_USE
            }
            return PrismPermissionStatus(level = level, precise = fine)
        }

        private fun Context.has(permission: String): Boolean =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}
