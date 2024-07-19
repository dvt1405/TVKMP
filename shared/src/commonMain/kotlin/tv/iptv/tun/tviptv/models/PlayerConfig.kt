package tv.iptv.tun.tviptv.models

import kotlinx.serialization.Serializable
import tv.iptv.tun.tviptv.Constants

@Serializable
class PlayerConfig(
    val dataSourceType: String,
    val referer: String,
    val origin: String,
    val userAgent: String = Constants.USER_AGENT,
    val acceptLanguage: String = Constants.ACCEPT_LANGUAGE,
    val streamToken: String? = null,
    val host: String? = null,
    val streamAuthKey: String? = null,
    val streamAuthorization: String? = null,
    val dashLicenseType: String? = null,
    val drmLicenseUrl: String? = null,
    val dashLicenseKey: String? = null,
    val extraHeaders: Map<String, String>? = null,
    val drmExtra: Map<String, String>? = null
) {

    enum class Type(val value: String) {
        HLS("hls"),
        DASH("dash"),
        PROGRESSIVE("progressive"),
        DEFAULT("default"),
        RTSP("rtsp"),
        SMOOTH_STREAMING("smooth_streaming")
    }
    override fun toString(): String {
        return "{" +
                "\"dataSourceType\": \"$dataSourceType\", " +
                "\"referer\": \"$referer\", " +
                "\"origin\": \"$origin\", " +
                "\"userAgent\": \"$userAgent\", " +
                "\"acceptLanguage\": \"$acceptLanguage\", " +
                "\"streamToken\": \"$streamToken\", " +
                "\"host\": \"$host\", " +
                "\"host\": \"$streamAuthKey\", " +
                "\"streamAuthorization\": \"$streamAuthorization\", " +
                "\"dashLicenseType\": \"$dashLicenseType\", " +
                "\"dashLicenseUrl\": \"$drmLicenseUrl\", " +
                "\"dashLicenseKey\": \"$dashLicenseKey\", " +
                "\"host\": \"$host\"" +
                "}"
    }
}