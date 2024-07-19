package tv.iptv.tun.tviptv.models

import kotlinx.serialization.Serializable

@Serializable
data class ChannelDTO(
    val channelId: String,
    val channelName: String,
    val channelCategoryId: String,
    val channelCategoryName: String,
    val channelLogo: String,
    val channelUrls: List<ChannelUrl>,
    val channelType: String,
    val tags: String?,
) {

    fun isRadio() = channelType == "Radio" || channelCategoryId == "VOV"
            || channelCategoryId == "VOH" || channelCategoryId == "Radio"

    @Serializable
    data class ChannelUrl(
        val url: String,
        val priority: Int,
        val sourceFrom: String
    ) {
        override fun toString(): String {
            return "{" +
                    "\"url\": \"$url\", " +
                    "\"priority\": $priority, " +
                    "\"sourceFrom\": \"$sourceFrom\"" +
                    "}"
        }
    }

    override fun toString(): String {
        return "{" +
                "\"channelId\": \"$channelId\", " +
                "\"channelName\": \"$channelName\", " +
                "\"channelCategoryId\": \"$channelCategoryId\", " +
                "\"channelCategoryName\": \"$channelCategoryName\", " +
                "\"channelLogo\": \"$channelLogo\", " +
                "\"channelUrls\": $channelUrls, " +
                "\"channelType\": \"$channelType\", " +
                "\"tags\": \"$tags\"" +
                "}"
    }

}