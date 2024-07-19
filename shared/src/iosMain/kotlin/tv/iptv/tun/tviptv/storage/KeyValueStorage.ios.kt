package tv.iptv.tun.tviptv.storage

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun getPlatformSetting(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults())
}