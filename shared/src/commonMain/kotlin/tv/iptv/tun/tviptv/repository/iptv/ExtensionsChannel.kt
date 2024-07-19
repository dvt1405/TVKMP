package tv.iptv.tun.tviptv.repository.iptv

import tv.iptv.tun.tviptv.PlayerConstants
import tv.iptv.tun.tviptv.repository.iptv.model.TVScheduler
import tv.iptv.tun.tviptv.utils.toUrlOrNull

data class ExtensionsChannel(
    var tvGroup: String,
    val logoChannel: String,
    val tvChannelName: String,
    val tvStreamLink: String,
    val sourceFrom: String,
    val channelId: String,
    val channelPreviewProviderId: Long = -1,
    val isHls: Boolean,
    val catchupSource: String = "",
    val userAgent: String = "",
    val referer: String = "",
    val props: Map<String, String>? = null,
    val extensionSourceId: String
) {

    var currentProgramme: TVScheduler.Programme? = null

    val isValidChannel: Boolean
        get() {
            return tvGroup.isNotBlank() && tvStreamLink.toUrlOrNull()?.host != null
                    && !(tvChannelName.contains("Donate")
                    || tvChannelName.lowercase().startsWith("tham gia group")
                    || tvChannelName.lowercase().startsWith("nhóm zalo")
                    )

        }

    fun getMapData() = mapOf(
        PlayerConstants.EXTRA_MEDIA_ID to channelId,
        PlayerConstants.EXTRA_MEDIA_TITLE to tvChannelName,
        PlayerConstants.EXTRA_MEDIA_DESCRIPTION to (currentProgramme?.description?.takeIf {
            it.isNotBlank()
        } ?: tvGroup),
        PlayerConstants.EXTRA_MEDIA_ALBUM_TITLE to tvGroup,
        PlayerConstants.EXTRA_MEDIA_THUMB to logoChannel,
        PlayerConstants.EXTRA_MEDIA_ALBUM_ARTIST to extensionSourceId,
        PlayerConstants.EXTRA_MEDIA_SOURCE_TYPE to "IPTV"
    )

    override fun toString(): String {
        return "{" +
                "channelId=$channelId,\n" +
                "tvGroup=$tvGroup,\n" +
                "logoChannel=$logoChannel,\n" +
                "tvChannelName=$tvChannelName,\n" +
                "tvStreamLink=$tvStreamLink,\n" +
                "sourceFrom=$sourceFrom,\n" +
                "channelPreviewProviderId=$channelPreviewProviderId,\n" +
                "isHls=$isHls,\n" +
                "catchupSource=$catchupSource,\n" +
                "userAgent=$userAgent,\n" +
                "referer=$referer,\n" +
                "extensionSourceId=$extensionSourceId,\n" +
                "props=$props," +
                "currentProgramme: $currentProgramme" +
                "}"
    }
}

class ExtensionsChannelAndConfig(
    val channel: ExtensionsChannel,
    val config: IPTVSourceConfig
) {
}