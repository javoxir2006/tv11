# IPTV Android TV — Kotlin / Compose / ExoPlayer

Native Android TV rewrite of the HTML+HLS.js IPTV app.

---

## What fixed the "goes black after 5 minutes" bug

The root cause in the web version was two things working against each other:

1. **Android doze / CPU sleep** — after ~5 minutes of no UI interaction,
   Android throttles the CPU and network. A WebView with HLS.js has no way
   to prevent this. ExoPlayer prevents it via:

   ```kotlin
   ExoPlayer.Builder(context).setWakeMode(C.WAKE_MODE_NETWORK)
   ```

   This acquires a `PARTIAL_WAKE_LOCK` + `WIFI_LOCK` automatically so the
   CPU and Wi-Fi radio never sleep while the player is active.

2. **Server-side idle timeout** — many HLS CDNs (smotrim, cdnvideo) drop
   connections that haven't fetched a new segment in ~60 seconds. HLS.js on
   a sleeping WebView missed segments; by the time it woke up the connection
   was dead. ExoPlayer's built-in `DefaultLoadControl` + the auto-retry
   listener in `PlayerManager` reconnects within ~2 seconds.

3. **FLAG_KEEP_SCREEN_ON** in `MainActivity` keeps the display on as an
   extra layer of protection.

---

## Project structure

```
app/src/main/kotlin/com/iptv/tv/
├── data/
│   ├── model/Channel.kt               — data class
│   └── repository/ChannelRepository.kt — hardcoded channel list
├── player/
│   └── PlayerManager.kt               — ExoPlayer wrapper (single instance)
├── ui/
│   ├── screens/HomeScreen.kt          — root Compose screen
│   ├── components/
│   │   ├── ChannelList.kt             — left panel
│   │   └── VideoPlayer.kt             — ExoPlayer surface wrapper
│   └── theme/Theme.kt                 — colours matching original CSS
├── viewmodel/MainViewModel.kt
└── MainActivity.kt
```

---

## How to build

1. Open in Android Studio Ladybug (2024.2+) or later.
2. Sync Gradle.
3. Run on an Android TV emulator (API 23+) or physical device.

To install on a real TV via ADB:
```bash
adb connect <TV_IP>:5555
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Updating channels

Edit `ChannelRepository.kt` — add/remove `Channel(...)` entries.
All channel objects use direct `.m3u8` URLs. The two `smotrim.ru` iframe
URLs from the original HTML **cannot** be used by ExoPlayer; they have been
replaced with the known direct HLS endpoints for those channels.

---

## Notes on specific streams

| # | Channel     | Note |
|---|-------------|------|
| 1 | Россия 24   | Replaced smotrim iframe → direct cdnvideo HLS |
| 6 | Планета 1   | Replaced smotrim iframe → direct cdnvideo HLS |
| 7 | СТВ         | sitv.ru stream — may require VPN from some regions |

If a channel stops working, only `ChannelRepository.kt` needs updating.
