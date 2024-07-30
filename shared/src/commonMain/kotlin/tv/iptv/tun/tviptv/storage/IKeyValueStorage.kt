package tv.iptv.tun.tviptv.storage

import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import tv.iptv.tun.tviptv.models.SourceFrom
import tv.iptv.tun.tviptv.repository.iptv.IPTVSourceConfig

expect val playFormSettings: Settings

interface IKeyValueStorage {
    fun has(key: String): Boolean = false
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

}

fun IKeyValueStorage.cacheCookie(vtcBackup: SourceFrom): Map<String, String>? {
    return getObject(vtcBackup.name, MapSerializer(String.serializer(), String.serializer()))
}


fun IKeyValueStorage.isFirstOpenApp(): Boolean {
    return !has(KVStorageConstants.KEY_FIRST_OPEN_APP)
}

fun IKeyValueStorage.isOnboardingSuccess(): Boolean {
    return getBoolean(KVStorageConstants.KEY_ONBOARDING_SUCCESS)
}

fun IKeyValueStorage.isPrivacyAccepted(): Boolean {
    return getBoolean(KVStorageConstants.KEY_PRIVACY_ACCEPTED)
}

fun IKeyValueStorage.setOnBoardingSuccess(success: Boolean = true) {
    putBoolean(KVStorageConstants.KEY_ONBOARDING_SUCCESS, success)
}

fun IKeyValueStorage.setPrivacyAccepted(accepted: Boolean) {
    putBoolean(KVStorageConstants.KEY_PRIVACY_ACCEPTED, accepted)
}

fun IKeyValueStorage.setFistOpened(opened: Boolean) {
    putBoolean(KVStorageConstants.KEY_FIRST_OPEN_APP, opened)
}

fun IKeyValueStorage.getCurrentIPTVSource(): IPTVSourceConfig? {
    return getObject(KVStorageConstants.KEY_CURRENT_IPTV_SOURCE, IPTVSourceConfig.serializer())
}

fun IKeyValueStorage.saveCurrentIPTVSource(
    iptvSourceConfig: IPTVSourceConfig
) {
    putObject(
        KVStorageConstants.KEY_CURRENT_IPTV_SOURCE,
        iptvSourceConfig,
        IPTVSourceConfig.serializer()
    )
}
