package org.cloudburstmc.protocol.bedrock.packet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.ItemUseTransaction;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.LegacySetItemSlotData;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;

@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class InventoryTransactionPacket implements BedrockPacket {
    public int legacyRequestId;
    public final List<LegacySetItemSlotData> legacySlots = new ObjectArrayList<>();
    public final List<InventoryActionData> actions = new ObjectArrayList<>();
    public InventoryTransactionType transactionType;
    public int actionType;
    public long runtimeEntityId;
    public Vector3i blockPosition;
    public int blockFace;
    public int hotbarSlot;
    public ItemData itemInHand;
    public Vector3f playerPosition;
    public Vector3f clickPosition;
    public Vector3f headPosition;
    /**
     * @since v407
     * @deprecated v431
     */
    @Deprecated
    public boolean usingNetIds;
    /**
     * Block definition of block being picked.
     * ItemUseInventoryTransaction only
     *
     * @param blockDefinition block definition of block
     * @return block definition of block
     */
    public BlockDefinition blockDefinition;
    /**
     * @since v712
     */
    public ItemUseTransaction.TriggerType triggerType;

    /**
     * @since v712
     */
    public ItemUseTransaction.PredictedResult clientInteractPrediction;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.INVENTORY_TRANSACTION;
    }

    @Override
    public InventoryTransactionPacket clone() {
        try {
            return (InventoryTransactionPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}

