package tv.iptv.tun.tviptv.ui.screens.iptv

import androidx.lifecycle.ViewModel
import tv.iptv.tun.tviptv.repository.iptv.ParserIPTVDataSource

class IPTVViewModel(
    private val iptvParserIPTVDataSource: ParserIPTVDataSource = ParserIPTVDataSource
) : ViewModel() {
}