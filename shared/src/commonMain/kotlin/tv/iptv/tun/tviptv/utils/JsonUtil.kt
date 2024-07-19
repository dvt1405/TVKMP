package tv.iptv.tun.tviptv.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

object JsonUtil {
    private val json = Json { ignoreUnknownKeys = true }

    fun <T> toJson(data: T, serializer: KSerializer<T>): String {
        return json.encodeToString(serializer, data)
    }

    fun <T> fromJson(jsonString: String, serializer: KSerializer<T>): T {
        return json.decodeFromString(serializer, jsonString)
    }

    fun <T> listFromJson(jsonString: String, serializer: KSerializer<T>): List<T> {
        val listSerializer = ListSerializer((serializer))
        return json.decodeFromString(listSerializer, jsonString)
    }
}

fun JsonObject.getString(key: String, defValue: String? = null): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull ?: defValue
}

fun JsonObject.optString(key: String): String {
    return (this[key] as? JsonPrimitive)?.contentOrNull ?: ""
}

fun JsonObject.getInt(key: String, defValue: Int? = null): Int? {
    return (this[key] as? JsonPrimitive)?.intOrNull ?: defValue
}

fun JsonObject.getBoolean(key: String, defValue: Boolean? = null): Boolean {
    return (this[key] as? JsonPrimitive)?.booleanOrNull ?: defValue ?: false
}

fun JsonObject.getJsonObject(key: String): JsonObject? {
    return (this[key] as? JsonObject)
}

fun JsonObject.optJSONObject(key: String): JsonObject? {
    return getJsonObject(key)
}

fun JsonObject.getJsonArray(key: String): JsonArray? {
    return (this[key] as? JsonArray)
}
