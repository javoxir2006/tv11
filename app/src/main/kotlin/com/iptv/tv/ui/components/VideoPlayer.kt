package com.iptv.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Thin Compose wrapper around ExoPlayer's [PlayerView].
 *
 * We pass the already-configured [ExoPlayer] instance in — we never
 * create or destroy the player here to avoid memory leaks.
 */
@Composable
fun VideoPlayer(
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false               // TV: no on-screen controls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true                 // Extra safety: keep display on
                }
            },
            update = { view ->
                // Reconnect player if view is recycled (e.g. after back stack)
                if (view.player !== player) {
                    view.player = player
                }
            }
        )
    }

    // Nothing to dispose — player lifecycle is owned by MainActivity
    DisposableEffect(Unit) {
        onDispose { /* intentionally empty */ }
    }
}
