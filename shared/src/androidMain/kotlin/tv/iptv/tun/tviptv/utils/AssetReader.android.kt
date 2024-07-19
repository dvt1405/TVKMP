package tv.iptv.tun.tviptv.utils

import tv.iptv.tun.tviptv.App

actual object AssetReader {
    actual fun readTextFile(fileName: String): String {
        return App.get().assets.open(fileName)
            .bufferedReader()
            .readText()
    }

}