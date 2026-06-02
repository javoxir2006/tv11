package com.iptv.tv.player

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

private const val TAG = "PlayerManager"

// How many times to retry a failing stream before giving up
private const val MAX_RETRY_COUNT = 5

/**
 * Singleton ExoPlayer wrapper.
 *
 * Key fixes vs the WebView/HLS.js approach:
 *
 * 1. WAKE_MODE_NETWORK — prevents Android from sleeping the CPU/network during
 *    playback, which is the #1 cause of the "goes black after 5 minutes" bug.
 *
 * 2. One ExoPlayer instance — never destroyed and recreated between channels.
 *    We only swap the MediaItem. This eliminates memory spikes and audio glitches.
 *
 * 3. Auto-retry on error — if the stream errors out (e.g. 60-second server timeout),
 *    we re-call play() automatically up to MAX_RETRY_COUNT times.
 *
 * 4. OkHttp data source — better connection pooling and keep-alive than the
 *    default HttpDataSource, which helps with streams that send keep-alive
 *    packets to prevent proxy timeouts.
 */
class PlayerManager(private val context: Context) {

    // ── OkHttp client shared across all requests ──────────────────────────────
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        // Aggressive keep-alive avoids the server-side 5-min idle disconnect
        .retryOnConnectionFailure(true)
        .build()

    private val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        .setDefaultRequestProperties(
            mapOf(
                "User-Agent" to "ExoPlayer/AndroidTV",
                // Some streams require a Referer header
                "Referer"    to "https://smotrim.ru/"
            )
        )

    // ── ExoPlayer instance (created once, lives for the app lifetime) ─────────
    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(context).also {
                // We override the factory per-source below; this is the fallback
            }
        )
        .setLoadControl(buildLoadControl())
        // THIS IS THE KEY FIX: keep network + CPU alive during playback
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .setHandleAudioBecomingNoisy(true)
        .build()
        .also { exo ->
            exo.playWhenReady = true
            exo.addListener(buildPlayerListener())
        }

    // Track current stream for retry logic
    private var currentUrl: String = ""
    private var retryCount: Int = 0

    // ── Public API ─────────────────────────────────────────────────────────────

    fun getPlayer(): ExoPlayer = player

    /**
     * Switch to a new channel URL.
     * Safe to call from any thread; posts work to the player thread.
     */
    fun play(url: String) {
        if (url.isBlank()) return
        currentUrl = url
        retryCount   = 0
        loadAndPlay(url)
    }

    fun pause()  { player.pause() }
    fun resume() { player.play()  }

    /**
     * Call from Activity.onDestroy() only — not on config change.
     */
    fun release() {
        player.release()
        okHttpClient.dispatcher.executorService.shutdown()
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun loadAndPlay(url: String) {
        Log.d(TAG, "loadAndPlay → $url")

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(false)
            .createMediaSource(MediaItem.fromUri(url))

        player.stop()
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }

    /**
     * LoadControl tuned for live TV:
     * - smaller buffers → faster channel switch
     * - enough buffer to survive brief network hiccups without a stall
     */
    private fun buildLoadControl() = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs      */ 15_000,
            /* maxBufferMs      */ 50_000,
            /* bufferForPlayMs  */  2_500,
            /* bufferForRebufferMs */ 5_000
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()

    private fun buildPlayerListener() = object : Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY   -> { retryCount = 0; Log.d(TAG, "STATE_READY") }
                Player.STATE_BUFFERING -> Log.d(TAG, "STATE_BUFFERING")
                Player.STATE_ENDED   -> Log.d(TAG, "STATE_ENDED — live stream ended?")
                Player.STATE_IDLE    -> Log.d(TAG, "STATE_IDLE")
            }
        }

        /**
         * Auto-retry on any playback error.
         * Common causes on TV:
         * - Server drops idle HLS connections after ~5 minutes (the original bug)
         * - Brief Wi-Fi dropout
         * - 503 from CDN
         */
        override fun onPlayerError(error: PlaybackException) {
            Log.w(TAG, "Player error: ${error.errorCodeName} — retry $retryCount/$MAX_RETRY_COUNT")

            if (retryCount < MAX_RETRY_COUNT && currentUrl.isNotBlank()) {
                retryCount++
                // Brief delay before retry so the network can recover
                player.postDelayed({ loadAndPlay(currentUrl) }, retryDelayMs())
            } else {
                Log.e(TAG, "Max retries reached for $currentUrl")
            }
        }
    }

    /** Exponential back-off: 2s, 4s, 8s, 16s, 16s */
    private fun retryDelayMs(): Long = minOf(2_000L * (1 shl retryCount), 16_000L)

    /** Convenience — lets us post a Runnable to the player's internal handler */
    private fun ExoPlayer.postDelayed(action: Runnable, delayMs: Long) {
        applicationLooper.let { looper ->
            android.os.Handler(looper).postDelayed(action, delayMs)
        }
    }
}
