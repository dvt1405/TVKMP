package tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child

import kotlinx.coroutines.flow.Flow
import tv.iptv.tun.tviptv.models.ChannelDTO
import tv.iptv.tun.tviptv.models.ChannelStreamLink

class MyTVDataSource : AbsChildTVDataSource() {
    override suspend fun getChannelStreamLinkByUrl(
        channelUrl: ChannelDTO.ChannelUrl,
        channelDTO: ChannelDTO
    ): Flow<Result<List<ChannelStreamLink>>> {
        TODO("Not yet implemented")
    }
}