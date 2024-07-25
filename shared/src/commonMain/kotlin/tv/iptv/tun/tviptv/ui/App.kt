package tv.iptv.tun.tviptv.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import tv.iptv.tun.tviptv.ui.navigations.TVAppNavHost


@Composable
fun App() {
    MaterialTheme {
        TVAppNavHost()
    }
}