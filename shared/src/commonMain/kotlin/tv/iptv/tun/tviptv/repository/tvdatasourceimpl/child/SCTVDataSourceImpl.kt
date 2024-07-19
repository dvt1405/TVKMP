package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import io.ktor.http.Url
import io.ktor.util.logging.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.network.apimodels.NetworkResponse
import tv.iptv.tun.tviptv.repository.firebase.RemoteConfigWrapper
import tv.iptv.tun.tviptv.repository.firebase.getSctvConfig
import tv.iptv.tun.tviptv.utils.getJsonArray
import tv.iptv.tun.tviptv.utils.getJsonObject
import tv.iptv.tun.tviptv.utils.getLogger
import tv.iptv.tun.tviptv.utils.getOrigin
import tv.iptv.tun.tviptv.utils.getString

class SCTVDataSourceImpl(
    private val remoteConfig: RemoteConfigWrapper = RemoteConfigWrapper,
    private val httpClientManager: HttpClientManager = HttpClientManager,
    private val logger: Logger = getLogger("SctvDataSource")
) : AbsChildTVDataSource() {

    private val _config by lazy {
        remoteConfig.getSctvConfig()
    }

    private val _baseUrl by lazy {
        _config?.getString("baseBackendUrl")
    }

    private val _headers by lazy {
        _config?.getJsonObject("headers")?.run {
            val headers = mutableMapOf<String, String>()
            this.keys.forEach { key ->
                this.getString(key)?.let {
                    headers[key] = it
                }
            }
            headers
        } ?: _defaultHeaders
    }

    private val _paths by lazy {
        _cachePathsConfig ?: _config?.getJsonArray("paths")?.let {
            val paths = mutableMapOf<String, SctvPathConfig>()
            for (i in 0 until it.size) {
                val jsonObject = it[i] as? JsonObject ?: continue
                val key = jsonObject.getString("key") ?: continue
                val value = jsonObject.getString("value") ?: continue
                paths[key] = SctvPathConfig(
                    path = value,
                    replacePathOrQueryKeys = jsonObject.getJsonArray("replacePathOrQueryKeys")
                        ?.let { array ->
                            val list = mutableListOf<String>()
                            for (j in 0 until array.size) {
                                list.add(array[i].toString())
                            }
                            list
                        }
                )
            }
            paths
        }?.also {
            _cachePathsConfig = it
        }
    }

    private var _cachePathsConfig: Map<String, SctvPathConfig>? = null

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = flow {
        val channelSlug = try {
            val urlStr = channelDTO.channelUrls.last {
                it.sourceFrom == SourceFrom.SCTV.name
            }.url
            val url = Url(urlStr)
            url.pathSegments.last {
                it.isNotBlank()
            }
        } catch (e: NoSuchElementException) {
            throw e
        }

        val url = buildLinkStreamBackedEndUrl(channelSlug)
        val networkResponse = httpClientManager.get(
            url,
            JsonObject.serializer(),
            *_defaultHeaders
                .map { Pair(it.key, it.value) }
                .toTypedArray()
        )

        if (networkResponse is NetworkResponse.Success) {
            val json = networkResponse.data
            val linkPlay =
                json.getString("link_play") ?: throw NullPointerException("Empty link play")

            val hlsPlayInfo: String? = json.getJsonObject("play_info")
                ?.getJsonObject("dataT")
                ?.getString("hls_link_play")
                ?.takeIf {
                    it != "null" && it.isNotEmpty()
                }

            logger.debug("$linkPlay, $hlsPlayInfo")
            val realChannel = ChannelStreamLink.wrap(
                channelDTO = channelDTO,
                playUrl = linkPlay,
                playUrlSourceFrom = SourceFrom.SCTV.name,
                playerConfig = PlayerConfig(
                    dataSourceType = "hls",
                    referer = WEB_PAGE_BASE_URL,
                    origin = WEB_PAGE_BASE_URL.getOrigin()
                )
            )
            if (hlsPlayInfo.isNullOrEmpty()) {
                emit(Result.success(listOf(realChannel)))
            } else {
                emit(
                    Result.success(
                        listOf(
                            realChannel,
                            ChannelStreamLink.wrap(
                                channelDTO = channelDTO,
                                playUrl = linkPlay,
                                playUrlSourceFrom = SourceFrom.SCTV.name,
                                playerConfig = PlayerConfig(
                                    dataSourceType = "hls",
                                    referer = WEB_PAGE_BASE_URL,
                                    origin = WEB_PAGE_BASE_URL.getOrigin()
                                )
                            )
                        )
                    )
                )
            }
        } else {
            throw (networkResponse as NetworkResponse.Fail).cause

        }
    }.catch {
        emit(Result.failure(it))
    }

    private fun buildLinkStreamBackedEndUrl(channelSlug: String): String {
        val pathConfig = _paths?.get("linkStream")
        val linkStreamPath = pathConfig?.path?.trim()
        if (!linkStreamPath.isNullOrEmpty()) {
            return "$_baseUrl${linkStreamPath.replace("{channel_slug}", channelSlug)}"
        }

        return "$BACKEND_BASE_URL${
            PATH_QUERY_CHANNEL_DETAIL.replace(
                "{channel_slug}",
                channelSlug
            )
        }$SELECT_QUERY_CHANEL_DETAIL_VALUE"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTVChannelPageForMenu(menuId: String = "truyen-hinh-ecb1ec92"): Flow<List<SCTVPages.Ribbon>> {
        return getMainPageMenu(2)
            .map {
                val id = it.first {
                    it.slug == menuId
                }.id
                val url = "$BACKEND_BASE_URL$TENANTS${
                    PATH_QUERY_PAGES_MENU.replace(
                        "{menu_page_id}", id
                    )
                }$SELECT_QUERY_PAGES_FOR_MENU_ID"
                url
            }.filter {
                try {
                    Url(it)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            .mapLatest {
                val response = httpClientManager.get(
                    it,
                    SCTVPages.serializer(),
                    "origin" to "https://sctvonline.vn",
                    "referer" to REFERER
                )
                if (!response.isSuccess) {
                    throw response.throwable
                }
                return@mapLatest response.data.ribbons
            }
    }

    private fun getMainPageMenu(retryCount: Int): Flow<List<SCTVMainMenu.Item>> = flow {
        val mainPageUrl = "$BACKEND_BASE_URL$MAIN_PAGE_MENU"
        val listSerializer = ListSerializer(SCTVMainMenu.Item.serializer())

        val response = httpClientManager.get(
            mainPageUrl,
            listSerializer,
            "origin" to "https://sctvonline.vn",
            "referer" to REFERER
        )

        if (response is NetworkResponse.Success) {
            emit(response.data)
        } else {
            throw (response as NetworkResponse.Fail).cause
        }
    }.retryWhen { cause: Throwable, attempt: Long ->
        return@retryWhen cause.takeUnless { it.message == "canretry" } != null &&
                attempt.toInt() < retryCount
    }

    class SCTVMainMenu {
        @Serializable
        data class Item(
            val banner_style: String,
            val color_one: String,
            val color_two: String,
            val display_style: String,
            val icon: String,
            val id: String,
            val name: String,
            val page_options: PageOptions,
            val required: Boolean,
            val slug: String,
        ) {
            @Serializable
            data class PageOptions(
                val contain_sub_item: Boolean,
                val content_navigation_option: String
            )
        }
    }

    @Serializable
    class SCTVPages(
        val banner_style: String,
        val display_style: String,
        val name: String,
        val page_options: PageOptions,
        val ribbons: List<Ribbon>,
    ) {
        @Serializable
        data class PageOptions(
            val contain_sub_item: Boolean,
            val content_navigation_option: String
        )

        @Serializable
        data class Ribbon(
            val display_type: Int,
            val id: String,
            val is_default_display: Boolean,
            val is_visible_in_ribbon_main_section: Boolean,
            val items: List<Item>,
            val name: String,
            val odr: Int,
            val show_flag_odr: Boolean,
            val slug: String,
            val type: Int
        )

        @Serializable
        data class Item(
            val content_categories: List<ContentCategory>,
            val has_free_content: Boolean,
            val hide_from_top_contents: Boolean,
            val id: String,
            val images: Images,
            val is_new_release: Boolean,
            val is_premium: Boolean,
            val released_episode_count: Int,
            val slug: String,
            val title: String,
            val top_index: Int,
            val total_episodes: Int,
            val type: Int,
            val video_source: Int
        )

        @Serializable
        data class ContentCategory(
            val id: String,
            val name: String,
            val slug: String
        )

        @Serializable
        data class Images(
            val backdrop: String,
            val banner: String,
            val banner_190_67_ratio: String,
            val banner_19_6_ratio: String,
            val banner_movie: String,
            val banner_tv_show: String,
            val channel_logo: String,
            val channel_wide_logo: String,
            val poster: String,
            val poster_banner: String,
            val rectangle_banner: String,
            val thumbnail: String,
            val thumbnail_9_5_ratio: String,
            val title_image: String
        )
    }

    @Serializable
    private data class SctvPathConfig(
        val path: String,
        val replacePathOrQueryKeys: List<String>? = null
    )

    companion object {
        private val _defaultHeaders by lazy {
            mapOf(
                "Origin" to "https://sctvonline.vn",
                "Referer" to "https://sctvonline.vn/",
                "User-Agent" to "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
                "Accept" to "*/*",
                "Accept-Encoding" to "gzip, deflate, br",
                "Accept-Language" to "en-US,en;q=0.9,vi;q=0.8",
                "Connection" to "keep-alive",
                "Sec-Fetch-Dest" to "empty",
                "Sec-Fetch-Mode" to "cors",
                "Sec-Fetch-Site" to "same-site"
            )
        }
        private const val WEB_PAGE_BASE_URL = "https://sctvonline.vn/"
        private const val BACKEND_BASE_URL = "https://apicdn.sctvonline.vn/"
        private const val MAIN_PAGE_MENU = "backend/cm/menu/sctv-mobile/"
        private const val REFERER = "https://sctvonline.vn/"
        private const val TENANTS = "tenants/sctv/"

        private const val PATH_QUERY_PAGES_MENU = "tenant_pages/" +
                "{menu_page_id}/ribbons/" +
                "?apply_filter_for_side_navigation_section=true" +
                "&limit=50&select="
        private const val SELECT_QUERY_PAGES_FOR_MENU_ID =
            "{\"Content\":" +
                    "[\"id\",\"slug\",\"has_free_content\"," +
                    "\"is_new_release\"," +
                    "\"is_premium\",\"has_free_content\",\"content_categories\"," +
                    "\"total_episodes\",\"released_episode_count\",\"images\",\"title\"," +
                    "\"video_source\",\"type\",\"top_index\",\"min_sub_tier\"]," +
                    "\"Banner\":[" +
                    "\"num_first_episode_preview\",\"slug\",\"id\"," +
                    "\"is_premium\",\"type\",\"is_watchable\",\"has_free_content\"," +
                    "\"long_description\",\"short_description\",\"title\"," +
                    "\"has_free_content\",\"images\",\"min_sub_tier\"" +
                    "]," +
                    "\"RibbonDetail\":[" +
                    "\"display_type\",\"id\",\"items\",\"name\",\"odr\",\"show_flag_odr\"," +
                    "\"slug\",\"type\",\"is_visible_in_ribbon_main_section\"," +
                    "\"is_default_display\",\"min_sub_tier\"" +
                    "]}"

        private const val PATH_QUERY_CHANNEL_DETAIL =
            "${TENANTS}contents/{channel_slug}/view?select="
        private const val SELECT_QUERY_CHANEL_DETAIL_VALUE =
            "{\"Content\":[\"current_season\",\"id\",\"slug\"," +
                    "\"is_watchable\",\"progress\",\"youtube_video_id\"," +
                    "\"link_play\",\"play_info\",\"payment_infors\"," +
                    "\"is_favorite\",\"drm_session_info\"]}"
    }

}