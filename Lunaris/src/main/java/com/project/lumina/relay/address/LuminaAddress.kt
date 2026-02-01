package com.project.lumina.relay.address

import java.net.InetSocketAddress

data class LuminaAddress(val hostName: String, val port: Int)

inline val LuminaAddress.inetSocketAddress
    get() = InetSocketAddress(hostName, port)

inline val InetSocketAddress.luminaAddress
    get() = LuminaAddress(hostName, port)