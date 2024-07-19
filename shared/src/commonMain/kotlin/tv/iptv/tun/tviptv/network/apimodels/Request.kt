package tv.iptv.tun.tviptv.network.apimodels

import kotlinx.serialization.KSerializer
import tv.iptv.tun.tviptv.utils.JsonUtil

abstract class Body() {
}

class SerializerBody<T : KSerializer<T>>(
    private val data: T,
    private val serializer: KSerializer<T>
) : Body() {
    override fun toString(): String {
        return JsonUtil.toJson(data, serializer)
    }
}

class StringBody(val json: String) : Body() {
    override fun toString(): String {
        return json
    }
}

class FormBody(
    val form: Map<String, String>
) : Body() {
    class Builder() {
        private val form: MutableMap<String, String> = mutableMapOf()
        fun add(key: String, value: String): Builder {
            form[key] = value
            return this
        }

        fun build() = FormBody(form)
    }
}

class Request(
    val url: String,
    val body: Body? = null,
    val headers: Map<String, String>? = null
) {
    class Builder() {
        lateinit var url: String
        private var body: Body? = null

        private var _headers: MutableMap<String, String>? = null
        private val headers: MutableMap<String, String>
            get() = _headers ?: mutableMapOf<String, String>().also {
                _headers = it
            }

        fun url(url: String): Builder {
            this.url = url
            return this
        }

        fun header(key: String, value: String): Builder {
            headers[key] = value
            return this
        }

        fun addHeader(key: String, value: String): Builder {
            headers[key] = value
            return this
        }

        fun post(body: Body): Builder {
            this.body = body
            return this
        }

        fun build() = Request(url, body, headers)
    }
}