package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v800.serializer.CameraAimAssistPresetsSerializer_v800;
import org.cloudburstmc.protocol.bedrock.data.camera.*;

public class CameraAimAssistPresetsSerializer_v898 extends CameraAimAssistPresetsSerializer_v800 {

    public static final CameraAimAssistPresetsSerializer_v898 INSTANCE = new CameraAimAssistPresetsSerializer_v898();

    @Override
    protected void writeCategory(ByteBuf buffer, BedrockCodecHelper helper, CameraAimAssistCategory category) {
        helper.writeString(buffer, category.getName());

        helper.writeArray(buffer, category.getEntityPriorities(), this::writePriority);
        helper.writeArray(buffer, category.getBlockPriorities(), this::writePriority);
        helper.writeArray(buffer, category.getBlockTagPriorities(), this::writePriority);

        helper.writeOptionalNull(buffer, category.getEntityDefaultPriorities(), ByteBuf::writeIntLE);
        helper.writeOptionalNull(buffer, category.getBlockDefaultPriorities(), ByteBuf::writeIntLE);
    }

    @Override
    protected CameraAimAssistCategory readCategory(ByteBuf buffer, BedrockCodecHelper helper) {
        CameraAimAssistCategory category = new CameraAimAssistCategory();
        category.setName(helper.readString(buffer));

        helper.readArray(buffer, category.getEntityPriorities(), this::readPriority);
        helper.readArray(buffer, category.getBlockPriorities(), this::readPriority);
        helper.readArray(buffer, category.getBlockTagPriorities(), this::readPriority);

        category.setEntityDefaultPriorities(helper.readOptional(buffer, null, ByteBuf::readIntLE));
        category.setBlockDefaultPriorities(helper.readOptional(buffer, null, ByteBuf::readIntLE));
        return category;
    }

    @Override
    protected void writePreset(ByteBuf buffer, BedrockCodecHelper helper, CameraAimAssistPresetDefinition preset) {
        helper.writeString(buffer, preset.getIdentifier());
        helper.writeArray(buffer, preset.getBlockExclusionList(), helper::writeString);
        helper.writeArray(buffer, preset.getEntityExclusionList(), helper::writeString);
        helper.writeArray(buffer, preset.getBlockTagExclusionList(), helper::writeString);
        helper.writeArray(buffer, preset.getLiquidTargetingList(), helper::writeString);
        helper.writeArray(buffer, preset.getItemSettings(), this::writeItemSetting);
        helper.writeOptionalNull(buffer, preset.getDefaultItemSettings(), helper::writeString);
        helper.writeOptionalNull(buffer, preset.getHandSettings(), helper::writeString);
    }

    @Override
    protected CameraAimAssistPresetDefinition readPreset(ByteBuf buffer, BedrockCodecHelper helper) {
        final CameraAimAssistPresetDefinition preset = new CameraAimAssistPresetDefinition();
        preset.setIdentifier(helper.readString(buffer));
        helper.readArray(buffer, preset.getBlockExclusionList(), helper::readString);
        helper.readArray(buffer, preset.getEntityExclusionList(), helper::readString);
        helper.readArray(buffer, preset.getBlockTagExclusionList(), helper::readString);
        helper.readArray(buffer, preset.getLiquidTargetingList(), helper::readString);
        helper.readArray(buffer, preset.getItemSettings(), this::readItemSetting);
        preset.setDefaultItemSettings(helper.readOptional(buffer, null, helper::readString));
        preset.setHandSettings(helper.readOptional(buffer, null, helper::readString));
        return preset;
    }
}
