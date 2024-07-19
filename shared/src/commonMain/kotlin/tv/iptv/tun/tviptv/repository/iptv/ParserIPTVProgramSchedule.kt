package tv.iptv.tun.tviptv.repository.iptv

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import tv.iptv.tun.tviptv.Constants
import tv.iptv.tun.tviptv.database.DatabaseQueries
import tv.iptv.tun.tviptv.database.Programme
import tv.iptv.tun.tviptv.database.sqlDriverFactory
import tv.iptv.tun.tviptv.jsonxml.ProgrammeResponseDTO
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.network.HttpClientManager
import tv.iptv.tun.tviptv.repository.firebase.RemoteConfigWrapper
import tv.iptv.tun.tviptv.repository.iptv.model.TVScheduler
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.utils.JsonUtil
import tv.iptv.tun.tviptv.utils.Logger
import tv.iptv.tun.tviptv.utils.d
import tv.iptv.tun.tviptv.utils.e
import tv.iptv.tun.tviptv.utils.formatCurrentTime
import tv.iptv.tun.tviptv.utils.getString
import tv.iptv.tun.tviptv.utils.isToday
import tv.iptv.tun.tviptv.utils.removeAllSpecialChars
import tv.iptv.tun.tviptv.utils.toFlow
import tv.iptv.tun.tviptv.utils.vietnamTimeZone
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ParserIPTVProgramSchedule constructor(
    private val client: HttpClientManager = HttpClientManager,
    private val storage: IKeyValueStorage = IKeyValueStorage,
    private val databaseQueries: DatabaseQueries = DatabaseQueries(sqlDriverFactory),
    private val firebaseRemoteConfig: RemoteConfigWrapper = RemoteConfigWrapper,
) : CoroutineScope {
    private val pendingSource by lazy {
        mutableMapOf<String, Flow<*>>()
    }

    private val pendingSourceStatus by lazy {
        mutableMapOf<String, PendingSourceStatus>()
    }
    private val mappingEpgId by lazy {
        mutableMapOf<String, String>()
    }

    fun getRelatedProgram(channel: ChannelDTO) =
        getMappingEpgChannelId()[channel.channelId]?.split("|")?.map { newId ->
            getListProgramForTVChannel(newId, true)
                .map {
                    it.map {
                        TVScheduler.Programme(
                            channel = channel.channelId,
                            channelNumber = it.channelNumber,
                            start = it.start,
                            stop = it.stop,
                            title = it.title,
                            description = it.description,
                            extensionsConfigId = it.extensionsConfigId,
                            extensionEpgUrl = it.extensionEpgUrl
                        )
                    }
                }
        }?.merge()

    private fun getMappingEpgChannelId(): Map<String, String> {
        try {
            if (mappingEpgId.isNotEmpty()) return mappingEpgId
            val remoteMapping = firebaseRemoteConfig.getString("tv_epg_mapping")
            val jsonArr = JsonUtil.fromJson(remoteMapping, JsonArray.serializer())
            Logger.d(
                this@ParserIPTVProgramSchedule,
                message = "{\"RemoteMapping\": $remoteMapping}"
            )
            for (i in 0 until jsonArr.size) {
                val key = jsonArr[i].jsonObject.getString("key") ?: continue
                val value = jsonArr[i].jsonObject.getString("value") ?: continue
                mappingEpgId[key] = value
            }
            if (mappingEpgId.isNotEmpty()) {
                return mappingEpgId
            }
        } catch (_: Exception) {
        }
        return Constants.mapping
    }

    private enum class PendingSourceStatus {
        PENDING,
        RUNNING,
        DONE,
        ERROR
    }

    fun getListProgramForExtensionsChannel(
        channel: ExtensionsChannel
    ): Flow<List<TVScheduler.Programme>> {
        return flowOf(getListProgramForChannel(channel.channelId, true))
    }

    private fun getListProgramForTVChannel(
        tvChannelId: String,
        useAbsoluteId: Boolean = false
    ): Flow<List<TVScheduler.Programme>> {
        return flowOf(getListProgramForChannel(tvChannelId, useAbsoluteId))
    }

    private fun getListProgramForChannel(
        channelId: String,
        useAbsoluteId: Boolean
    ): List<TVScheduler.Programme> {
        val queryChannelId = if (useAbsoluteId) {
            channelId
        } else {
            channelId.removeAllSpecialChars()
                .removePrefix("viechannel")
        }
        return (if (useAbsoluteId) {
            databaseQueries.queryProgrammeByID(queryChannelId)
                .executeAsList()
        } else {
            databaseQueries.queryProgrammeRelatedChannelName(queryChannelId)
                .executeAsList()
        }).map {
            TVScheduler.Programme(
                channel = it.channel ?: "",
                channelNumber = it.channelNumber ?: "",
                extensionsConfigId = it.extensionsConfigId ?: "",
                title = it.title ?: "",
                description = it.description ?: "",
                start = it.start ?: "",
                stop = it.stop ?: "",
                extensionEpgUrl = it.extensionEpgUrl ?: ""
            )
        }
    }

    fun getCurrentProgramForTVChannel(
        channelId: String
    ): Flow<TVScheduler.Programme> {
        return getCurrentProgramForChannel(
            channelId,
            useAbsoluteId = false,
            filterTimestamp = true
        )
    }

    fun getCurrentProgramForExtensionChannel(
        channel: ExtensionsChannel,
        configType: IPTVSourceConfig.Type
    ): Flow<TVScheduler.Programme> {
        return getCurrentProgramForChannel(
            channel.channelId,
            useAbsoluteId = true,
            filterTimestamp = configType == IPTVSourceConfig.Type.TV_CHANNEL
        )
    }

    private fun getCurrentProgramForChannel(
        tvChannelId: String,
        useAbsoluteId: Boolean,
        filterTimestamp: Boolean
    ): Flow<TVScheduler.Programme> {
        val currentTime: Long = Clock.System
            .now()
            .toEpochMilliseconds()
        return getListProgramForChannel(tvChannelId, useAbsoluteId).filter {
            if (filterTimestamp) {
                val start: Long = if (it.start.trim() == "+0700") {
                    firstTimeOfDay()
                } else {
                    it.start.formatCurrentTime()
                }

                val stop: Long = if (it.stop.trim() == "+0700") {
                    lastTimeOfDay()
                } else {
                    it.stop.formatCurrentTime()
                }
                if (!start.isToday() && !stop.isToday()) return@filter false
                currentTime in start..stop
            } else {
                true
            }
        }.toFlow()
    }

    private fun lastTimeOfDay() = Clock.System
        .now()
        .plus(1.toDuration(DurationUnit.DAYS))
        .toLocalDateTime(vietnamTimeZone)
        .let {
            LocalDateTime(
                it.year, it.month, it.dayOfMonth,
                0, 0, 0
            ).toInstant(vietnamTimeZone)
        }.toEpochMilliseconds()

    private fun firstTimeOfDay() = Clock.System
        .now()
        .toLocalDateTime(vietnamTimeZone)
        .let {
            LocalDateTime(it.year, it.month, it.dayOfMonth, 0, 0, 0)
                .toInstant(vietnamTimeZone)
        }
        .toEpochMilliseconds()

    suspend fun parseForConfig(config: IPTVSourceConfig) {
        databaseQueries.getAllByExtensionlId(config.sourceUrl)
            .executeAsList()
            .toFlow()
//            .transform<Programme> {
//                parseForConfig(config, it.extensionEpgUrl)
//            }
            .onCompletion {

            }
            .collectLatest {

            }
    }

    fun runPendingSource() {
        if (pendingSource.isEmpty()) {
            return
        }
        val sources = pendingSource.filter {
            pendingSourceStatus[it.key] == PendingSourceStatus.PENDING
        }.takeIf {
            it.isNotEmpty()
        } ?: return

        var sourceCount = 0
        for ((key, source) in sources.entries) {
            launch {
                source.collectLatest {
                    Logger.d(this@ParserIPTVProgramSchedule, message = "Complete")
                }
            }
            pendingSourceStatus[key] = PendingSourceStatus.RUNNING
            sourceCount++
            if (sourceCount > 3) {
                break
            }
        }
    }

    suspend fun appendParseForConfigTask(config: IPTVSourceConfig, programScheduleUrl: String) {
        programScheduleUrl.split(",")
            .filter {
                it.trim().isNotBlank()
            }
            .forEach { url ->
                if (pendingSource[url] == null ||
                    (pendingSourceStatus[url] != PendingSourceStatus.RUNNING &&
                            pendingSourceStatus[url] != PendingSourceStatus.PENDING)
                ) {
                    pendingSource[url] = parseListTvProgramFlow(config, url)
                    pendingSourceStatus[url] = PendingSourceStatus.PENDING
                }
            }
    }

    fun parseForConfig(config: IPTVSourceConfig, tvgUrlList: String): Flow<Nothing> {
        return flow {
            tvgUrlList.split(",")
                .filter {
                    it.trim().isNotBlank()
                }.forEach {
                    emit(it)
                }
        }.transform { url ->
            parseListTvProgramFlow(config, url)
        }
    }

    private suspend fun parseListTvProgramFlow(
        config: IPTVSourceConfig,
        programScheduleUrl: String
    ): Flow<List<TVScheduler.Programme>> {
        databaseQueries.deleteProgramByConfig(config.sourceUrl, programScheduleUrl)
        return parseListTvProgram(config, programScheduleUrl)
            .retry(3) {
                Logger.e(
                    this@ParserIPTVProgramSchedule,
                    message = "retry - $programScheduleUrl"
                )
                return@retry it !is CannotRetryThrowable
            }
            .onCompletion {
                Logger.e(
                    this@ParserIPTVProgramSchedule,
                    message = "$programScheduleUrl - Complete insert"
                )
                removePendingSource(programScheduleUrl)
                runPendingSource()
            }.catch {
                Logger.e(
                    this@ParserIPTVProgramSchedule,
                    message = "$programScheduleUrl - Error"
                )
                Logger.e(this@ParserIPTVProgramSchedule, exception = it)
                removePendingSource(programScheduleUrl)
                runPendingSource()
            }.map {
                it.map {
                    TVScheduler.Programme(

                    )
                }
            }
    }

    private fun removePendingSource(programScheduleUrl: String) {
        pendingSource.remove(programScheduleUrl)
        pendingSourceStatus.remove(programScheduleUrl)
    }

    private suspend fun parseListTvProgram(
        config: IPTVSourceConfig,
        programScheduleUrl: String
    ): Flow<List<Programme>> = flow {
        val listProgramme = client.getXML(
            programScheduleUrl,
            ProgrammeResponseDTO.serializer(),
            "Content-Type" to "text/xml"
        ).data.listProgram
            .map {
                Programme(
                    channel = it.channel,
                    channelNumber = it.channelNumber,
                    start = it.start,
                    stop = it.stop,
                    extensionsConfigId = config.sourceUrl,
                    extensionEpgUrl = programScheduleUrl,
                    description = it.desc,
                    title = it.title
                )
            }
        listProgramme.forEach {
            databaseQueries.insertProgrammeObj(it)
        }
        emit(listProgramme)
    }.retry(3)

    suspend fun delete(url: String) {
        databaseQueries.deleteProgramByConfigId(url)
    }


    private class InvalidOrNotFoundUrlThrowable(
        override val message: String? = ""
    ) : CannotRetryThrowable(message) {
    }

    private class InvalidFormatThrowable(
        override val message: String? = ""
    ) : CannotRetryThrowable(message) {
    }

    private open class CannotRetryThrowable(
        override val message: String? = ""
    ) : Throwable(message) {
    }

    fun clearCache() {
        mappingEpgId.clear()
    }

    init {
        instance = this
    }

    companion object {
        private var instance: ParserIPTVProgramSchedule? = null

        fun getInstance(): ParserIPTVProgramSchedule? = instance
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
}