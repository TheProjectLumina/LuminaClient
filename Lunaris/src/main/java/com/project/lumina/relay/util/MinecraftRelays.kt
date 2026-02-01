package com.project.lumina.relay.util

import com.project.lumina.relay.LuminaRelay
import com.project.lumina.relay.LuminaRelaySession
import com.project.lumina.relay.address.LuminaAddress
import org.cloudburstmc.protocol.bedrock.BedrockPong

/**
 * Creates a LuminaRelay instance in capture mode that will connect to the specified server.
 * This function automatically sets up the relay to capture packets between the client and server.
 * 
 * @param advertisement The server advertisement details used for client connections
 * @param localAddress The local address the relay will bind to
 * @param remoteAddress The remote server address to connect to
 * @param onSessionCreated Callback executed when a relay session is created
 * @return A configured LuminaRelay instance
 */
fun captureLuminaRelay(
    advertisement: BedrockPong = LuminaRelay.createNativeAdvertisement(),
    localAddress: LuminaAddress = LuminaAddress("0.0.0.0", 19132),
    remoteAddress: LuminaAddress,
    onSessionCreated: LuminaRelaySession.() -> Unit
): LuminaRelay {
    return LuminaRelay(
        localAddress = localAddress,
        advertisement = advertisement
    ).capture(
        remoteAddress = remoteAddress,
        onSessionCreated = onSessionCreated
    )
}