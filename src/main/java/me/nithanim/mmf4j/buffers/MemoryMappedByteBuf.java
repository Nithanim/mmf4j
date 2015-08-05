package me.nithanim.mmf4j.buffers;

import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.buffer.ByteBuf;

/**
 * The ByteBuf makes it easy to access and modify the file mapped in memory by a
 * {@link me.nithanim.mmf4j.MemoryMap}. It is also responsible for enforcing the
 * read/write boundaries.
 */
public abstract class MemoryMappedByteBuf extends AbstractReferenceCountedByteBuf {
    public MemoryMappedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        if(src.hasMemoryAddress()) {
            setBytesNatively(index, srcIndex, srcIndex, length);
        } else {
            setBytesPieceByPiece(index, src, srcIndex, length);
        }
        return this;
    }

    protected abstract ByteBuf setBytesNatively(int index, long srcAddr, int srcIndex, int length);

    private ByteBuf setBytesPieceByPiece(int index, ByteBuf src, int srcIndex, int length) {
        int longs = length / 8;
        int bytes = length - (longs * 8);

        while (longs > 0) {
            this.setLong(index, src.getLong(srcIndex));
            index += Long.SIZE / 8;
            srcIndex += Long.SIZE / 8;
            longs--;
        }
        while (bytes > 0) {
            this.setByte(index, src.getByte(srcIndex));
            index++;
            srcIndex++;
            bytes--;
        }
        return this;
    }
}
