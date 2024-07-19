package tv.iptv.tun.tviptv

import android.app.Application

class App : Application() {
    override fun onCreate() {
        INSTANCE = this
        super.onCreate()
    }

    companion object {
        private lateinit var INSTANCE: App

        fun get() = INSTANCE
    }
}