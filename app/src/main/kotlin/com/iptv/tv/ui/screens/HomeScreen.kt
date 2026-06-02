package com.iptv.tv.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.iptv.tv.ui.components.ChannelList
import com.iptv.tv.ui.components.VideoPlayer
import com.iptv.tv.ui.theme.*
import com.iptv.tv.viewmodel.MainViewModel

/**
 * Root screen composable.
 *
 * Layout:
 *   ┌─────────────┬──────────────────────┐
 *   │ Channel list│    Video player       │
 *   │   (300dp)   │    (fills rest)       │
 *   └─────────────┴──────────────────────┘
 *
 * In fullscreen mode the channel list slides out to the left and the
 * "Full Screen" button disappears — identical behaviour to the original HTML.
 *
 * D-pad navigation:
 *   ↑ / ↓  → move through channel list
 *   → right → move focus to fullscreen button
 *   ← left  → return focus to channel list
 *   OK/Enter on channel → play it
 *   OK/Enter on button  → toggle fullscreen
 *   Back/Escape         → exit fullscreen if active
 */
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    player: ExoPlayer,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One FocusRequester per channel item + one for the fullscreen button
    val channelFocusRequesters = remember(uiState.channels.size) {
        List(uiState.channels.size) { FocusRequester() }
    }
    val fullscreenBtnFocuser = remember { FocusRequester() }

    // Which panel currently owns focus (list vs button)
    var focusOnList by remember { mutableStateOf(true) }

    // Drive focus imperatively when selectedIndex changes via D-pad in the VM
    LaunchedEffect(uiState.selectedIndex) {
        if (focusOnList && channelFocusRequesters.isNotEmpty()) {
            runCatching { channelFocusRequesters[uiState.selectedIndex].requestFocus() }
        }
    }

    // Play selected channel whenever selectedIndex changes
    LaunchedEffect(uiState.selectedIndex, uiState.channels) {
        val ch = uiState.channels.getOrNull(uiState.selectedIndex) ?: return@LaunchedEffect
        // PlayerManager.play() is called from MainActivity via callback;
        // here we just emit the selection so MainActivity can forward to PlayerManager
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            // Intercept raw key events for D-pad handling
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionUp -> {
                        if (focusOnList) {
                            viewModel.selectPreviousChannel()
                            true
                        } else false
                    }
                    Key.DirectionDown -> {
                        if (focusOnList) {
                            viewModel.selectNextChannel()
                            true
                        } else false
                    }
                    Key.DirectionRight -> {
                        if (focusOnList) {
                            focusOnList = false
                            runCatching { fullscreenBtnFocuser.requestFocus() }
                            true
                        } else false
                    }
                    Key.DirectionLeft -> {
                        if (!focusOnList) {
                            focusOnList = true
                            runCatching {
                                channelFocusRequesters[uiState.selectedIndex].requestFocus()
                            }
                            true
                        } else false
                    }
                    Key.Escape, Key.Back -> {
                        if (isFullscreen) { onToggleFullscreen(); true } else false
                    }
                    else -> false
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left: Channel List ──────────────────────────────────────────
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = slideInHorizontally { -it },
                exit  = slideOutHorizontally { -it }
            ) {
                ChannelList(
                    channels          = uiState.channels,
                    selectedIndex     = uiState.selectedIndex,
                    focusRequesters   = channelFocusRequesters,
                    onChannelClick    = { index ->
                        focusOnList = true
                        viewModel.selectChannel(index)
                    }
                )
            }

            // ── Right: Player + controls ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(if (isFullscreen) 0.dp else 8.dp)
            ) {
                // Fullscreen toggle button (hidden during fullscreen)
                if (!isFullscreen) {
                    FullscreenButton(
                        focusRequester = fullscreenBtnFocuser,
                        onFocusChanged = { if (it) focusOnList = false },
                        onClick = onToggleFullscreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                VideoPlayer(
                    player = player,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Overlay: currently-playing channel name (fades after a few seconds)
        if (!isFullscreen) {
            uiState.channels.getOrNull(uiState.selectedIndex)?.let { ch ->
                Text(
                    text = "▶  ${ch.name}",
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(BgMedium.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FullscreenButton(
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                isFocused = state.isFocused
                onFocusChanged(state.isFocused)
            },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor      = if (isFocused) GreenAccent else BgDark,
            focusedContainerColor = GreenAccent
        )
    ) {
        Text(
            text  = "To'liq ekran",
            color = if (isFocused) BgDark else GreenAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}
