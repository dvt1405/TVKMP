package tv.iptv.tun.tviptv.exceptions

abstract class MyExceptions(
    open val errorCode: Int,
    open val errorMessage: String? = null
) : Throwable(errorMessage) {
}