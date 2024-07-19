package tv.iptv.tun.tviptv.models

import kotlinx.serialization.Serializable

@Serializable
data class ChannelSourceConfig(
    val baseUrl: String,
    val mainPagePath: String,
    val getLinkStreamPath: String
) {
}