package me.nithanim.mmf4j;

import com.sun.jna.Pointer;
import io.netty.buffer.AbstractReferenceCountedByteBuf;

public abstract class MemoryMappedByteBuf extends AbstractReferenceCountedByteBuf {
    public MemoryMappedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }
    
    /**
     * Exposes the internal pointer to the memory
     * INTERNAL USE ONLY!
     */
    public abstract Pointer getPointer();
}
