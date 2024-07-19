package tv.iptv.tun.tviptv.utils

import io.ktor.http.Url
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.toUrlOrNull(): Url? {
    return try {
        Url(this)
    } catch (e: Exception) {
        null
    }
}

fun String.getHost(): String {
    return try {
        Url(this).host
    } catch (e: Exception) {
        this
    }
}

fun String.getBaseUrl(): String {
    return try {
        val url = Url(this)
        "${url.protocol.name}://${url.host}"
    } catch (e: Exception) {
        this
    }
}

fun String.getOrigin(): String {
    return getBaseUrl()
}

fun String.getRefererBaseUrl(): String {
    return "${getBaseUrl()}/"
}

fun String.findFirstNumber(): String? {
    val regex = "(\\d+)"
    return Regex(regex).find(this)?.value
}

fun Map<String, String>.buildCookie(): String {
    val cookieBuilder = StringBuilder()
    for (i in this.entries) {
        cookieBuilder.append(i.key)
            .append("=")
            .append(i.value)
            .append(";")
            .append(" ")
    }
    return cookieBuilder.toString().trim().removeSuffix(";")
}

fun String.removeAllSpecialChars(): String {
    return this.replace(
        Regex(
            "[^0-9a-zA-Z+áàảãạăắằẳẵặâấầẩẫậeéèẻẽẹêếềểễệđ" +
                    "íìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữự& ]"
        ), ""
    )
        .replace("\\s+", "+")
        .replace("-", "")
        .trim()
}