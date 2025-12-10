package org.cloudburstmc.protocol.bedrock.codec.v898;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.EntityDataTypeMap;
import org.cloudburstmc.protocol.bedrock.codec.v844.BedrockCodecHelper_v844;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.TextProcessingEventOrigin;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.ItemStackRequestActionType;
import org.cloudburstmc.protocol.common.util.TypeMap;

import java.util.UUID;

import static org.cloudburstmc.protocol.common.util.Preconditions.checkNotNull;

public class BedrockCodecHelper_v898 extends BedrockCodecHelper_v844 {

    public BedrockCodecHelper_v898(EntityDataTypeMap entityData, TypeMap<Class<?>> gameRulesTypes, TypeMap<ItemStackRequestActionType> stackRequestActionTypes,
                                   TypeMap<ContainerSlotType> containerSlotTypes, TypeMap<Ability> abilities, TypeMap<TextProcessingEventOrigin> textProcessingEventOrigins) {
        super(entityData, gameRulesTypes, stackRequestActionTypes, containerSlotTypes, abilities, textProcessingEventOrigins);
    }

    @Override
    public CommandOriginData readCommandOrigin(ByteBuf buffer) {
        readString(buffer);
        CommandOriginType origin = CommandOriginType.PLAYER; // Hardcode "player" for now
        UUID uuid = readUuid(buffer);
        String requestId = readString(buffer);
        long playerId = buffer.readLongLE();
        return new CommandOriginData(origin, uuid, requestId, playerId);
    }

    @Override
    public void writeCommandOrigin(ByteBuf buffer, CommandOriginData originData) {
        checkNotNull(originData, "commandOriginData");
        writeString(buffer, "player"); // Hardcode "player" for now
        writeUuid(buffer, originData.getUuid());
        writeString(buffer, originData.getRequestId());
        buffer.writeLongLE(originData.getPlayerId());
    }
}
