package tv.iptv.tun.tviptv

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform