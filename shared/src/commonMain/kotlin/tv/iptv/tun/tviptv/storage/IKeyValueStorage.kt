package tv.iptv.tun.tviptv.storage

import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.utils.JsonUtil

expect fun getPlatformSetting(): Settings

interface IKeyValueStorage {
    fun putString(key: String, value: String)
    fun getString(key: String, defValue: String?): String?
    fun putFloat(key: String, value: Float)
    fun getFloat(key: String, defValue: Float?): Float?
    fun putDouble(key: String, value: Double)
    fun getDouble(key: String, defValue: Double?): Double?
    fun putInt(key: String, value: Int)
    fun getInt(key: String, defValue: Int?): Int?
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defValue: Boolean? = null): Boolean
    fun <T : Any> putObject(key: String, value: T, serializable: KSerializer<T>)
    fun <T : Any> getObject(key: String, serializable: KSerializer<T>): T?

    companion object : IKeyValueStorage {
        private val settings: Settings by lazy {
            getPlatformSetting()
        }

        override fun putString(key: String, value: String) {
            settings.putString(key, value)
        }

        override fun getString(key: String, defValue: String?): String? {
            return settings.getStringOrNull(key) ?: defValue
        }

        override fun putFloat(key: String, value: Float) {
            settings.putFloat(key, value)
        }

        override fun getFloat(key: String, defValue: Float?): Float? {
            return settings.getFloatOrNull(key) ?: defValue
        }

        override fun putDouble(key: String, value: Double) {
            settings.putDouble(key, value)
        }

        override fun getDouble(key: String, defValue: Double?): Double? {
            return settings.getDoubleOrNull(key) ?: defValue
        }

        override fun putInt(key: String, value: Int) {
            settings.putInt(key, value)
        }

        override fun getInt(key: String, defValue: Int?): Int? {
            return settings.getIntOrNull(key) ?: defValue
        }

        override fun putBoolean(key: String, value: Boolean) {
            settings.putBoolean(key, value)
        }

        override fun getBoolean(key: String, defValue: Boolean?): Boolean {
            return settings.getBooleanOrNull(key) ?: defValue ?: false
        }

        override fun <T : Any> putObject(key: String, value: T, serializable: KSerializer<T>) {
            settings.putString(
                key,
                JsonUtil.toJson(value, serializable)
            )
        }

        override fun <T : Any> getObject(key: String, serializable: KSerializer<T>): T? {
            val str = settings.getString(key, "").takeIf {
                it.isNotEmpty()
            } ?: return null
            return runCatching {
                JsonUtil.fromJson(str, serializable)
            }.getOrNull()
        }
    }
}

fun IKeyValueStorage.cacheCookie(vtcBackup: SourceFrom): Map<String, String>? {
    return getObject(vtcBackup.name, MapSerializer(String.serializer(), String.serializer()))
}