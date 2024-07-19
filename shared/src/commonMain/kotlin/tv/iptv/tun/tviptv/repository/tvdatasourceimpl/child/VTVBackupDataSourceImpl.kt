package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.Constants
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelSourceConfig
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.repository.BrowserDataSource
import tv.iptv.tun.tviptv.utils.d
import tv.iptv.tun.tviptv.utils.Logger
import tv.iptv.tun.tviptv.utils.getBoolean
import tv.iptv.tun.tviptv.utils.getCookieForPage
import tv.iptv.tun.tviptv.utils.getHost
import tv.iptv.tun.tviptv.utils.getJsonArray
import tv.iptv.tun.tviptv.utils.htmlParser

class VTVBackupDataSourceImpl(
    private val client: HttpClientManager = HttpClientManager,
    private val browserDataSource: BrowserDataSource = BrowserDataSource(),
) : AbsChildTVDataSource() {

    private val config: ChannelSourceConfig by lazy {
        ChannelSourceConfig(
            baseUrl = "https://vtvgo.vn",
            mainPagePath = "trang-chu.html",
            getLinkStreamPath = "ajax-get-stream"
        )
    }

    private suspend fun getLinkStream(
        channelDTO: ChannelDTO,
        channelUrl: ChannelDTO.ChannelUrl,
    ): List<ChannelStreamLink> {
        val htmlPage = browserDataSource.getHtmlPage(channelUrl.url)
        val script = htmlParser.getElementByTag(htmlPage, "script")
        for (it in script) {
            val html = it.trim()
            if (html.contains("token")) {
                val token: String? = getVarFromHtml("token", html)
                val id = getVarNumberFromHtml("id", html)
                val typeId: String? = getVarFromHtml("type_id", html)
                val time: String? = getVarFromHtml("time", html)
                if (anyNotNull(token, id, typeId, time)) {
                    val genSignedLink = getVarFromHtml2("genSignedLink", html)
                    val startTimeSignedToken = getVarFromHtml2("startTimeSignedToken", html)
                    val endTimeSignedToken = getVarFromHtml2("endTimeSignedToken", html)
                    return getStream(
                        channelDTO,
                        channelUrl,
                        token!!,
                        id!!,
                        typeId!!,
                        time!!,
                        genSignedLink,
                        startTimeSignedToken,
                        endTimeSignedToken
                    )
                }
            }
        }
        throw RetryThrowable("Cannot get any script tag from html page")
    }

    private suspend fun getStream(
        channelDTO: ChannelDTO,
        channelUrl: ChannelDTO.ChannelUrl,
        token: String,
        id: String,
        typeId: String,
        time: String,
        genSignedLink: String?,
        startTimeSignedToken: String?,
        endTimeSignedToken: String?
    ): List<ChannelStreamLink> {
        val url = "${config.baseUrl}/${config.getLinkStreamPath}"
        val response = client.postFormJson(
            url,
            mapOf(
                "type_id" to typeId,
                "id" to id,
                "time" to time,
                "token" to token
            ),
            "Accept" to "*/*",
            "cookie" to buildCookie(),
            "X-Requested-With" to "XMLHttpRequest",
            "Accept-Language" to "en-US,en;q=0.5",
            "origin" to config.baseUrl,
            "referer" to channelUrl.url,
            "user-agent" to Constants.USER_AGENT,
            "Accept-encoding" to "application/json"
        )
        if (!response.isSuccess) {
            if (response.resCode != -1) {
                throw RetryThrowable(
                    "Response code not OK: {responseCode: ${response.resCode}, " +
                            "${response.throwable.message}}"
                )
            } else {
                throw response.throwable
            }
        }
        val jsRes = response.data
        val streamUrl = jsRes.getJsonArray("stream_url")
            ?.takeIf { jsArr ->
                jsArr.size > 0
            }
            ?.let { jsArr ->
                val listStream = mutableListOf<String>()
                for (i in 0 until jsArr.size) {
                    val link = jsArr[i].toString().takeIf {
                        it.isNotEmpty()
                    } ?: continue
                    listStream.add(link)
                }
                listStream
            } ?: throw Throwable("Parse Json fail with response: $jsRes")

        val isDrm = jsRes.getBoolean("is_drm", false)
        val genToken = getAuthToken(
            isDrm, genSignedLink,
            startTimeSignedToken,
            endTimeSignedToken
        )
        val listStream = streamUrl.map {
            ChannelStreamLink.wrap(
                channelDTO = channelDTO,
                playUrl = it,
                playerConfig = PlayerConfig(
                    dataSourceType = PlayerConfig.Type.HLS.value,
                    referer = channelUrl.url,
                    origin = config.baseUrl,
                    host = null,
                    drmExtra = genToken?.let {
                        mutableMapOf(
                            "Authorization" to "Bearer $genToken"
                        )
                    },
                    drmLicenseUrl = "https://live.aes.vnetwork.dev/drm/keyfile",
                    extraHeaders = genToken?.let {
                        mutableMapOf(
                            "Authorization" to "Bearer $genToken"
                        )
                    }
                ),
                playUrlSourceFrom = SourceFrom.VTV_BACKUP.name
            )
        }
        return listStream
    }


    private suspend fun getAuthToken(
        isDrm: Boolean,
        genSignedLink: String?,
        startTimeSignedToken: String?,
        endTimeSignedToken: String?,
        retryTime: Int = 3
    ): String? {
        if (retryTime < 0) {
            return null
        }
        if (!isDrm) {
            return null
        }
        val body = client.post(
            genSignedLink!!,
            "{\"r\":\"Vtvd1g1t@l2@24\",\"stime\":\"$startTimeSignedToken\",\"etime\":\"$endTimeSignedToken\"}",
            String.serializer(),
            "Accept" to "application/json, text/javascript, */*; q=0.01",
            "Accept-Encoding" to "gzip, deflate, br, zstd",
            "Accept-Language" to "en-US,en;q=0.5",
            "Origin" to config.baseUrl,
            "Referer" to config.baseUrl,
            "Host" to genSignedLink.getHost(),
            "User-agent" to Constants.USER_AGENT,
            "Accept-encoding" to "application/json"
        )
        if (!body.isSuccess) {
            getAuthToken(
                isDrm,
                genSignedLink,
                startTimeSignedToken,
                endTimeSignedToken,
                retryTime - 1
            )
        }
        return body.data
    }

    private fun buildCookie(): String {
        return getCookieForPage("https://vtvgo.vn/")
    }

    private fun getVarFromHtml(name: String, text: String): String? {
        val regex = "(?<=var\\s$name\\s=\\s\').*?(?=\')"
        return Regex(regex).find(text)?.groups?.get(0)?.value
    }

    private fun getVarFromHtml2(name: String, text: String): String? {
        return Regex("(?<=var\\s$name\\s=\\s\").*?(?=\")").find(text)?.value
    }

    private fun getVarNumberFromHtml(name: String, text: String): String? {
        return Regex("(?<=var\\s$name\\s=\\s)(\\d+)").find(text)?.value
    }

    private fun anyNotNull(vararg variable: Any?): Boolean {
        for (v in variable) {
            if (v == null) return false
        }
        return true
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = flow {
        Logger.d(this@VTVBackupDataSourceImpl, "", "$channelUrl")
        val result = getLinkStream(channelDTO, channelUrl)
        Logger.d(this@VTVBackupDataSourceImpl, "", "$result")
        emit(Result.success(result))
    }.retryWhen { cause, attempt ->
        delay(2_000)
//        Firebase.crashlytics.recordException(cause)
        attempt < 2 && cause is RetryThrowable
    }.catch {
        emit(Result.failure(it))
    }.onCompletion {
        browserDataSource.setOnDisableWebView()
    }

    internal class RetryThrowable(override val message: String?) : Exception(message)

}

data class VtvStream(
    val ads_tags: String,
    val ads_time: String,
    val channel_name: String,
    val chromecast_url: String,
    val content_id: Int,
    val date: String,
    val geoname_id: String,
    val is_drm: Boolean,
    val player_type: String,
    val remoteip: String,
    val stream_info: String,
    val stream_url: List<String>
)