package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.network.apimodels.FormBody
import tv.iptv.tun.tviptv.network.apimodels.Request
import tv.iptv.tun.tviptv.utils.d
import tv.iptv.tun.tviptv.utils.HtmlParser
import tv.iptv.tun.tviptv.utils.JsonUtil
import tv.iptv.tun.tviptv.utils.Logger
import tv.iptv.tun.tviptv.utils.getBaseUrl
import tv.iptv.tun.tviptv.utils.getJsonObject
import tv.iptv.tun.tviptv.utils.getOrigin
import tv.iptv.tun.tviptv.utils.getString
import tv.iptv.tun.tviptv.utils.htmlParser
import tv.iptv.tun.tviptv.utils.optJSONObject
import tv.iptv.tun.tviptv.utils.optString

class VDataSourceImpl(
    private val _httpClient: HttpClientManager = HttpClientManager,
    private val _htmlParser: HtmlParser = htmlParser
) : AbsChildTVDataSource() {

    override suspend fun getChannelList(refreshData: Boolean): Flow<Result<List<ChannelDTO>>> {
        return flowOf(Result.failure(Throwable("Child repository do not implement this method")))
    }

    override suspend fun getChannelStreamLink(channelDTO: ChannelDTO): Flow<Result<List<ChannelStreamLink>>> {
        return flowOf(
            Result.failure(Throwable("Child repository do not implement this method"))
        )
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = channelFlow {
        if (channelDTO.channelCategoryId == "VOV" || channelDTO.channelCategoryId == "VOH") {
            channel.trySend(
                Result.success(
                    listOf(
                        mapToChannelStreamLink(
                            channelUrl,
                            channelDTO,
                            channelUrl.url,
                            if (channelUrl.url.contains(".m3u8")) {
                                PlayerConfig.Type.HLS
                            } else if (channelUrl.url.contains(".mpd")) {
                                PlayerConfig.Type.DASH
                            } else {
                                PlayerConfig.Type.DEFAULT
                            }
                        )
                    )
                )
            )
            return@channelFlow
        }
        val url = channelUrl.url

        val htmlBody = _httpClient.newCall(
            Request.Builder()
                .url(url)
                .header("referer", url)
                .header("origin", url.getBaseUrl())
                .build(),
            String.serializer()
        )
        Logger.d(
            this@VDataSourceImpl, "getChannelStreamLinkByUrl",
            "channelDTO: $channelDTO," +
                    "channelUrl: $channelUrl"
        )
        _htmlParser.getElementById(htmlBody.data, "__NEXT_DATA__")?.let {
            val jsonObject = JsonUtil.fromJson(it, JsonObject.serializer())

            val token = jsonObject.getJsonObject("props")
                ?.getJsonObject("initialState")
                ?.getJsonObject("App")
                ?.getString("token")
                ?.also {
                    Logger.d(this@VDataSourceImpl, message = "token: $it")
                }

            val listStreamLink = getTVLinkStream(
                token = token ?: "",
                channelUrl = channelUrl,
                channelDTO = channelDTO,
                slug = channelUrl.url.removePrefix("https://vieon.vn")
            )

            val playerConfig = PlayerConfig(
                dataSourceType = "hls",
                referer = url,
                origin = url
            )

            Logger.d(this@VDataSourceImpl, "GetStreamSuccess", "${listStreamLink.size}")
            if (listStreamLink.size == 0) {
                throw IllegalStateException("Empty channel")
            } else {
                channel.trySend(Result.success(listStreamLink))
            }
        }
    }.catch {
        emit(Result.failure(it))
    }

    private suspend fun getTVLinkStream(
        token: String,
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO,
        slug: String
    ): MutableList<ChannelStreamLink> {
        val baseUrl = API_URL
        val request = Request.Builder()
            .url(baseUrl)
            .header("Accept", "application/json, text/plain, */*")
            .header(
                "Accept-Language",
                "vi-VN,vi;q=0.9,fr-FR;q=0.8,fr;q=0.7,en-US;q=0.6,en;q=0.5,am;q=0.4,en-AU;q=0.3"
            )
            .header("Authorization", token)
            .header("Connection", "keep-alive")
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Origin", "https://vieon.vn")
            .header("Referer", channelUrl.url)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Mobile Safari/537.36"
            )
            .post(
                FormBody.Builder()
                    .add(
                        "livetv_slug",
                        slug.trim().takeIf { it.isNotEmpty() }
                            ?: channelUrl.url.removePrefix("https://vieon.vn")
                    )
                    .add("platform", "web")
                    .add("ui", "012021")
                    .build()
            )
        val response = _httpClient.newCall(request.build(), String.serializer()).data
        val listStreamLink = mutableListOf<ChannelStreamLink>()
        val json = JsonUtil.fromJson(response, JsonObject.serializer())

        json.getString("hls_link_play", "")?.trim().takeIf {
            !it.isNullOrEmpty()
        }?.let {
            mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.HLS)
        }?.also {
            listStreamLink.add(it)
        }

        json.getString("dash_link_play", "")?.trim().takeIf {
            !it.isNullOrEmpty()
        }?.let {
            mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.DASH)
        }?.also {
            listStreamLink.add(it)
        }

        json.getString("link_play")?.trim().takeIf {
            !it.isNullOrEmpty()
        }?.let {
            if (it.contains(".m3u8")) {
                mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.HLS)
            } else if (it.contains(".mpd")) {
                mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.DASH)
            } else {
                mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.DEFAULT)
            }
        }?.also {
            listStreamLink.add(it)
        }

        json.getJsonObject("play_links")
            ?.getJsonObject("h264")
            ?.let { playLinksJs ->
                playLinksJs.getString("hls")?.trim().takeIf {
                    !it.isNullOrEmpty()
                }?.let {
                    mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.HLS)
                }?.also {
                    listStreamLink.add(it)
                }
                playLinksJs.getString("dash")?.trim().takeIf {
                    !it.isNullOrEmpty()
                }?.let {
                    mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.DASH)
                }?.also {
                    listStreamLink.add(it)
                }
            }

        json.optJSONObject("play_links")
            ?.optJSONObject("h265")
            ?.let {
                it.optString("hls").trim().takeIf {
                    it.isNotEmpty()
                }?.let {
                    mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.HLS)
                }?.also {
                    listStreamLink.add(it)
                }
                it.optString("dash").trim().takeIf {
                    it.isNotEmpty()
                }?.let {
                    mapToChannelStreamLink(channelUrl, channelDTO, it, PlayerConfig.Type.DASH)
                }?.also {
                    listStreamLink.add(it)
                }
            }
        return listStreamLink
    }

    private fun mapToChannelStreamLink(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO,
        it: String,
        type: PlayerConfig.Type = PlayerConfig.Type.HLS
    ): ChannelStreamLink {
        val playerConfig = PlayerConfig(
            dataSourceType = type.value,
            referer = channelUrl.url,
            origin = channelUrl.url.getOrigin()
        )
        return ChannelStreamLink.wrap(
            channelDTO,
            it,
            channelUrl.sourceFrom,
            playerConfig
        )
    }

    /// time_stamp_exp = time_stamp + 48 * HOUR
    /// time_stamp = System.currentTimeMillis() / 1000
    private val jwtDefault by lazy {
        "{\n" +
                "  \"exp\": {time_stamp_exp},\n" +
                "  \"jti\": \"$JTI\",\n" +
                "  \"aud\": \"\",\n" +
                "  \"iat\": {time_stamp},\n" +
                "  \"iss\": \"VieOn\",\n" +
                "  \"nbf\": {time_stamp},\n" +
                "  \"sub\": \"anonymous_$RANDOM_ID-$RANDOM_ID_2-{time_stamp}\",\n" +
                "  \"scope\": \"$DEFAULT_SCOPE\",\n" +
                "  \"di\": \"$RANDOM_ID-$RANDOM_ID_2-{time_stamp}\",\n" +
                "  \"ua\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36\",\n" +
                "  \"dt\": \"web\",\n" +
                "  \"mth\": \"anonymous_login\",\n" +
                "  \"md\": \"\",\n" +
                "  \"ispre\": 0,\n" +
                "  \"version\": \"\"\n" +
                "}"
    }

    companion object {
        private const val HOUR = 60 * 60
        private const val RANDOM_ID = "{random_id}" // 20a0d9d2ebff609035fc1da808d99a64
        private const val RANDOM_ID_2 = "{random_id_2}" // 7d5b16a43d3a24dc379e47d2dd57bfe2
        private const val JTI = "{jti}" //6965615484c9a71c91a04a7f59c21c2a
        private const val DEFAULT_SCOPE = "cm:read cas:read cas:write billing:read"
        private const val API_URL = "https://api.vieon.vn/backend/cm/v5/slug/livetv/detail?platform=web&ui=012021"
    }
}