package dev.arnv.bluke.ui

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/** Same translucent scrim that every remote-control top bar already draws over the case colour. */
internal val RemoteBarScrim = Color.Black.copy(alpha = 0.45f)

@Composable
internal fun rememberIsLandscape(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

/** The colour a surface gets after [RemoteBarScrim] is drawn on top of it. */
private fun Color.underScrim(): Color = Color(
    red = red * (1f - RemoteBarScrim.alpha),
    green = green * (1f - RemoteBarScrim.alpha),
    blue = blue * (1f - RemoteBarScrim.alpha),
    alpha = 1f,
)

/**
 * Portrait: system bars stay visible and the status bar icons follow the top-bar colour.
 * Landscape: immersive (system bars hidden, swipe to reveal) to give the controls maximum room.
 */
@Composable
internal fun RemoteSystemBars(isLandscape: Boolean, baseColor: Color) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = baseColor.underScrim().luminance() > 0.5f
        controller.isAppearanceLightNavigationBars = baseColor.luminance() > 0.5f
        if (isLandscape) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(view) {
        onDispose {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

/** Navigation-bar padding plus camera-cutout padding on the left/right edges (landscape). */
internal fun Modifier.remoteRootInsets(): Modifier = composed {
    Modifier
        .windowInsetsPadding(WindowInsets.navigationBars)
        .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
}

/**
 * Portrait top bar container. It reserves the status-bar height and paints the same scrim as the
 * bar itself, so the status bar visually becomes part of the app bar (same colour as the theme).
 */
@Composable
internal fun RemoteTopBarFrame(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RemoteBarScrim),
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars),
        )
        content()
    }
}

/** One horizontally scrollable line of a portrait (two-line) top bar. */
@Composable
internal fun PortraitBarRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
