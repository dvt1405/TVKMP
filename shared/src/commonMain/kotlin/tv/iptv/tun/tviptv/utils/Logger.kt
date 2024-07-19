package tv.iptv.tun.tviptv.utils

import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import tv.iptv.tun.tviptv.getPlatform

val Logger = KtorSimpleLogger("TVIptv${getPlatform()}")
fun getLogger(name: String) = KtorSimpleLogger("${name}_${getPlatform()}")
fun Logger.d(tag: Any, s: String = "", message: String) {
    debug("${tag::class.simpleName}_${s}: $message")
}

fun Logger.d(tag: String, s: String, message: String) {
    debug("${tag::class.simpleName}_${s}: $message")
}

fun Logger.e(tag: Any, message: String) {
    error("${tag::class.simpleName}_$message")
}

fun Logger.e(tag: Any, exception: Throwable) {
    error(tag::class.simpleName ?: "", exception)
}