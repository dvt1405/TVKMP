package tv.iptv.tun.tviptv.repository

import kotlinx.coroutines.flow.Flow
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink

interface ITVDataSource {

    suspend fun getChannelList(refreshData: Boolean = true): Flow<Result<List<ChannelDTO>>>
    suspend fun getChannelStreamLink(channelDTO: ChannelDTO): Flow<Result<List<ChannelStreamLink>>>
    suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>>

}