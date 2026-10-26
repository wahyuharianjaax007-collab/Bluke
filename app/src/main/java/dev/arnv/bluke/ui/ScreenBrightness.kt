package dev.arnv.bluke.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/**
 * Adjusts the Android phone's own screen brightness (not the Bluetooth host).
 * Needs the "Modify system settings" special permission (WRITE_SETTINGS), which the user grants
 * once from a system screen that this helper opens automatically.
 */
internal object ScreenBrightness {
    const val STEP = 12          // on the 0..255 system scale (~5 %)
    private const val MIN = 5    // never go fully black, the screen would be unusable
    private const val MAX = 255

    /** Returns false (and opens the permission screen) when WRITE_SETTINGS is not granted yet. */
    fun change(context: Context, delta: Int): Boolean {
        if (!Settings.System.canWrite(context)) {
            requestPermission(context)
            return false
        }
        return try {
            val resolver = context.contentResolver
            // An active auto-brightness mode overrides the manual value, so switch to manual first.
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
            )
            val current = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 128)
            Settings.System.putInt(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (current + delta).coerceIn(MIN, MAX),
            )
            true
        } catch (_: SecurityException) {
            requestPermission(context)
            false
        }
    }

    private fun requestPermission(context: Context) {
        Toast.makeText(
            context,
            "Izinkan \"Ubah setelan sistem\" untuk mengatur kecerahan",
            Toast.LENGTH_LONG,
        ).show()
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
