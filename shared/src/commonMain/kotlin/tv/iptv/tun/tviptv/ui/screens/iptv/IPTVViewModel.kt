package tv.iptv.tun.tviptv.ui.screens.iptv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import tv.iptv.tun.tviptv.repository.iptv.IPTVChannel
import tv.iptv.tun.tviptv.repository.iptv.IPTVSourceConfig
import tv.iptv.tun.tviptv.repository.iptv.ParserIPTVDataSource
import tv.iptv.tun.tviptv.storage.IKeyValueStorage
import tv.iptv.tun.tviptv.storage.KeyValueStorage
import tv.iptv.tun.tviptv.storage.saveCurrentIPTVSource

class IPTVViewModel(
    private val iptvParserIPTVDataSource: ParserIPTVDataSource = ParserIPTVDataSource,
    private val keyValueStorage: IKeyValueStorage = KeyValueStorage,
) : ViewModel() {
    private val _uiState: MutableStateFlow<IPTVUIState> by lazy {
        MutableStateFlow(IPTVUIState.Empty)
    }

    val popularListIPTV: List<String> = listOf(
        "https://xemtvhd.net (only in Live TV)",
        "https://hqth.me/tviptv",
        "https://tth.vn/vmttv",
        "https://tth.vn/taiiptvhd",
        "https://hqth.me/tai_iptv",
        "https://gg.gg/",
    )

    val uiState: StateFlow<IPTVUIState>
        get() = _uiState

    init {
        getCurrentIPTVSource()
    }

    fun getCurrentIPTVSource() {
        val currentIPTV = iptvParserIPTVDataSource.currentIPTVSource() ?: return
        _uiState.compareAndSet(_uiState.value, IPTVUIState.LoadingIPTVChannel(currentIPTV))
        viewModelScope.launch(Dispatchers.IO) {
            iptvParserIPTVDataSource.getIPTVSource(currentIPTV)
                .catch {
                    println(it)
                }
                .collectLatest {
                    _uiState.emit(
                        IPTVUIState.IPTVWithListChannel(
                            iptvSource = currentIPTV,
                            listChannel = it
                        )
                    )
                }
        }
    }

    fun changeIPTVSource(id: String) {

    }

    fun gotoAddIPTVSource() {

    }

    fun addIPTVSource(url: String, name: String) {
        val currentIPTV = IPTVSourceConfig(
            sourceUrl = url,
            sourceName = name
        )
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(
                IPTVUIState.LoadingIPTVChannel(currentIPTV)
            )
            val listChannel = iptvParserIPTVDataSource.parseSource(currentIPTV)
                .toList()
            keyValueStorage.saveCurrentIPTVSource(currentIPTV)
            _uiState.emit(
                IPTVUIState.IPTVWithListChannel(
                    iptvSource = currentIPTV,
                    listChannel = listChannel
                )
            )
        }

    }
}

sealed interface IPTVUIState {
    data object Empty : IPTVUIState

    data object AddIPTV : IPTVUIState
    data class LoadingIPTVChannel(
        val iptvSource: IPTVSourceConfig
    ) : IPTVUIState

    data class IPTVWithListChannel(
        val listChannel: List<IPTVChannel>,
        val iptvSource: IPTVSourceConfig
    ) : IPTVUIState
}