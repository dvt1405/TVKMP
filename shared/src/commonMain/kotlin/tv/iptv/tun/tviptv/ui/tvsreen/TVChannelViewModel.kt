package tv.iptv.tun.tviptv.ui.tvsreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.iptv.tun.tviptv.repository.tvdatasourceimpl.MainDataSource

class TVChannelViewModel(
    private val mainDataSource: MainDataSource = MainDataSource
) : ViewModel() {



    init {
        viewModelScope.launch(Dispatchers.IO) {
            mainDataSource.getChannelList(true)
                .stateIn(viewModelScope)
        }
    }

}