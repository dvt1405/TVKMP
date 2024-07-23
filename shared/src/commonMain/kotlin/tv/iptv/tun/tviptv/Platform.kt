package tv.iptv.tun.tviptv

interface Platform {
    val name: String
    val os: PlatformOS
}

expect fun getPlatform(): Platform

enum class PlatformOS {
    Android, IOS, Desktop, Web
}