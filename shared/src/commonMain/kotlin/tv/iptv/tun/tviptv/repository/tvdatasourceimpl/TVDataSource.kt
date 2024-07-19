package tv.iptv.tun.tviptv.repository.tvdatasourceimpl

import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.repository.ITVDataSource
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.HTVBackUpDataSourceImpl
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.MyTVDataSource
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.OnLiveDataSource
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.ReadyStreamingDataSourceImpl
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.SCTVDataSourceImpl
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.VDataSourceImpl
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.VTVBackupDataSourceImpl
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.child.VtcBackupDataSourceImpl

object TVDataSource {
    open class Factory {
        private val _cacheDataSource by lazy {
            mutableMapOf<SourceFrom, ITVDataSource>()
        }

        fun create(sourceFrom: SourceFrom? = null): ITVDataSource {
            if (_cacheDataSource[sourceFrom] != null) {
                return _cacheDataSource[sourceFrom]!!
            }
            return when (sourceFrom) {
                SourceFrom.HTV_BACKUP -> HTVBackUpDataSourceImpl()
                SourceFrom.V -> VDataSourceImpl()
                SourceFrom.VTV_BACKUP -> VTVBackupDataSourceImpl()
                SourceFrom.VTC_BACKUP -> VtcBackupDataSourceImpl()
                SourceFrom.SCTV -> SCTVDataSourceImpl()
                SourceFrom.OnLive -> OnLiveDataSource()
                SourceFrom.Streaming -> ReadyStreamingDataSourceImpl()
                SourceFrom.MyTV -> MyTVDataSource()
                else -> MainDataSource
            }.also { dataSource ->
                sourceFrom?.let {
                    _cacheDataSource[it] = dataSource
                }
            }
        }

        companion object : Factory()
    }
}