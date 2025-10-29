package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityLinkData;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityProperties;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class AddEntityPacket implements BedrockPacket {
    public List<AttributeData> attributes = new ObjectArrayList<>();
    public EntityDataMap metadata = new EntityDataMap();
    public List<EntityLinkData> entityLinks = new ObjectArrayList<>();
    public long uniqueEntityId;
    public long runtimeEntityId;
    public String identifier;
    public int entityType;
    public Vector3f position;
    public Vector3f motion;
    public Vector2f rotation;
    /**
     * @since v534
     */
    public float headRotation;
    /**
     * @since v534
     */
    public float bodyRotation;
    /**
     * @since v557
     */
    public final EntityProperties properties = new EntityProperties();

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.ADD_ENTITY;
    }

    @Override
    public AddEntityPacket clone() {
        try {
            return (AddEntityPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

