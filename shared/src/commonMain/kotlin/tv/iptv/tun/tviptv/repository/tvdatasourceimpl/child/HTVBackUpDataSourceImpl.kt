package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelSourceConfig
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.utils.htmlParser
import tv.iptv.tun.tviptv.utils.toUrlOrNull

open class HTVBackUpDataSourceImpl(
    private val client: HttpClientManager = HttpClientManager,
    private val sharePreference: IKeyValueStorage = IKeyValueStorage
) : AbsChildTVDataSource() {

    private val config: ChannelSourceConfig by lazy {
        ChannelSourceConfig(
            baseUrl = "https://hplus.com.vn/",
            mainPagePath = "tivi-online/",
            getLinkStreamPath = "content/getlinkvideo/",
        )
    }

    private suspend fun getLinkStream(
        link: String,
        channelUrl: ChannelDTO.ChannelUrl
    ): String {
        val res = client.postForm(
            "${config.baseUrl}${config.getLinkStreamPath}",
            mapOf(
                "url" to link,
                "type" to "1",
                "is_mobile" to "1",
                "csrf_test_name" to "",
            ),
            String.serializer(),
            "Origin" to config.baseUrl,
            "Referer" to channelUrl.url
        )
        return res.data.trim().replace("https //", "https://")
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = flow {
        val body = client.get(channelUrl.url, String.serializer()).data
        var link: String? = null
        val listScript = htmlParser.getElementByTag(body, "script")
        for (script in listScript) {
            if (script.contains("var link_stream = iosUrl")) {
                val regex = "(?<=var\\slink_stream\\s=\\siosUrl\\s=).*?(\".*?\")"
                link = Regex(regex).find(script)
                    ?.groups
                    ?.get(0)
                    ?.value
                    ?.trim()
                    ?.removeSuffix("\"")
                    ?.removePrefix("\"")
                if (link != null && link.toUrlOrNull() != null) {
                    break
                }
            }
        }
        if (link == null) {
            emit(Result.failure(Throwable("Cannot get stream link")))
        } else {
            val result = kotlin.runCatching {
                val url = getLinkStream(link, channelUrl)
                ChannelStreamLink.wrap(
                    channelDTO,
                    url,
                    SourceFrom.HTV_BACKUP.name,
                    playerConfig = PlayerConfig(
                        dataSourceType = PlayerConfig.Type.HLS.value,
                        origin = config.baseUrl,
                        referer = channelUrl.url
                    )
                ).let {
                    listOf(it)
                }
            }
            emit(result)
        }
    }

    companion object : HTVBackUpDataSourceImpl() {}
}