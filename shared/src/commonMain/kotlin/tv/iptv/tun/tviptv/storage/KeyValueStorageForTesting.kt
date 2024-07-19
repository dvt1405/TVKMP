package tv.iptv.tun.tviptv.storage

import kotlinx.serialization.KSerializer


open class KeyValueStorageForTesting : IKeyValueStorage {
    val testingStorage by lazy {
        mutableMapOf<String, Any>()
    }

    override fun putString(key: String, value: String) {
        testingStorage[key] = value
    }

    override fun getString(key: String, defValue: String?): String? {
        return testingStorage[key] as? String ?: defValue
    }

    override fun putFloat(key: String, value: Float) {
        testingStorage[key] = value
    }

    override fun getFloat(key: String, defValue: Float?): Float? {
        return testingStorage[key] as? Float ?: defValue
    }

    override fun putDouble(key: String, value: Double) {
        testingStorage[key] = value
    }

    override fun getDouble(key: String, defValue: Double?): Double? {
        return testingStorage[key] as? Double ?: defValue
    }

    override fun putInt(key: String, value: Int) {
        testingStorage[key] = value
    }

    override fun getInt(key: String, defValue: Int?): Int? {
        return testingStorage[key] as? Int ?: defValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        testingStorage[key] = value
    }

    override fun getBoolean(key: String, defValue: Boolean?): Boolean {
        return (testingStorage[key] as? Boolean) ?: defValue ?: false
    }

    override fun <T : Any> putObject(key: String, value: T, serializable: KSerializer<T>) {
        testingStorage[key] = value
    }

    override fun <T : Any> getObject(key: String, serializable: KSerializer<T>): T? {
        return testingStorage[key] as? T
    }

    companion object : KeyValueStorageForTesting()
}