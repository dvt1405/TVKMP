package tv.iptv.tun.tviptv

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    override val os: PlatformOS
        get() = PlatformOS.Android
}

actual fun getPlatform(): Platform = AndroidPlatform()