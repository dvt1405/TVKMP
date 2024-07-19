package tv.iptv.tun.tviptv.repository.tvdatasourceimpl

import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.isActive
import kotlinx.serialization.builtins.ListSerializer
import tv.iptv.tun.tviptv.exceptions.EmptyDataException
import tv.iptv.tun.tviptv.exceptions.RemoteEmptyDataException
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.repository.ITVDataSource
import tv.iptv.tun.tviptv.repository.firebase.RemoteConfigWrapper
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.utils.AssetReader
import tv.iptv.tun.tviptv.utils.JsonUtil
import tv.iptv.tun.tviptv.utils.Logger
import tv.iptv.tun.tviptv.utils.d
import tv.iptv.tun.tviptv.utils.e

open class MainDataSource(
    private val _mapDataSource: TVDataSource.Factory = TVDataSource.Factory,
    private val _sharedPreference: IKeyValueStorage = IKeyValueStorage,
    private val _remoteConfig: RemoteConfigWrapper = RemoteConfigWrapper
) : ITVDataSource {

    override suspend fun getChannelList(
        refreshData: Boolean
    ): Flow<Result<List<ChannelDTO>>> = flow {
        if (refreshData) {
            emit(Result.success(getListDataFromRemoteConfig()))
        } else {
            emit(Result.success(getListDataFromSharedPreference()))
        }
    }.retryWhen { cause, attempt ->
        val isIoException = cause is IOException || cause is EmptyDataException
        attempt < 3 && isIoException
    }.catch {
        when (it) {
            is RemoteEmptyDataException -> {
                emit(Result.success(getLocalData()))
            }

            is EmptyDataException -> {
                emit(Result.success(getLocalData()))
            }

            else -> {
                emit(Result.failure(it))
            }
        }
    }

    private fun getLocalData(): List<ChannelDTO> {
        val allText = AssetReader.readTextFile("tv_channels_new.json")
        val listSerializer = ListSerializer(ChannelDTO.serializer())
        return JsonUtil.fromJson(allText, listSerializer)
    }

    private fun getListDataFromSharedPreference(): List<ChannelDTO> {
        val tvChannelsStr = _sharedPreference.getString("tv_channels", "")
        if (tvChannelsStr.isNullOrEmpty()) {
            throw EmptyDataException(
                SourceFrom.V,
                "tv_channels is empty"
            )
        }
        return JsonUtil.listFromJson(tvChannelsStr, ChannelDTO.serializer())
    }

    private fun getListDataFromRemoteConfig(): List<ChannelDTO> {
        val tvChannelsStr = _remoteConfig.getString("tv_channels")
        if (tvChannelsStr.isEmpty()) {
            throw RemoteEmptyDataException(
                SourceFrom.V,
                "tv_channels is empty"
            )
        }
        val currentVersion = _remoteConfig.getInt(EXTRA_CURRENT_DB_VERSION)
        Logger.d(TAG, "SaveDBVersion", message = "{currentVersion: $currentVersion}")
        _sharedPreference.putInt(EXTRA_CURRENT_DB_VERSION, currentVersion)
        _sharedPreference.putString("tv_channels", tvChannelsStr)
        return JsonUtil.listFromJson(tvChannelsStr, ChannelDTO.serializer())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getChannelStreamLink(
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> {
        val priorityList = channelDTO.channelUrls.sortedBy {
            it.priority
        }
        var index = 0

        suspend fun flowGetChannel(): Flow<Result<List<ChannelStreamLink>>> {
            val channelUrl = priorityList[index]
            return getChannelStreamLinkByUrl(channelUrl, channelDTO)
                .flatMapLatest {
                    if (it.isFailure) {
                        throw it.exceptionOrNull() ?: Throwable("Get link stream failed")
                    } else {
                        if (it.getOrNull().isNullOrEmpty()) {
                            index++
                            flowGetChannel()
                        } else {
                            flowOf(it)
                        }
                    }
                }
                .retryWhen { cause, attempt ->
                    cause !is IllegalStateException && attempt < 2
                }
                .catch {
                    if (index < priorityList.size - 1) {
                        index++
                        emitAll(flowGetChannel())
                    } else {
                        emit(Result.failure(it))
                    }
                }
        }

        return flowGetChannel()
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> = channelFlow {
        val sourceFrom = channelUrl.sourceFrom
        val childRepository: ITVDataSource = _mapDataSource.create(SourceFrom.valueOf(sourceFrom))
        childRepository.getChannelStreamLinkByUrl(channelUrl, channelDTO).collectLatest {
            channel.trySend(it)
        }
    }.retryWhen { cause, attempt ->
        Logger.e(this@MainDataSource, message = cause.message ?: "Error in retry: $attempt")
        attempt < 2 && currentCoroutineContext().isActive
                && cause !is IOException
                && cause !is IllegalStateException
                && cause !is NoSuchElementException
    }.catch {
        emit(Result.failure(it))
    }

    companion object : MainDataSource() {
        private const val EXTRA_CURRENT_DB_VERSION = "current_store_version"
        private const val TAG = "MainDataSource"
        const val CACHE_REPO = "extra:cache_repo"
    }
}


