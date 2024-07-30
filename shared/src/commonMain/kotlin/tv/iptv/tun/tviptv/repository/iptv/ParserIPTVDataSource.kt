package tv.iptv.tun.tviptv.repository.iptv

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.database.DatabaseQueries
import tv.iptv.tun.tviptv.database.sqlDriverFactory
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.repository.firebase.RemoteConfigWrapper
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.storage.KeyValueStorage
import tv.iptv.tun.tviptv.storage.getCurrentIPTVSource
import tv.iptv.tun.tviptv.storage.saveCurrentIPTVSource
import tv.iptv.tun.tviptv.utils.JsonUtil
import tv.iptv.tun.tviptv.utils.Logger
import tv.iptv.tun.tviptv.utils.d

open class ParserIPTVDataSource(
    private val client: HttpClientManager = HttpClientManager,
    private val storage: IKeyValueStorage = KeyValueStorage,
    private val roomDataBase: DatabaseQueries = DatabaseQueries(sqlDriverFactory),
    private val programScheduleParser: ParserIPTVProgramSchedule = ParserIPTVProgramSchedule(),
    private val remoteConfig: RemoteConfigWrapper = RemoteConfigWrapper
) {

    fun currentIPTVSource(): IPTVSourceConfig? {
        var currentIPTV = storage.getCurrentIPTVSource()
        if (currentIPTV == null) {
            val cache = roomDataBase.getAllIPTVSource()
                .executeAsList()
                .firstOrNull()
            if (cache != null) {
                currentIPTV = cache.let {
                    IPTVSourceConfig(
                        sourceUrl = it.sourceUrl,
                        sourceName = it.sourceName ?: "",
                        type = it.type?.let {
                            kotlin.runCatching {
                                IPTVSourceConfig.Type.valueOf(it)
                            }.getOrNull()
                        } ?: IPTVSourceConfig.Type.TV_CHANNEL
                    )
                }
                storage.saveCurrentIPTVSource(currentIPTV)
                return currentIPTV
            }
        }
        return currentIPTV
    }

    suspend fun getIPTVSource(iptvConfig: IPTVSourceConfig): Flow<List<IPTVChannel>> = flow {
        val mapSerializer = MapSerializer(String.serializer(), String.serializer())
        val listIPTVChannel = roomDataBase.queryIPTVChannelByIPTVSource(iptvConfig.sourceUrl)
            .executeAsList()
            .map {
                IPTVChannel(
                    tvGroup = it.tvGroup ?: "",
                    channelId = it.channelId ?: "",
                    catchupSource = it.catchupSource ?: "",
                    sourceFrom = it.sourceFrom ?: "",
                    extensionSourceId = it.extensionSourceId ?: "",
                    tvChannelName = it.tvChannelName ?: "",
                    logoChannel = it.logoChannel ?: "",
                    tvStreamLink = it.tvStreamLink ?: "",
                    userAgent = it.userAgent ?: "",
                    isHls = it.isHls?.toBoolean() ?: false,
                    props = it.props?.let {
                        JsonUtil.fromJson(it, mapSerializer)
                    },
                    referer = it.referer ?: "",
                    channelPreviewProviderId = it.channelPreviewProviderId?.toLong() ?: -1
                )
            }
            .takeIf {
                it.isNotEmpty()
            }

            ?: parseSource(iptvConfig).toList(mutableListOf())
        emit(listIPTVChannel)
    }

    private fun getIntervalRefreshData(configType: IPTVSourceConfig.Type): Int {
        val key = EXTRA_INTERVAL_REFRESH_DATA_KEY + configType.name
        val defaultValue = when (configType) {
            IPTVSourceConfig.Type.TV_CHANNEL -> INTERVAL_REFRESH_DATA_TV_CHANNEL
            IPTVSourceConfig.Type.FOOTBALL -> INTERVAL_REFRESH_DATA_FOOTBALL
            IPTVSourceConfig.Type.MOVIE -> INTERVAL_REFRESH_DATA_MOVIE
        }

        return storage.getInt(key, -1)
            .takeIf {
                it != null && it > -1
            } ?: defaultValue.also {
            storage.putInt(key, it)
        }
    }

    suspend fun parseSource(iptvConfig: IPTVSourceConfig): Flow<IPTVChannel> {
        val sourceFrom = iptvConfig.sourceName
        var iptvChannel: IPTVChannel?
        var channelId = ""
        var channelLogo = ""
        var channelGroup = ""
        var channelName = ""
        var tvCatchupSource = ""
        var userAgent = ""
        var referer = ""
        var channelLink = ""
        var props = mutableMapOf<String, String>()
        var isHls = false
        val mapSerializer = MapSerializer(String.serializer(), String.serializer())
        return client.getLineByLine(iptvConfig.sourceUrl)
            .transform { line ->
                Logger.d(this@ParserIPTVDataSource, "execute", line)
                if (line.trim().isBlank()) {
                    return@transform
                }
                if (line.startsWith(TAG_EXT_INFO) || line.startsWith("EXTINF:")) {
                    iptvChannel = IPTVChannel(
                        tvGroup = channelGroup,
                        logoChannel = channelLogo,
                        tvChannelName = channelName.trim(),
                        tvStreamLink = channelLink,
                        sourceFrom = sourceFrom,
                        channelId = channelId,
                        channelPreviewProviderId = -1,
                        isHls = isHls,
                        extensionSourceId = iptvConfig.sourceUrl,
                        props = props,
                        userAgent = userAgent,
                        catchupSource = tvCatchupSource,
                        referer = referer
                    )

                    if (iptvChannel!!.isValidChannel) {
                        emit(iptvChannel!!)
                    }
                    channelId = ""
                    channelLogo = ""
                    channelGroup = ""
                    channelName = ""
                    tvCatchupSource = ""
                    userAgent = ""
                    referer = ""
                    channelLink = ""
                    isHls = false
                    props = mutableMapOf()
                }

                if (line.contains(URL_TVG_PREFIX)) {
                    programScheduleParser.appendParseForConfigTask(
                        iptvConfig,
                        getByRegex(REGEX_PROGRAM_SCHEDULE_URL, line)
                    )
                }

                if (line.contains(CATCHUP_SOURCE_PREFIX)) {
                    tvCatchupSource = getByRegex(CHANNEL_CATCH_UP_SOURCE_REGEX, line)
                }

                if (line.contains(TAG_USER_AGENT)) {
                    userAgent = getByRegex(REGEX_USER_AGENT, line)
                }

                if (line.contains(TAG_REFERER)) {
                    referer = getByRegex(REFERER_REGEX, line)
                    props[REFERER] = referer
                }

                if (line.removePrefix("#").startsWith("http") && channelLink.isEmpty()) {
                    channelLink = line.trim()
                        .removePrefix("#")
                        .trim()
                        .replace(REGEX_TRIM_END_LINE, "")
                    while (channelLink.contains(TAG_REFERER)) {
                        val refererInChannelLink = getByRegex(REFERER_REGEX, channelLink)
                        val textToReplace = "$TAG_REFERER=$refererInChannelLink"
                        if (channelLink.contains(textToReplace)) {
                            channelLink = channelLink.replace(textToReplace, "")
                                .trim()
                        } else {
                            if (channelLink.contains("|")) {
                                channelLink = channelLink.substring(0, channelLink.indexOf("|"))
                            }
                        }
                        if (DEBUG) {
                            Logger.d(
                                this@ParserIPTVDataSource,
                                "ChannelLink",
                                "Remove referer: $channelLink|$TAG_REFERER=$refererInChannelLink "
                            )
                        }
                    }
                    channelLink = channelLink.trim()
                        .removeSuffix("#")
                        .trim()
                    if (channelLink.contains("|")) {
                        channelLink = channelLink.substring(0, channelLink.indexOf("|"))
                    }
                    if (DEBUG) {
                        Logger.d(this@ParserIPTVDataSource, "ChannelLink", channelLink)
                    }
                }

                when {
                    line.contains(LOGO_PREFIX) || line.contains(ID_PREFIX) || line.contains(
                        TITLE_PREFIX
                    ) -> {
                        if (line.contains(ID_PREFIX)) {
                            channelId = getByRegex(CHANNEL_ID_REGEX, line)
                        }

                        if (line.contains(LOGO_PREFIX)) {
                            channelLogo = getByRegex(CHANNEL_LOGO_REGEX, line)
                        }

                        if (line.contains(TITLE_PREFIX)) {
                            channelGroup = getByRegex(CHANNEL_GROUP_TITLE_REGEX, line)
                        }

                        if (line.contains(TYPE_REGEX)) {
                            TYPE_REGEX.find(line)?.groupValues
                                ?.takeIf {
                                    it.isNotEmpty()
                                }?.let {
                                    when (it[0]) {
                                        "stream" -> {
                                            isHls = true
                                        }
                                    }
                                }
                        }

                        val lastCommaIndex = line.lastIndexOf(",")
                        if (lastCommaIndex >= 0 && lastCommaIndex < line.length) {
                            channelName = line.substring(lastCommaIndex + 1)
                            if (channelName.contains("Tham gia group")) {
                                val index = channelName.indexOf("Tham gia group")
                                if (index > 0) {
                                    channelName = channelName.substring(0, index)
                                        .trim()
                                        .removeSuffix("-")
                                }
                            }
                            if (channelName.contains("Mời bạn tham gia nhóm Zalo")) {
                                val index = channelName.indexOf("Mời bạn tham gia nhóm Zalo")
                                if (index > 0) {
                                    channelName = channelName.substring(0, index)
                                        .trim()
                                        .removeSuffix("-")
                                }
                            }
                        }

                    }

                    line.contains(TAG_EXTVLCOPT) -> {
                        val keyValue = getKeyValueByRegex(REGEX_EXTVLCOPT_PROP_KEY, line)
                        props[keyValue.first] = keyValue.second
                    }

                    line.contains(TAG_KODIPROP) -> {
                        Logger.d(this@ParserIPTVDataSource, TAG_KODIPROP, message = line)
                        val keyValue = getKeyValueByRegex(REGEX_KODI_PROP_KEY, line)
                        props[keyValue.first] = keyValue.second
                    }
                }
            }.onEach {
                roomDataBase.insertIPTVChannel(
                    it.tvGroup,
                    it.logoChannel,
                    it.tvChannelName,
                    it.tvStreamLink,
                    it.sourceFrom,
                    it.channelId,
                    it.channelPreviewProviderId.toString(),
                    it.isHls.toString(),
                    it.catchupSource,
                    it.userAgent,
                    it.referer,
                    it.props?.let {
                        JsonUtil.toJson(it, mapSerializer)
                    },
                    it.extensionSourceId
                )
            }
            .onCompletion {
                println(it)
                programScheduleParser.runPendingSource()
            }.retry(3)
    }


    private fun getByRegex(pattern: Regex, finder: String): String {
        return pattern.find(finder)?.value ?: ""
    }

    private fun getKeyValueByRegex(regex: Regex, finder: String): Pair<String, String> {
        val key = getByRegex(regex, finder)
        val startIndex = finder.indexOf("=")
        val value = finder.substring(startIndex + 1, finder.length)
            .trim()
            .removePrefix("\"")
            .removeSuffix("\r")
            .removeSuffix("\"")
        val realHttpKey = realKeys[key] ?: key
        Logger.d(this@ParserIPTVDataSource, "Extract", "key: $key, value: $value")
        return Pair(realHttpKey, value)
    }

    class ParserIPTVThrowable(
        val canRetry: Boolean,
        override val message: String? = null,
    ) : Throwable(message)

    companion object : ParserIPTVDataSource() {
        private const val DEBUG = false
        private const val EXTRA_INTERVAL_REFRESH_DATA_KEY = "extra:interval_refresh_data"
        private const val INTERVAL_REFRESH_DATA_TV_CHANNEL: Int = 60 * 60 * 1000
        private const val INTERVAL_REFRESH_DATA_MOVIE: Int = 24 * 60 * 60 * 1000
        private const val INTERVAL_REFRESH_DATA_FOOTBALL: Int = 15 * 60 * 1000
        private const val OFFSET_TIME = 2 * 60 * 60 * 1000
        private const val MINIMUM_ITEM_COUNT_TO_SAVE = 100
        private const val EXTRA_EXTENSIONS_KEY = "extra:extensions_key"
        private const val TAG_START = "#EXTM3U"
        private const val TAG_EXT_INFO = "#EXTINF:"
        private const val TAG_REFERER = "|Referer"
        private val REGEX_MEDIA_DURATION = Regex("#EXTINF:( )?-?\\d+")
        private val REGEX_MEDIA_DURATION_2 = Regex("EXTINF:( )?-?\\d+")
        private const val URL_TVG_PREFIX = "url-tvg"
        private const val CACHE_PREFIX = "cache"
        private const val RATIO_PREFIX = "aspect-ratio"
        private const val DEINTERLACE_PREFIX = "deinterlace"
        private const val TVG_SHIFT_PREFIX = "tvg-shift"
        private const val M3U_AUTO_LOAD_PREFIX = "m3uautoload"
        private const val CATCHUP_SOURCE_PREFIX = "catchup-source"
        private const val TAG_USER_AGENT = "user-agent"
        private const val TAG_KODIPROP = "KODIPROP"
        private const val TAG_EXTVLCOPT = "EXTVLCOPT"
        private val REGEX_USER_AGENT = Regex("(?<=user-agent=\").*?(?=\")")

        private const val ID_PREFIX = "tvg-id"
        private const val LOGO_PREFIX = "tvg-logo"
        private const val TITLE_PREFIX = "group-title"
        private const val TYPE_PREFIX = "type"

        private val REGEX_TRIM_END_LINE = Regex("[\t\b\r ]")
        private val URL_TVG_REGEX = Regex("(?<=url-tvg=\").*?(?=\")")
        private val CACHE_REGEX = Regex("(?<=cache=).*?(?= )")
        private val DEINTERLACE_REGEX = Regex("(?<=deinterlace=).*?(?= )")
        private val RATIO_REGEX = Regex("(?<=aspect-ratio=).*?(?= )")
        private val TVG_SHIFT_REGEX = Regex("(?<=tvg-shift=).*?(?= )")
        private val M3U_AUTO_REGEX = Regex("(?<=m3uautoload=).*?(?= )")
        private val CHANNEL_ID_REGEX = Regex("(?<=tvg-id=\").*?(?=\")")
        private val CHANNEL_LOGO_REGEX = Regex("(?<=tvg-logo=\").*?(?=\")")
        private val CHANNEL_GROUP_TITLE_REGEX = Regex("(?<=group-title=\").*?(?=\")")
        private val TYPE_REGEX = Regex("(?<=type=\").*?(?=\")")
        private val CHANNEL_CATCH_UP_SOURCE_REGEX = Regex("(?<=catchup-source=\").*?(?=\")")
        private val REFERER_REGEX = Regex("(?<=\\|Referer=).*")
        private val CHANNEL_TYPE_REGEX = Regex("(?<=type=\").*?(?=\")")
        private val CHANNEL_TITLE_REGEX = Regex("(?<=\").*?(?=\")")
        private val REGEX_KODI_PROP_KEY = Regex("(?<=KODIPROP:).*?(?==)")
        private val REGEX_EXTVLCOPT_PROP_KEY = Regex("(?<=EXTVLCOPT:).*?(?==)")
        private val REGEX_PROGRAM_SCHEDULE_URL = Regex("(?<=url-tvg=\").*?(?=\")")
        private val realKeys = mapOf(
            "http-referrer" to "referer",
            "http-user-agent" to "user-agent"
        )
        private const val REFERER = "referer"

        val filmData = mapOf(
            "Phim lẻ TVHay" to "http://hqth.me/tvhayphimle",
            "Phim lẻ FPTPlay" to "http://hqth.me/jsfptphimle",
            "Phim bộ" to "http://hqth.me/phimbo",
            "Phim miễn phí" to "https://hqth.me/phimfree",
            "Film" to "https://gg.gg/films24",
        )

        val mapBongDa: Map<String, String> = mapOf(
            "Bóng đá" to "http://gg.gg/SN-90phut",
        )

        val tvChannel: Map<String, String> by lazy {
            mapOf(
                "K+" to "https://s.id/nhamng",
                "VThanhTV" to "http://vthanhtivi.pw",
            )
        }

        private fun Map<String, String>.mapToListExConfig(type: IPTVSourceConfig.Type) = map {
            IPTVSourceConfig(
                sourceName = it.key,
                sourceUrl = it.value,
                type = type
            )
        }
    }

}