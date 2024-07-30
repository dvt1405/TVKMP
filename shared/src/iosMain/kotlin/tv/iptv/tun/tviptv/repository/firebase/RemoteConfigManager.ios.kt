package tv.iptv.tun.tviptv.repository.firebase

actual fun getRemoteConfig(): IRemoteConfig {
    return object : IRemoteConfig {
        override suspend fun fetchConfig() {
            TODO("Not yet implemented")
        }

        override fun getString(key: String): String {
            TODO("Not yet implemented")
        }

        override fun getBoolean(key: String): Boolean {
            TODO("Not yet implemented")
        }

        override fun getInt(key: String): Int {
            TODO("Not yet implemented")
        }

        override fun getDouble(key: String): Double {
            TODO("Not yet implemented")
        }

        override fun getLong(key: String): Long {
            TODO("Not yet implemented")
        }

    }
}