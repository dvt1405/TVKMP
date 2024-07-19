package tv.iptv.tun.tviptv.storage

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import tv.iptv.tun.tviptv.App

actual fun getPlatformSetting(): Settings {
    return SharedPreferencesSettings(
        App.get().getSharedPreferences("_defaultShared", Context.MODE_PRIVATE)
    )
}