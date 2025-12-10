package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.CommandOutputSerializer_v291;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOutputMessage;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOutputType;
import org.cloudburstmc.protocol.bedrock.packet.CommandOutputPacket;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class CommandOutputSerializer_v898 extends CommandOutputSerializer_v291 {

    public static final CommandOutputSerializer_v898 INSTANCE = new CommandOutputSerializer_v898();

    private static final List<String> OUTPUT_TYPE = Arrays.asList("none", "lastoutput", "silent", "alloutput", "dataset");

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, CommandOutputPacket packet) {
        helper.writeCommandOrigin(buffer, packet.getCommandOriginData());
        helper.writeString(buffer, OUTPUT_TYPE.get(packet.getType().ordinal()));
        buffer.writeIntLE(packet.getSuccessCount());
        helper.writeArray(buffer, packet.getMessages(), this::writeMessage);
        helper.writeOptionalNull(buffer, packet.getData(), helper::writeString);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, CommandOutputPacket packet) {
        packet.setCommandOriginData(helper.readCommandOrigin(buffer));
        packet.setType(CommandOutputType.values()[OUTPUT_TYPE.indexOf(helper.readString(buffer))]);
        packet.setSuccessCount((int) buffer.readUnsignedIntLE());
        helper.readArray(buffer, packet.getMessages(), this::readMessage);
        packet.setData(helper.readOptional(buffer, null, (buf, codecHelper) -> codecHelper.readString(buf)));
    }

    @Override
    public CommandOutputMessage readMessage(ByteBuf buffer, BedrockCodecHelper helper) {
        String messageId = helper.readString(buffer);
        boolean internal = buffer.readBoolean();
        String[] parameters = helper.readArray(buffer, new String[0], helper::readString);
        return new CommandOutputMessage(internal, messageId, parameters);
    }

    @Override
    public void writeMessage(ByteBuf buffer, BedrockCodecHelper helper, CommandOutputMessage outputMessage) {
        requireNonNull(outputMessage, "CommandOutputMessage is null");

        helper.writeString(buffer, outputMessage.getMessageId());
        buffer.writeBoolean(outputMessage.isInternal());
        helper.writeArray(buffer, outputMessage.getParameters(), helper::writeString);
    }
}
