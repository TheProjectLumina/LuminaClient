package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.*;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.ItemUseTransaction;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class PlayerAuthInputPacket implements BedrockPacket {
    public Vector3f rotation; // head rot after motion
    public Vector3f position;
    public Vector2f motion;
    public final Set<PlayerAuthInputData> inputData = EnumSet.noneOf(PlayerAuthInputData.class);
    public InputMode inputMode;
    public ClientPlayMode playMode;
    /**
     * @deprecated since v748
     */
    public Vector3f vrGazeDirection;
    public long tick;
    public Vector3f delta;
    /**
     * {@link #inputData} must contain {@link PlayerAuthInputData#PERFORM_ITEM_INTERACTION} in order for this to not be null.
     *
     * @since v428
     */
    public ItemUseTransaction itemUseTransaction;
    /**
     * {@link #inputData} must contain {@link PlayerAuthInputData#PERFORM_ITEM_STACK_REQUEST} in order for this to not be null.
     *
     * @since v428
     */
    public ItemStackRequest itemStackRequest;
    /**
     * {@link #inputData} must contain {@link PlayerAuthInputData#PERFORM_BLOCK_ACTIONS} in order for this to not be empty.
     *
     * @since v428
     */
    public final List<PlayerBlockActionData> playerActions = new ObjectArrayList<>();
    /**
     * @since v527
     */
    public InputInteractionModel inputInteractionModel;
    /**
     * @since v748
     */
    public Vector2f interactRotation;
    /**
     * @since 575
     */
    public Vector2f analogMoveVector;
    /**
     * @since 649
     */
    public long predictedVehicle;
    /**
     * @since 662
     */
    public Vector2f vehicleRotation;
    /**
     * @since v748
     */
    public Vector3f cameraOrientation;
    /**
     * @since v766
     */
    public Vector2f rawMoveVector;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.PLAYER_AUTH_INPUT;
    }

    @Override
    public PlayerAuthInputPacket clone() {
        try {
            return (PlayerAuthInputPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

