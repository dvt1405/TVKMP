package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelSourceConfig
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.network.apimodels.Request
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.storage.KeyValueStorage
import tv.iptv.tun.tviptv.storage.cacheCookie
import tv.iptv.tun.tviptv.utils.HtmlParser
import tv.iptv.tun.tviptv.utils.createHTMLParser

class VOVDataSourceImpl constructor(
    private val client: HttpClientManager = HttpClientManager,
    private val sharePreference: IKeyValueStorage = KeyValueStorage,
    private val htmlParser: HtmlParser = createHTMLParser()
) : AbsChildTVDataSource() {
    private val cookie: MutableMap<String, String>

    private val config: ChannelSourceConfig by lazy {
        ChannelSourceConfig(
            baseUrl = "http://vovmedia.vn/",
            mainPagePath = "",
            getLinkStreamPath = ""
        )
    }

    init {
        val cacheCookie = sharePreference.cacheCookie(SourceFrom.VOV_BACKUP)
        cookie = cacheCookie?.toMutableMap() ?: mutableMapOf()
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = flow {
        try {
            val connection = client.newCall(
                Request.Builder()
                    .url(channelUrl.url)
                    .build(),
                String.serializer()
            )
            if (!connection.isSuccess) {
                throw connection.throwable
            }
            connection.cookie?.forEach {
                cookie[it.name] = it.value
            }

            val listUrl = mutableListOf<String>()
            htmlParser.getElementByTag(connection.data, "script").forEach { script ->
                val regex = Regex("(?<=url: \").*?(?=\")")
                regex.find(script)?.groups?.get(0)?.value?.let {
                    listUrl.add(it)
                }
            }
            val rs = listUrl.map { url ->
                ChannelStreamLink.wrap(
                    channelDTO,
                    url,
                    SourceFrom.HTV_BACKUP.name,
                    playerConfig = PlayerConfig(
                        dataSourceType = PlayerConfig.Type.HLS.value,
                        origin = config.baseUrl,
                        referer = channelUrl.url
                    )
                )
            }
            emit(Result.success(rs))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.retry(3)
}