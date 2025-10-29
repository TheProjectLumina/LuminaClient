package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString(doNotUseGetters = true)
public class GameTestRequestPacket implements BedrockPacket {
    public int maxTestsPerBatch;
    public int repeatCount;
    public int rotation;
    public boolean stoppingOnFailure;
    public Vector3i testPos;
    public int testsPerRow;
    public String testName;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.GAME_TEST_REQUEST;
    }

    @Override
    public GameTestRequestPacket clone() {
        try {
            return (GameTestRequestPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
