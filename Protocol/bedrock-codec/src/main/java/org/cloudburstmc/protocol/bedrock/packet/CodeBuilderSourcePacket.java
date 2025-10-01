package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.CodeBuilderCategoryType;
import org.cloudburstmc.protocol.bedrock.data.CodeBuilderCodeStatus;
import org.cloudburstmc.protocol.bedrock.data.CodeBuilderOperationType;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class CodeBuilderSourcePacket implements BedrockPacket {

    public CodeBuilderOperationType operation;
    public CodeBuilderCategoryType category;
    /**
     * @deprecated since v685
     */
    public String value;
    /**
     * @since v685
     */
    public CodeBuilderCodeStatus codeStatus;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CODE_BUILDER_SOURCE;
    }

    @Override
    public CodeBuilderSourcePacket clone() {
        try {
            return (CodeBuilderSourcePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

