package com.iptv.tv.data.repository

import com.iptv.tv.data.model.Channel

/**
 * Single source of truth for channel data.
 *
 * NOTE: Channels 1 and 6 originally used smotrim.ru iframe URLs which cannot
 * be played directly by ExoPlayer. They have been replaced with their known
 * direct HLS endpoints. If they stop working, update the streamUrl here only.
 *
 * To load channels from a remote JSON/M3U in the future, replace getChannels()
 * with a suspend function that fetches from your API.
 */
class ChannelRepository {

    fun getChannels(): List<Channel> = listOf(
        Channel(
            id = 1,
            name = "Россия 24",
            logo = "Р24",
            // Direct HLS — replaces the iframe smotrim URL which ExoPlayer can't open
            streamUrl = "https://live-vgtrksmotrim.cdnvideo.ru/vgtrksmotrim/smotrim-live-05.smil/playlist.m3u8"
        ),
        Channel(
            id = 2,
            name = "Россия 1",
            logo = "Р1",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/5.m3u8"
        ),
        Channel(
            id = 3,
            name = "НТВ",
            logo = "НТВ",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/7.m3u8"
        ),
        Channel(
            id = 4,
            name = "Первый канал",
            logo = "П1",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/4.m3u8"
        ),
        Channel(
            id = 5,
            name = "360 Новости",
            logo = "360",
            streamUrl = "https://live-vgtrksmotrim.cdnvideo.ru/vgtrksmotrim/smotrim-live-04-srt.smil/playlist.m3u8"
        ),
        Channel(
            id = 6,
            name = "Планета 1",
            logo = "Пла",
            // Replaced iframe URL with known direct stream
            streamUrl = "https://live-vgtrksmotrim.cdnvideo.ru/vgtrksmotrim/smotrim-live-02.smil/playlist.m3u8"
        ),
        Channel(
            id = 7,
            name = "СТВ",
            logo = "СТВ",
            streamUrl = "https://sitv.ru/vgtrk/stv.m3u8"
        ),
        Channel(
            id = 8,
            name = "Смотрим",
            logo = "Смт",
            streamUrl = "https://live-vgtrksmotrim.cdnvideo.ru/vgtrksmotrim/smotrim-live-01.smil/playlist.m3u8"
        ),
        Channel(
            id = 9,
            name = "ТОЛК",
            logo = "ТЛК",
            streamUrl = "https://live-tolknews.cdnvideo.ru/tolknews/stream/playlist.m3u8"
        ),
        Channel(
            id = 10,
            name = "Домашний",
            logo = "Дом",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/17.m3u8"
        ),
        Channel(
            id = 11,
            name = "За ТВ",
            logo = "ЗаТ",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/25.m3u8"
        ),
        Channel(
            id = 12,
            name = "Звезда",
            logo = "Зв",
            streamUrl = "https://tvzvezda.bonus-tv.ru/cdn/tvzvezda/playlist.m3u8"
        ),
        Channel(
            id = 13,
            name = "Мир",
            logo = "Мир",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/22.m3u8"
        ),
        Channel(
            id = 14,
            name = "Пятница",
            logo = "Пят",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/19.m3u8"
        ),
        Channel(
            id = 15,
            name = "Спас",
            logo = "Сп",
            streamUrl = "https://streaming.televizor-24-tochka.ru/live/15.m3u8"
        ),
        Channel(
            id = 16,
            name = "Север",
            logo = "Сев",
            streamUrl = "https://live2.mediacdn.ru/sr1/sever/playlist.m3u8"
        ),
        Channel(
            id = 17,
            name = "РТ Документари",
            logo = "РТ",
            streamUrl = "https://rt-doc.rttv.com/dvr/rtdru/playlist.m3u8"
        ),
    )
}
