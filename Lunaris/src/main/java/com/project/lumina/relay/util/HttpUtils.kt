package com.project.lumina.relay.util

import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy

object HttpUtils {
    private const val DEFAULT_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1788.0"

    val client = OkHttpClient.Builder()
        .proxy(getSystemProxyConfig())
        .addNetworkInterceptor { chain ->
            chain.proceed(chain.request()
                .newBuilder()
                .header("User-Agent", DEFAULT_AGENT)
                .build())
        }
        .build()

    /**
     * @return http proxy from JVM Options, [Proxy.NO_PROXY] if JVM Option not set
     */
    private fun getSystemProxyConfig(): Proxy {
        val proxyHost = System.getProperty("http.proxyHost") ?: return Proxy.NO_PROXY
        val proxyPort = System.getProperty("http.proxyPort") ?: return Proxy.NO_PROXY

        return try {
            Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort.toInt()))
        } catch (t: Throwable) {
            t.printStackTrace()
            Proxy.NO_PROXY
        }
    }

}