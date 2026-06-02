package com.iptv.tv.data.model

/**
 * Represents a single TV channel.
 *
 * @param id        Unique channel number shown in the list
 * @param name      Display name of the channel
 * @param streamUrl Direct HLS (.m3u8) or DASH stream URL
 * @param logo      Short abbreviation used as placeholder logo text
 */
data class Channel(
    val id: Int,
    val name: String,
    val streamUrl: String,
    val logo: String = name.take(3)
)
