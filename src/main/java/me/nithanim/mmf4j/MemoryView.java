package me.nithanim.mmf4j;

import com.sun.jna.Pointer;
import io.netty.util.AbstractReferenceCounted;

/**
 * The {@link MemoryView} is a wrapper around the raw memory created by a
 * {@link MemoryMap}. It can be updated by the underlying {@link MemoryMap} if
 * necessary.
 *
 * @see MemoryMap
 * @see me.nithanim.mmf4j.buffers.MemoryMappedByteBuf
 */
public class MemoryView extends AbstractReferenceCounted {
    public static MemoryView getInstance(MemoryMap memoryMap, Pointer pointer, long offset, int size) {
        return new MemoryView(memoryMap, pointer, offset, size);
    }

    private final MemoryMap memoryMap;
    private Pointer pointer;
    private final long offset;
    private final int size;
    private boolean valid = true;

    private PointerChangeListener listener;

    private MemoryView(MemoryMap memoryMap, Pointer pointer, long offset, int size) {
        this.memoryMap = memoryMap;
        this.pointer = pointer;
        this.offset = offset;
        this.size = size;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
        listener.onPointerChange(pointer);
    }

    public void setPointerChangeListener(PointerChangeListener listener) {
        this.listener = listener;
    }

    public Pointer getPointer() {
        return pointer;
    }

    public long getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public boolean isValid() {
        return valid;
    }

    void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.pointer != null ? this.pointer.hashCode() : 0);
        hash = 59 * hash + (int) (this.offset ^ (this.offset >>> 32));
        hash = 59 * hash + this.size;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MemoryView other = (MemoryView) obj;
        return (this.pointer == other.pointer || (this.pointer != null && this.pointer.equals(other.pointer)))
            && this.offset == other.offset
            && this.size == other.size;
    }

    @Override
    protected void deallocate() {
        memoryMap.destroyView(this);
    }

    public interface PointerChangeListener {
        void onPointerChange(Pointer p);
    }
}
