package tv.iptv.tun.tviptv.network

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

actual val httpClientEngine: HttpClientEngineFactory<HttpClientEngineConfig>
    get() = Android