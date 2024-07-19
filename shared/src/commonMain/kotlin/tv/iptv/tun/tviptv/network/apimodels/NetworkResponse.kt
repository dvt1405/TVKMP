package tv.iptv.tun.tviptv.network.apimodels

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Cookie
import io.ktor.http.Headers
import io.ktor.http.setCookie
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import tv.iptv.tun.tviptv.utils.JsonUtil

@Serializable
sealed class NetworkResponse<T>(open val resCode: Int) {

    data class Success<T>(
        val code: Int, val dataT: T,
        val _cookie: List<Cookie>? = null,
        val headers: Headers? = null
    ) : NetworkResponse<T>(code)

    data class Fail<T>(val code: Int, val cause: Throwable) : NetworkResponse<T>(code)

    val throwable: Throwable
        get() = (this as Fail).cause

    val isSuccess: Boolean
        get() = this is Success

    val data: T
        get() = (this as Success).dataT

    val cookie: List<Cookie>?
        get() = (this as Success)._cookie

    companion object {
        suspend fun <T> wrapSuccess(
            response: HttpResponse,
            serializer: KSerializer<T>
        ): Success<T> {
            val body = response.bodyAsText()
            val js = JsonUtil.fromJson(body, serializer)
            return Success(
                dataT = js,
                code = response.status.value,
                _cookie = response.setCookie(),
                headers = response.headers
            )
        }
    }
}