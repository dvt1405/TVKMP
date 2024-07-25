package tv.iptv.tun.tviptv.ui.customview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebViewCompose(
    url: String,
    modifier: Modifier = Modifier
)