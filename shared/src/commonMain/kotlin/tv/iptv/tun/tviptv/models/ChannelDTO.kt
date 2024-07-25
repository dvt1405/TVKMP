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

    companion object {
        val testObj by lazy {
            ChannelDTO(
                "Channel Test",
                "Channel Test",
                "Channel Test",
                "Channel Test",
                "https://s8.edge.cdn.sctvonline.vn/cdn-cgi/edge/v2/imaginary.endpoint.cdn.sctvonline.vn/nginx.s8.edge.cdn.sctvonline.vn/tenants/none_tenant/photos/3_efafd3ec.jpg?width=384&version=9&type=auto&s3_origin=https%3A%2F%2Fsctv-main-2.s3-hcm.cloud.cmctelecom.vn",
                listOf(),
                "Channel Test",
                "Channel Test",
            )
        }
    }

}