package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tv.iptv.tun.tviptv.Constants
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelSourceConfig
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.network.apimodels.FormBody
import tv.iptv.tun.tviptv.network.apimodels.Request
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.storage.cacheCookie
import tv.iptv.tun.tviptv.utils.buildCookie
import tv.iptv.tun.tviptv.utils.findFirstNumber
import tv.iptv.tun.tviptv.utils.getString

class VtcBackupDataSourceImpl(
    private val client: HttpClientManager = HttpClientManager,
    private val sharePreference: IKeyValueStorage = IKeyValueStorage,
) : AbsChildTVDataSource() {
    private val cookies: MutableMap<String, String>
    private val config: ChannelSourceConfig by lazy {
        ChannelSourceConfig(
            baseUrl = "https://vtc.gov.vn/",
            mainPagePath = "live",
            getLinkStreamPath = "StreamChannelPlayer/GetProtectedStreamUrl",
        )
    }

    init {
        val cacheCookie = sharePreference.cacheCookie(SourceFrom.VTC_BACKUP)
        cookies = cacheCookie?.toMutableMap() ?: mutableMapOf()
    }

    private suspend fun getTvLinkFromDetail(tvChannel: ChannelDTO): ChannelStreamLink {

        val realId = tvChannel.channelId.findFirstNumber()
        val channelId = try {
            if (realId!!.toInt() == 16) {
                "15"
            } else {
                realId
            }
        } catch (e: Exception) {
            realId
        }


        val m3u8Url = getPlaylistM3u8ById(channelId)
        return ChannelStreamLink.wrap(
            channelDTO = tvChannel,
            playUrl = m3u8Url,
            playUrlSourceFrom = SourceFrom.VTC_BACKUP.name,
            playerConfig = PlayerConfig(
                dataSourceType = "hls",
                origin = config.baseUrl,
                referer = config.baseUrl
            )
        )
    }

    private suspend fun getPlaylistM3u8ById(id: String?): String {
        id ?: throw Throwable("Channel id is null")
        val requestBody = FormBody.Builder()
            .add("streamID", id)
            .build()
        val realUrl =
            "${config.baseUrl.removeSuffix("/")}/${config.getLinkStreamPath!!.removeSuffix("/")}"
        val request = Request.Builder()
            .url(realUrl)
            .addHeader("Origin", config.baseUrl.removeSuffix("/"))
            .addHeader("Referer", config.baseUrl)
            .addHeader("User-Agent", Constants.USER_AGENT)
            .addHeader("Cookie", cookies.buildCookie())
            .post(requestBody)
            .build()

        val m3u8Url = client.newCall(request).data.getString("StreamUrl")

        return m3u8Url ?: throw Throwable("Cannot get stream url")
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = flow {
        val streamLink = getTvLinkFromDetail(channelDTO)
        emit(Result.success(listOf(streamLink)))
    }
}
