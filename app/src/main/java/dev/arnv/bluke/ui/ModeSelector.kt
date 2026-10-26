package dev.arnv.bluke.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.arnv.bluke.QuickCycleActivity
import kotlin.math.abs

internal fun InputMode.icon(): ImageVector = when (this) {
    InputMode.KEYBOARD -> Icons.Default.Keyboard
    InputMode.TOUCHPAD -> Icons.Default.Mouse
    InputMode.GAMEPAD -> Icons.Default.SportsEsports
    InputMode.MEDIA_PRESENTATION -> Icons.Default.SmartDisplay
}

/**
 * Swipe vertically on the element to change input mode without tapping:
 * swipe up = next mode, swipe down = previous mode (like a picker wheel).
 */
@Composable
internal fun Modifier.modeSwipe(
    currentModeId: Int,
    sharedPrefs: SharedPreferences,
    onModeSelected: (Int) -> Unit,
): Modifier {
    val currentSelected by rememberUpdatedState(onModeSelected)
    return pointerInput(currentModeId) {
        val thresholdPx = 18.dp.toPx()
        var accumulated = 0f
        detectVerticalDragGestures(
            onDragStart = { accumulated = 0f },
            onDragEnd = { accumulated = 0f },
            onDragCancel = { accumulated = 0f },
        ) { change, dragAmount ->
            change.consume()
            accumulated += dragAmount
            if (abs(accumulated) >= thresholdPx) {
                val step = if (accumulated < 0f) 1 else -1
                accumulated = 0f
                val enabled = sharedPrefs.enabledInputModes()
                if (enabled.size > 1) {
                    val index = enabled.indexOfFirst { it.id == currentModeId }.coerceAtLeast(0)
                    currentSelected(enabled[(index + step + enabled.size) % enabled.size].id)
                }
            }
        }
    }
}

/** Drop-down list of the enabled input modes; picking one switches straight to it. */
@Composable
internal fun ModeDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentModeId: Int,
    sharedPrefs: SharedPreferences,
    onModeSelected: (Int) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        sharedPrefs.enabledInputModes().forEach { mode ->
            val selected = mode.id == currentModeId
            DropdownMenuItem(
                text = {
                    Text(
                        mode.displayName,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                leadingIcon = { Icon(mode.icon(), contentDescription = null) },
                trailingIcon = if (selected) {
                    { Icon(Icons.Default.Check, contentDescription = "Current mode") }
                } else {
                    null
                },
                onClick = {
                    onDismiss()
                    if (!selected) onModeSelected(mode.id)
                },
            )
        }
    }
}

internal fun Context.openQuickCycleSettings() {
    startActivity(Intent(this, QuickCycleActivity::class.java))
}

/**
 * Top-bar mode button. Tap = pick a mode from a list, swipe vertically = previous/next mode,
 * long-press = configure which modes are available.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ModeSwitchPill(
    currentModeId: Int,
    label: String,
    icon: ImageVector,
    sharedPrefs: SharedPreferences,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp,
    horizontalPadding: Dp = 8.dp,
    iconSize: Dp = 11.dp,
    fontSize: TextUnit = 9.sp,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = modifier
                .height(28.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.White.copy(alpha = 0.15f))
                .combinedClickable(
                    onClickLabel = "Choose input mode",
                    onLongClickLabel = "Configure input mode cycle",
                    onClick = { expanded = true },
                    onLongClick = { context.openQuickCycleSettings() },
                )
                .modeSwipe(currentModeId, sharedPrefs, onModeSelected)
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Switch Mode",
                tint = Color.White,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            )
        }
        ModeDropdown(
            expanded = expanded,
            onDismiss = { expanded = false },
            currentModeId = currentModeId,
            sharedPrefs = sharedPrefs,
            onModeSelected = onModeSelected,
        )
    }
}
