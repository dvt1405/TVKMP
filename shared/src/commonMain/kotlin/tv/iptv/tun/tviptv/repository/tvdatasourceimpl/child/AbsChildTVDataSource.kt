package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink
import tv.iptv.tun.tviptv.repository.ITVDataSource

abstract class AbsChildTVDataSource() : ITVDataSource {
    override suspend fun getChannelList(refreshData: Boolean): Flow<Result<List<ChannelDTO>>> {
        throw UnsupportedOperationException("Not yet implemented for child datasource")
    }

    override suspend fun getChannelStreamLink(channelDTO: ChannelDTO): Flow<Result<List<ChannelStreamLink>>> {
        throw UnsupportedOperationException("Not yet implemented for child datasource")
    }
}