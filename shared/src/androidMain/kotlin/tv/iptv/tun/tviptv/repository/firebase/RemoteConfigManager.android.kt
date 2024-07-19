package tv.iptv.tun.tviptv.repository.firebase

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.tasks.await

actual fun getRemoteConfig(): IRemoteConfig {
    return object : IRemoteConfig {
        private val remoteConfig = Firebase.remoteConfig
        override suspend fun fetchConfig() {
            remoteConfig.fetch(3600).await()
        }

        override fun getString(key: String): String {
            return remoteConfig.getString(key)
        }

        override fun getBoolean(key: String): Boolean {
            return remoteConfig.getBoolean(key)
        }

        override fun getInt(key: String): Int {
            return remoteConfig.getLong(key).toInt()
        }

        override fun getDouble(key: String): Double {
            return remoteConfig.getDouble(key)
        }

        override fun getLong(key: String): Long {
            return remoteConfig.getLong(key)
        }

    }
}