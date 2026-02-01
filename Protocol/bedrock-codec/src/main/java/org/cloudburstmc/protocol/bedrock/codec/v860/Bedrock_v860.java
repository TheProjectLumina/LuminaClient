package org.cloudburstmc.protocol.bedrock.codec.v860;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v859.Bedrock_v859;

public class Bedrock_v860 extends Bedrock_v859 {

    public static final BedrockCodec CODEC = Bedrock_v859.CODEC.toBuilder()
            .raknetProtocolVersion(11)
            .protocolVersion(860)
            .minecraftVersion("1.21.124")
            .build();
}
