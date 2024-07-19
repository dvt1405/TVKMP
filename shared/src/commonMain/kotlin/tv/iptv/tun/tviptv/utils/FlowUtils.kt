package tv.iptv.tun.tviptv.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


inline fun <reified E> List<E>.toFlow(): Flow<E> {
    return flowOf(*this.toTypedArray())
}
