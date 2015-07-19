package me.nithanim.mmf4j.buffers;

import io.netty.buffer.AbstractReferenceCountedByteBuf;

public abstract class MemoryMappedByteBuf extends AbstractReferenceCountedByteBuf {
    public MemoryMappedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }
}
