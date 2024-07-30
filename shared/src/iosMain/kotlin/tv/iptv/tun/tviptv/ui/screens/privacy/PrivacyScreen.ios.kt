package tv.iptv.tun.tviptv.ui.screens.privacy

import platform.Foundation.NSBundle

actual fun getHtmlPrivacyUrl(): String {
    return NSBundle.mainBundle.pathForResource("privacy/privacy", "html")
        ?: "https://xemtvonline.org/privacy.html"
}