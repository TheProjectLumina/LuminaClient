package org.cloudburstmc.netty.handler.codec.query.enveloped;

import org.cloudburstmc.netty.handler.codec.query.QueryPacket;

import java.net.InetSocketAddress;

import io.netty.channel.DefaultAddressedEnvelope;

public class DirectAddressedQueryPacket extends DefaultAddressedEnvelope<QueryPacket, InetSocketAddress> {
    public DirectAddressedQueryPacket(QueryPacket message, InetSocketAddress recipient, InetSocketAddress sender) {
        super(message, recipient, sender);
    }

    public DirectAddressedQueryPacket(QueryPacket message, InetSocketAddress recipient) {
        super(message, recipient);
    }
}
