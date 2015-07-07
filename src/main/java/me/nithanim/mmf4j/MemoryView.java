package me.nithanim.mmf4j;

import com.sun.jna.Pointer;
import io.netty.util.AbstractReferenceCounted;
import java.util.Objects;

class MemoryView extends AbstractReferenceCounted {
    static MemoryView getInstance(MemoryMapWindows memoryMap, Pointer pointer, long offset, int size) {
        return new MemoryView(memoryMap, pointer, offset, size);
    }

    private final MemoryMapWindows memoryMap;
    private Pointer pointer;
    private final long offset;
    private final int size;
    private boolean valid = true;
    
    private PointerChangeListener listener;

    private MemoryView(MemoryMapWindows memoryMap, Pointer pointer, long offset, int size) {
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
        hash = 59 * hash + Objects.hashCode(this.pointer);
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
        return Objects.equals(this.pointer, other.pointer)
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
