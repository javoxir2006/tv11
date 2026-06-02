package com.iptv.tv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.tv.player.PlayerManager
import com.iptv.tv.ui.screens.HomeScreen
import com.iptv.tv.ui.theme.BgDark
import com.iptv.tv.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // PlayerManager is NOT a ViewModel — it holds a Context reference and
    // must be released in onDestroy, not on config change.
    private lateinit var playerManager: PlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on — belt-and-suspenders alongside WAKE_LOCK in the player
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system UI for a lean TV experience
        hideSystemUi()

        playerManager = PlayerManager(applicationContext)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var isFullscreen by remember { mutableStateOf(false) }

            // When the selected channel changes, tell the player to start streaming
            LaunchedEffect(uiState.selectedIndex, uiState.channels) {
                val channel = uiState.channels.getOrNull(uiState.selectedIndex) ?: return@LaunchedEffect
                playerManager.play(channel.streamUrl)
            }

            // Fullscreen toggle: hide/show system bars
            LaunchedEffect(isFullscreen) {
                if (isFullscreen) hideSystemUi() else showSystemUi()
            }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
            ) {
                HomeScreen(
                    viewModel         = viewModel,
                    player            = playerManager.getPlayer(),
                    isFullscreen      = isFullscreen,
                    onToggleFullscreen = { isFullscreen = !isFullscreen }
                )
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onStart() {
        super.onStart()
        playerManager.resume()
    }

    override fun onStop() {
        super.onStop()
        // Pause when the app is backgrounded — resumes in onStart
        playerManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only release when truly destroying (not config change)
        if (!isChangingConfigurations) {
            playerManager.release()
        }
    }

    // ── System UI helpers ─────────────────────────────────────────────────────

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUi() {
        WindowInsetsControllerCompat(window, window.decorView)
            .show(WindowInsetsCompat.Type.systemBars())
    }
}
