package tv.iptv.tun.tviptv.ui.customview

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun WebViewCompose(
    url: String,
    modifier: Modifier
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                loadUrl(url)
                this.settings.javaScriptEnabled = true
            }
        },
        modifier = modifier
    )
}