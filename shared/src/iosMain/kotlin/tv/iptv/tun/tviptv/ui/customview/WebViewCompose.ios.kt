package tv.iptv.tun.tviptv.ui.customview

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import platform.Foundation.NSMutableURLRequest.Companion.requestWithURL
import platform.Foundation.NSURL
import platform.UIKit.UIWebView

@Composable
actual fun WebViewCompose(
    url: String,
    modifier: Modifier
) {
    Box(
        modifier = Modifier
    ) {
        UIWebView().apply {
            this.loadRequest(requestWithURL(NSURL(url)))
        }
    }
}