package org.cloudburstmc.protocol.bedrock.packet;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

@Data
@ToString(doNotUseGetters = true, exclude = {"data"})
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class LevelChunkPacket extends AbstractReferenceCounted implements BedrockPacket {
    public int chunkX;
    public int chunkZ;
    public int subChunksLength;
    public boolean cachingEnabled;
    /**
     * @since v471
     */
    public boolean requestSubChunks;
    /**
     * @since v485
     */
    public int subChunkLimit;

    public final LongList blobIds = new LongArrayList();

    public ByteBuf data;

    /**
     * @since v649
     */
    public int dimension;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.LEVEL_CHUNK;
    }

    @Override
    public LevelChunkPacket touch(Object hint) {
        this.data.touch(hint);
        return this;
    }

    @Override
    protected void deallocate() {
        this.data.release();
    }

    @Override
    public LevelChunkPacket clone() {
        throw new UnsupportedOperationException("Can not clone reference counted packet");
    }
}

