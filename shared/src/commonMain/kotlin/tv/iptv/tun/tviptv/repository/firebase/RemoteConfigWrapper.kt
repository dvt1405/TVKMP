package tv.iptv.tun.tviptv.repository.firebase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import tv.iptv.tun.tviptv.Constants

expect fun getRemoteConfig(): IRemoteConfig

interface IRemoteConfig {
    suspend fun fetchConfig()
    fun getString(key: String): String
    fun getBoolean(key: String): Boolean
    fun getInt(key: String): Int
    fun getDouble(key: String): Double
    fun getLong(key: String): Long
}

class RemoteConfigForTesting(
    val defData: MutableMap<String, Any> = mutableMapOf()
) : IRemoteConfig {
    override suspend fun fetchConfig() {

    }

    override fun getString(key: String): String {
        return defData[key] as? String ?: ""
    }

    override fun getBoolean(key: String): Boolean {
        return defData[key] as? Boolean ?: false
    }

    override fun getInt(key: String): Int {
        return defData[key] as? Int ?: -1
    }

    override fun getDouble(key: String): Double {
        return defData[key] as? Double ?: -1.0
    }

    override fun getLong(key: String): Long {
        return defData[key] as? Long ?: -1L
    }
}

open class RemoteConfigWrapper constructor(
    val remoteConfig: IRemoteConfig
) {
    fun getString(key: String) = remoteConfig.getString(key)
    fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)
    fun getInt(key: String): Int = remoteConfig.getInt(key)
    fun getDouble(key: String): Double = remoteConfig.getDouble(key)
    fun getLong(key: String): Long = remoteConfig.getLong(key)

    companion object : RemoteConfigWrapper(getRemoteConfig()) {}
}

fun RemoteConfigWrapper.getSctvConfig(): JsonObject? {
    return this.remoteConfig.getString(Constants.EXTRA_KEY_SCTV_CONFIG).takeIf {
        it.isNotEmpty()
    }?.let {
        try {
            Json.decodeFromString<JsonObject>(it)
        } catch (e: Exception) {
            null
        }
    }
}