package tv.iptv.tun.tviptv.ui

import tv.iptv.tun.tviptv.Platform
import tv.iptv.tun.tviptv.getPlatform

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}