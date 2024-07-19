package tv.iptv.tun.tviptv.exceptions

class NetworkExceptions(
    val httpStatusCode: Int,
    override val errorMessage: String?,
    override val errorCode: Int = ErrorCode.NETWORK_FAIL_STATUS_CODE,
) : MyExceptions(errorCode, errorMessage) {
}