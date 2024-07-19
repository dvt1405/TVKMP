package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.models.PlayerConfig
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.repository.ITVDataSource
import tv.iptv.tun.tviptv.utils.getBaseUrl

class ReadyStreamingDataSourceImpl() : ITVDataSource {
    override suspend fun getChannelList(refreshData: Boolean): Flow<Result<List<ChannelDTO>>> {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override suspend fun getChannelStreamLink(channelDTO: ChannelDTO): Flow<Result<List<ChannelStreamLink>>> {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> {
        return flowOf(Result.success(
            listOf(ChannelStreamLink.wrap(
                channelDTO = channelDTO,
                playUrl = channelUrl.url,
                playUrlSourceFrom = SourceFrom.Streaming.name,
                playerConfig = PlayerConfig(
                    "hls",
                    channelUrl.url,
                    channelUrl.url.getBaseUrl(),
                )
            ))
        ))
    }
}