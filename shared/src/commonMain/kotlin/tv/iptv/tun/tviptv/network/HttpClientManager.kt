package tv.iptv.tun.tviptv.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.SocketTimeoutException
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import nl.adaptivity.xmlutil.serialization.XML
import tv.iptv.tun.tviptv.exceptions.NetworkExceptions
import tv.iptv.tun.tviptv.network.apimodels.FormBody
import tv.iptv.tun.tviptv.network.apimodels.NetworkResponse
import tv.iptv.tun.tviptv.network.apimodels.Request

expect val httpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig>

open class HttpClientManager private constructor() {
    val client = HttpClient(httpClientEngine) {
        engine {
            pipelining = true
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
        install(ContentEncoding) {
            gzip()
            deflate()
        }
    }

    suspend fun <T> getXML(
        url: String,
        serializer: KSerializer<T>,
        vararg headers: Pair<String, String>
    ): NetworkResponse<T> {
        return kotlin.runCatching {
            client.get(url) {
                if (headers.isNotEmpty()) {
                    headers.forEach { entry ->
                        this.headers[entry.first] = entry.second
                    }
                }
            }
        }.mapCatching { bodyRes ->
            if (bodyRes.status.isSuccess()) {
                NetworkResponse.Success(
                    bodyRes.status.value,
                    bodyRes.bodyAsText()
                        .let {
                            XML.decodeFromString(serializer, it)
                        },

                    )
            } else {
                NetworkResponse.Fail(
                    -1, NetworkExceptions(
                        bodyRes.status.value,
                        bodyRes.status.description
                    )
                )
            }
        }.getOrElse {
            println(it.message)
            NetworkResponse.Fail(-1, it)
        }
    }

    suspend fun getLineByLine(
        url: String,
        vararg headers: Pair<String, String>
    ): Flow<String> = flow {
        val res = try {
            client.prepareGet(url) {
                if (headers.isNotEmpty()) {
                    headers.forEach { entry ->
                        this.headers[entry.first] = entry.second
                    }
                }
            }.execute()
        } catch (e: Exception) {
            throw e
        }
        val channel = if (res.status.isSuccess()) {
            res.body<ByteReadChannel>()
        } else {
            throw NetworkExceptions(res.status.value, res.status.description)
        }
        emitAll(readLines(channel))
    }

    private fun readLines(channel: ByteReadChannel) = flow {
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line()
            if (line != null) {
                emit(line)
            }
        }
    }

    suspend fun get(
        url: String,
        vararg headers: Pair<String, String>
    ): NetworkResponse<JsonObject> {
        return get(url, JsonObject.serializer(), *headers)
    }

    suspend fun <T> get(
        url: String,
        serializer: KSerializer<T>,
        vararg headers: Pair<String, String>
    ): NetworkResponse<T> {
        return try {
            val bodyRes = client.get(url) {
                if (headers.isNotEmpty()) {
                    headers.forEach { entry ->
                        this.headers[entry.first] = entry.second
                    }
                }
            }
            bodyRes.headers.forEach { s, strings ->

            }
            if (bodyRes.status.isSuccess()) {
                NetworkResponse.wrapSuccess(
                    bodyRes,
                    serializer
                )
            } else {
                NetworkResponse.Fail(
                    bodyRes.status.value,
                    NetworkExceptions(
                        bodyRes.status.value,
                        bodyRes.status.description
                    )
                )
            }
        } catch (e: Exception) {
            NetworkResponse.Fail(-1, e)
        }
    }

    suspend fun <T> post(
        url: String,
        bodyStr: String,
        serializer: KSerializer<T>,
        vararg headers: Pair<String, String>
    ): NetworkResponse<T> {
        return try {
            val bodyRes = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(bodyStr)
                if (headers.isNotEmpty()) {
                    headers {
                        headers.forEach { (key, value) ->
                            append(key, value)
                        }
                    }
                }
            }
            if (bodyRes.status.isSuccess()) {
                NetworkResponse.wrapSuccess(
                    bodyRes,
                    serializer
                )
            } else {
                NetworkResponse.Fail(
                    bodyRes.status.value,
                    NetworkExceptions(
                        bodyRes.status.value,
                        bodyRes.status.description
                    )
                )
            }
        } catch (e: Exception) {
            NetworkResponse.Fail(-1, e)
        }
    }

    suspend fun postJson(
        url: String,
        bodyStr: String,
        vararg headers: Pair<String, String>
    ): NetworkResponse<JsonObject> {
        return post(url, bodyStr, JsonObject.serializer(), *headers)
    }

    suspend fun <T> postForm(
        url: String,
        formParams: Map<String, String>,
        serializer: KSerializer<T>,
        vararg headers: Pair<String, String>
    ): NetworkResponse<T> {
        return try {
            val bodyRes = client.post(url) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    formParams.forEach { (key, value) ->
                        append(key, value)
                    }
                }))
                if (headers.isNotEmpty()) {
                    headers {
                        headers.forEach { (key, value) ->
                            append(key, value)
                        }
                    }
                }
            }
            if (bodyRes.status.isSuccess()) {
                NetworkResponse.wrapSuccess(
                    bodyRes,
                    serializer
                )
            } else {
                NetworkResponse.Fail(
                    bodyRes.status.value,
                    NetworkExceptions(
                        bodyRes.status.value,
                        bodyRes.status.description
                    )
                )
            }
        } catch (e: Exception) {
            NetworkResponse.Fail(-1, e)
        }
    }

    suspend fun postFormJson(
        url: String,
        formParams: Map<String, String>,
        vararg headers: Pair<String, String>
    ): NetworkResponse<JsonObject> {
        return postForm(url, formParams, JsonObject.serializer(), *headers)
    }

    suspend fun newCall(request: Request): NetworkResponse<JsonObject> {
        val headers = request.headers?.entries?.map {
            Pair(it.key, it.value)
        }?.toTypedArray() ?: emptyArray()
        return when (val body = request.body) {
            null -> {
                get(url = request.url, *headers)
            }

            is FormBody -> {
                postFormJson(request.url, body.form, *headers)
            }

            else -> {
                postJson(request.url, body.toString(), *headers)
            }
        }
    }

    suspend fun <T> newCall(request: Request, serializer: KSerializer<T>): NetworkResponse<T> {
        val headers = request.headers?.entries?.map {
            Pair(it.key, it.value)
        }?.toTypedArray() ?: emptyArray()
        return when (val body = request.body) {
            null -> {
                get(url = request.url, serializer, *headers)
            }

            is FormBody -> {
                postForm(request.url, body.form, serializer, *headers)
            }

            else -> {
                post(request.url, body.toString(), serializer, *headers)
            }
        }

    }

    companion object : HttpClientManager() {
        val followUpResCode by lazy {
            listOf(
                HttpStatusCode.PermanentRedirect,
                HttpStatusCode.TemporaryRedirect,
                HttpStatusCode.MultipleChoices,
                HttpStatusCode.MovedPermanently,
                HttpStatusCode.SeeOther,
                HttpStatusCode.UseProxy,
            )
        }
    }
}