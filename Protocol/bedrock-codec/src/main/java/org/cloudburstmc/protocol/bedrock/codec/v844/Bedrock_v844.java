package org.cloudburstmc.protocol.bedrock.codec.v844;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.EntityDataTypeMap;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.LevelEventSerializer_v291;
import org.cloudburstmc.protocol.bedrock.codec.v361.serializer.LevelEventGenericSerializer_v361;
import org.cloudburstmc.protocol.bedrock.codec.v786.serializer.LevelSoundEventSerializer_v786;
import org.cloudburstmc.protocol.bedrock.codec.v827.Bedrock_v827;
import org.cloudburstmc.protocol.bedrock.codec.v844.serializer.BiomeDefinitionListSerializer_v844;
import org.cloudburstmc.protocol.bedrock.codec.v844.serializer.PlayerArmorDamageSerializer_v844;
import org.cloudburstmc.protocol.bedrock.codec.v844.serializer.ServerboundPackSettingChangeSerializer_v844;
import org.cloudburstmc.protocol.bedrock.data.*;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.bedrock.transformer.FlagTransformer;
import org.cloudburstmc.protocol.common.util.TypeMap;

public class Bedrock_v844 extends Bedrock_v827 {

    protected static final TypeMap<EntityFlag> ENTITY_FLAGS = Bedrock_v827.ENTITY_FLAGS
            .toBuilder()
            .insert(125, EntityFlag.CAN_USE_VERTICAL_MOVEMENT_ACTION)
            .build();

    protected static final EntityDataTypeMap ENTITY_DATA = Bedrock_v827.ENTITY_DATA
            .toBuilder()
            .update(EntityDataTypes.FLAGS, new FlagTransformer(ENTITY_FLAGS, 0))
            .update(EntityDataTypes.FLAGS_2, new FlagTransformer(ENTITY_FLAGS, 1))
            .build();

    protected static final TypeMap<SoundEvent> SOUND_EVENTS = Bedrock_v827.SOUND_EVENTS
            .toBuilder()
            .replace(563, SoundEvent.PLACE_ITEM)
            .insert(564, SoundEvent.SINGLE_ITEM_SWAP)
            .insert(565, SoundEvent.MULTI_ITEM_SWAP)
            .insert(566, SoundEvent.UNDEFINED)
            .build();

    protected static final TypeMap<ParticleType> PARTICLE_TYPES = Bedrock_v827.PARTICLE_TYPES.toBuilder()
            .insert(98, ParticleType.GREEN_FLAME)
            .build();

    protected static final TypeMap<LevelEventType> LEVEL_EVENTS = Bedrock_v827.LEVEL_EVENTS.toBuilder()
            .insert(LEVEL_EVENT_PARTICLE_TYPE, PARTICLE_TYPES)
            .build();

    public static final BedrockCodec CODEC = Bedrock_v827.CODEC.toBuilder()
            .raknetProtocolVersion(11)
            .protocolVersion(844)
            .minecraftVersion("1.21.111")
            .helper(() -> new BedrockCodecHelper_v844(ENTITY_DATA, GAME_RULE_TYPES, ITEM_STACK_REQUEST_TYPES, CONTAINER_SLOT_TYPES, PLAYER_ABILITIES, TEXT_PROCESSING_ORIGINS))
            .updateSerializer(BiomeDefinitionListPacket.class, BiomeDefinitionListSerializer_v844.INSTANCE)
            .updateSerializer(LevelEventPacket.class, new LevelEventSerializer_v291(LEVEL_EVENTS))
            .updateSerializer(LevelEventGenericPacket.class, new LevelEventGenericSerializer_v361(LEVEL_EVENTS))
            .updateSerializer(LevelSoundEventPacket.class, new LevelSoundEventSerializer_v786(SOUND_EVENTS))
            .updateSerializer(PlayerArmorDamagePacket.class, PlayerArmorDamageSerializer_v844.INSTANCE)
            .registerPacket(ServerboundPackSettingChangePacket::new, ServerboundPackSettingChangeSerializer_v844.INSTANCE, 329, PacketRecipient.SERVER)
            .build();
}
