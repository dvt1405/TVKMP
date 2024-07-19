package tv.iptv.tun.tviptv.exceptions

import tv.iptv.tun.tviptv.models.SourceFrom


class EmptyDataException(
    val sourceFrom: SourceFrom,
    override val errorMessage: String?
) : MyExceptions(ErrorCode.COMMON_EMPTY_DATA) {
}

class RemoteEmptyDataException(
    val sourceFrom: SourceFrom,
    override val errorMessage: String?
) : MyExceptions(ErrorCode.REMOTE_EMPTY_DATA)