package tv.iptv.tun.tviptv

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val os: PlatformOS
        get() = PlatformOS.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()