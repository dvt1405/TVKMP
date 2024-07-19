package tv.iptv.tun.tviptv.models

import kotlinx.serialization.Serializable

@Serializable
data class ChannelStreamLink(
    val channelId: String,
    val channelName: String,
    val channelCategoryId: String,
    val channelCategoryName: String,
    val channelLogo: String,
    val channelUrls: List<ChannelDTO.ChannelUrl>,
    val channelType: String,
    val tags: String?,
    val playUrl: String,
    val playerConfig: PlayerConfig,
    val playUrlSourceFrom: String
) {

    override fun toString(): String {
        return "{" +
                "\"channelId\":\"$channelId\"," +
                "\"channelCategoryId\":\"$channelCategoryId\"," +
                "\"channelUrls\":$channelUrls," +
                "\"channelType\":\"$channelType\"," +
                "\"channelLogo\":\"$channelLogo\"," +
                "\"channelName\":\"$channelName\"," +
                "\"channelCategoryName\":\"$channelCategoryName\"," +
                "\"tags\":\"$tags\"," +
                "\"playUrl\":\"$playUrl\"," +
                "\"playUrlSourceFrom\":\"$playUrlSourceFrom\"," +
                "\"playerConfig\": $playerConfig" +
                "}"
    }

    companion object {
        fun wrap(
            channelDTO: ChannelDTO,
            playUrl: String,
            playUrlSourceFrom: String,
            playerConfig: PlayerConfig
        ): ChannelStreamLink {
            return ChannelStreamLink(
                channelId = channelDTO.channelId,
                channelUrls = channelDTO.channelUrls,
                channelCategoryId = channelDTO.channelCategoryId,
                channelType = channelDTO.channelType,
                channelLogo = channelDTO.channelLogo,
                channelName = channelDTO.channelName,
                channelCategoryName = channelDTO.channelCategoryName,
                tags = channelDTO.tags,
                playUrl = playUrl,
                playUrlSourceFrom = playUrlSourceFrom,
                playerConfig = playerConfig
            )
        }
    }
}