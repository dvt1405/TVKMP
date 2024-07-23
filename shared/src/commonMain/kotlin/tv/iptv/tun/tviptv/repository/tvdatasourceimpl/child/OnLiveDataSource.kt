package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import io.ktor.http.Cookie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.Constants
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.network.apimodels.FormBody
import tv.iptv.tun.tviptv.network.apimodels.Request
import tv.iptv.tun.tviptv.repository.firebase.RemoteConfigWrapper
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.storage.KeyValueStorage
import tv.iptv.tun.tviptv.utils.getJsonObject
import tv.iptv.tun.tviptv.utils.getOrigin
import tv.iptv.tun.tviptv.utils.getString
import tv.iptv.tun.tviptv.utils.htmlParser

class OnLiveDataSource(
    private val client: HttpClientManager = HttpClientManager,
    private val sharePreference: IKeyValueStorage = KeyValueStorage
) : AbsChildTVDataSource() {
    private val mapSerializable = MapSerializer(String.serializer(), String.serializer())

    private val oldCookies: MutableMap<String, String> by lazy {
        cacheCookie(SourceFrom.OnLive).toMutableMap()
    }

    private fun cacheCookie(sourceFrom: SourceFrom): Map<String, String> {
        return try {
            sharePreference.getObject(
                "${sourceFrom.name}_cookies", mapSerializable
            ) ?: mapOf()
        } catch (e: Exception) {
            mapOf()
        }
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> {
        return flow {
            val livePlayer = getMainPage(channelDTO.channelId)
            redirectToLivePlayer(livePlayer)
            getLive(livePlayer)
            val aid = getAID(livePlayer)
            val linkStream = getLiveStream(livePlayer, aid)
            val channelStreamLink = ChannelStreamLink.wrap(
                channelDTO,
                linkStream,
                SourceFrom.OnLive.name,
                playerConfig = PlayerConfig(
                    dataSourceType = PlayerConfig.Type.HLS.value,
                    referer = channelUrl.url,
                    origin = channelUrl.url.getOrigin()
                )
            )
            emit(Result.success(listOf(channelStreamLink)))
        }.catch {
            emit(Result.failure(it))
        }
    }

    private suspend fun redirectToLivePlayer(livePlayer: LivePlayer) {
        val livePlayerUrl = "https://play.onlive.vn/${livePlayer.szBjId}/${livePlayer.nBroadNo}"
        val res = client.get(livePlayerUrl, String.serializer())
        if (res.isSuccess) {
            res.cookie?.forEach {
                oldCookies[it.name] = it.value
            }
        }
    }


    private suspend fun getMainPage(channelId: String = "joyfmmhz"): LivePlayer {
        val detailPageUrl = "$URL$channelId"
        val res = client.get(detailPageUrl, String.serializer())
        if (res.isSuccess) {
            res.cookie?.forEach {
                oldCookies[it.name] = it.value
            }
            htmlParser.parseWithCssSelector(res.data, "script").forEach {
                if (it.contains("LivePlayer")) {
                    val html = it
                    val szLang = regexExtract(html, "var szLang\\s*=\\s*'([^']*)'")
                    val szLocalCode = regexExtract(html, "var szLocalCode\\s*=\\s*'([^']*)'")
                    val szBjId = regexExtract(html, "var szBjId\\s*=\\s*'([^']*)'")
                    val szBjNick = regexExtract(html, "var szBjNick\\s*=\\s*'([^']*)'")
                    val nBroadNo = regexExtract(html, "var nBroadNo\\s*=\\s*(\\d+);")
                    println("szLang=$szLang, szLocalCode=$szLocalCode, szBjId=$szBjId, szBjNick=$szBjNick, nBroadNo=$nBroadNo")
                    return LivePlayer(szLang, szLocalCode, szBjId, szBjNick, nBroadNo)
                }
            }
        }
        throw IllegalStateException("No Live Player found")
    }

    private suspend fun getLive(livePlayer: LivePlayer): String {
        val url = "https://live.onlive.vn/afreeca/player_live_api.php?bjid=${livePlayer.szBjId}"
        val res = client.postForm(
            url,
            mapOf("bid" to livePlayer.szBjId),
            String.serializer(),
            "Origin" to "https://play.onlive.vn",
            "Referer" to "https://play.onlive.vn/${livePlayer.szBjId}/${livePlayer.nBroadNo}",
            "User-Agent" to Constants.USER_AGENT,
            "Cookie" to getCookieString(),
        )
        if (res.isSuccess) {
            putCookie(res.cookie)
            return res.data
        } else {
            throw res.throwable
        }
    }

    private suspend fun getAID(livePlayer: LivePlayer): String {
        val url = "https://live.onlive.vn/afreeca/player_live_api.php?bjid=${livePlayer.szBjId}"
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .header("Origin", "https://play.onlive.vn")
            .header("Referer", "https://play.onlive.vn/${livePlayer.szBjId}/${livePlayer.nBroadNo}")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36"
            )
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Cookie", getCookieString())
            .post(
                FormBody.Builder()
                    .add("bid", livePlayer.szBjId)
                    .add("bno", livePlayer.nBroadNo)
                    .add("type", "aid") //live, aid
                    .add("pwd", "")
                    .add("player_type", "html5")
                    .add("stream_type", "common")
                    .add("quality", "original") //HD, original
                    .add("mode", "landing")
                    .add("from_api", "0")
                    .build()
            )
            .build()

        val response = client.newCall(request)
        return response.data
            .getJsonObject("CHANNEL")
            ?.getString("AID") ?: ""
    }


    private suspend fun getLiveStream(livePlayer: LivePlayer, aid: String): String {
        val url =
            "https://livestream-manager.onlive.vn/broad_stream_assign.html?return_type=gcp_cdn&use_cors=true&cors_origin_url=play.onlive.vn&broad_key=${livePlayer.nBroadNo}-common-original-hls"
        val res = client.newCall(
            Request.Builder()
                .url(url)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header(
                    "User-Agent",
                    Constants.USER_AGENT
                )
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Cookie", getCookieString())
                .build()
        )

        if (res.isSuccess) {
            putCookie(res.cookie)
        }

        return res.data.getString("view_url")
            .let {
                "$it?aid=$aid"
            }
    }

    private fun putCookie(it: List<Cookie>?) {
        it?.forEach {
            oldCookies[it.name] = it.value
        }
    }


    private fun regexExtract(input: String, regex: String): String {
        val finder = Regex(regex).find(input)?.groups
        return try {
            finder?.get(1)?.value ?: finder?.get(0)?.value
            ?: throw NullPointerException("Null found with regex: $regex, input: $input")
        } catch (e: Exception) {
            ""
        }
    }

    @Serializable
    data class LivePlayer(
        val szLang: String,
        val szLocalCode: String,
        val szBjId: String,
        val szBjNick: String,
        val nBroadNo: String
    )

    private fun getCookieString(): String {
        return oldCookies.mapTo(mutableListOf()) { (k, v) ->
            "$k=$v"
        }.joinToString(";")
            .removeSuffix(";")
    }


    companion object {
        private const val URL = "https://play.onlive.vn/"
    }
}