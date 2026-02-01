package org.cloudburstmc.protocol.common.util;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.concurrent.FastThreadLocal;

/**
 * as we do not want to use com.nukkitx.natives,
 * we override the original class and use pure java implement
 */
public class Zlib {
    public static final Zlib DEFAULT = new Zlib(false);
    public static final Zlib RAW = new Zlib(true);

    private static final int CHUNK = 8192;

    private final FastThreadLocal<Inflater> inflaterLocal;
    private final FastThreadLocal<Deflater> deflaterLocal;
    private final FastThreadLocal<byte[]> chunkBytes;

    private Zlib(boolean raw) {
        // Required for Android API versions prior to 26.
        this.inflaterLocal = new FastThreadLocal() {
            @Override
            public Inflater initialValue() {
                return new Inflater(raw);
            }
        };
        this.deflaterLocal = new FastThreadLocal() {
            @Override
            protected Deflater initialValue() {
                return new Deflater(7, raw);
            }
        };
        this.chunkBytes = new FastThreadLocal() {
            @Override
            protected byte[] initialValue() {
                return new byte[CHUNK];
            }
        };
    }

    public ByteBuf inflate(ByteBuf buffer, int maxSize) throws DataFormatException {
        ByteBuf source = null;
        ByteBuf decompressed = ByteBufAllocator.DEFAULT.ioBuffer();

        try {
            if (!buffer.isDirect() || (buffer instanceof CompositeByteBuf && ((CompositeByteBuf) buffer).numComponents() > 1)) {
                // We don't have a direct buffer. Create one.
                ByteBuf temporary = ByteBufAllocator.DEFAULT.ioBuffer();
                temporary.writeBytes(buffer);
                source = temporary;
            } else {
                source = buffer;
            }

            Inflater inflater = inflaterLocal.get();
            inflater.reset();
            ByteBuffer input = source.internalNioBuffer(source.readerIndex(), source.readableBytes());
            if (input.hasArray()) {
                inflater.setInput(input.array(), input.arrayOffset() + input.position(), input.remaining());
            } else {
                byte[] bytes = new byte[input.remaining()];
                input.get(bytes);
                inflater.setInput(bytes);
            }
            inflater.finished();

            while (!inflater.finished()) {
                decompressed.ensureWritable(CHUNK);
                int index = decompressed.writerIndex();
                int written = inflate(inflater, decompressed.internalNioBuffer(index, CHUNK));
                if (written < 1) {
                    break;
                }
                decompressed.writerIndex(index + written);
                if (maxSize > 0 && decompressed.writerIndex() >= maxSize) {
                    throw new DataFormatException("Inflated data exceeds maximum size");
                }
            }
            return decompressed;
        } catch (DataFormatException e) {
            decompressed.release();
            throw e;
        } finally {
            if (source != null && source != buffer) {
                source.release();
            }
        }
    }

    public void deflate(ByteBuf uncompressed, ByteBuf compressed, int level) throws DataFormatException {
        ByteBuf destination = null;
        ByteBuf source = null;
        try {
            if (!uncompressed.isDirect() || (uncompressed instanceof CompositeByteBuf && ((CompositeByteBuf) uncompressed).numComponents() > 1)) {
                // Source is not a direct buffer. Work on a temporary direct buffer and then write the contents out.
                source = ByteBufAllocator.DEFAULT.ioBuffer();
                source.writeBytes(uncompressed);
            } else {
                source = uncompressed;
            }

            if (!compressed.isDirect()) {
                // Destination is not a direct buffer. Work on a temporary direct buffer and then write the contents out.
                destination = ByteBufAllocator.DEFAULT.ioBuffer();
            } else {
                destination = compressed;
            }

            Deflater deflater = deflaterLocal.get();
            deflater.reset();
            deflater.setLevel(level);
            ByteBuffer input = source.internalNioBuffer(source.readerIndex(), source.readableBytes());
            if (input.hasArray()) {
                deflater.setInput(input.array(), input.arrayOffset() + input.position(), input.remaining());
            } else {
                byte[] bytes = new byte[input.remaining()];
                input.get(bytes);
                deflater.setInput(bytes);
            }

            while (!deflater.finished()) {
                int index = destination.writerIndex();
                destination.ensureWritable(CHUNK);
                int written = deflate(deflater, destination.internalNioBuffer(index, CHUNK));
                destination.writerIndex(index + written);
            }

            if (destination != compressed) {
                compressed.writeBytes(destination);
            }
        } finally {
            if (source != null && source != uncompressed) {
                source.release();
            }
            if (destination != null && destination != compressed) {
                destination.release();
            }
        }
    }

    private int inflate(Inflater inflater, ByteBuffer output) throws DataFormatException {
        if (output.hasArray()) {
            return inflater.inflate(output.array(), output.arrayOffset() + output.position(), output.remaining());
        } else {
            int startPos = output.position();
            byte[] chunkBytes = this.chunkBytes.get();
            while (output.remaining() > 0 && !inflater.finished()) {
                int length = Math.min(output.remaining(), CHUNK);
                int result = inflater.inflate(chunkBytes, 0, length);
                output.put(chunkBytes, 0, result);
            }
            return output.position() - startPos;
        }
    }

    private int deflate(Deflater deflater, ByteBuffer output) {
        deflater.finish();
        if (output.hasArray()) {
            return deflater.deflate(output.array(), output.arrayOffset() + output.position(), output.remaining());
        } else {
            int startPos = output.position();
            byte[] chunkBytes = this.chunkBytes.get();
            while (output.remaining() > 0 && !deflater.finished()) {
                int length = Math.min(output.remaining(), CHUNK);
                int result = deflater.deflate(chunkBytes, 0, length);
                output.put(chunkBytes, 0, result);
            }
            return output.position() - startPos;
        }
    }
}