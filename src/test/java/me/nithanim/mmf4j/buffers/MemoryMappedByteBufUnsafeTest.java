package me.nithanim.mmf4j.buffers;


import me.nithanim.mmf4j.MemoryView;

public class MemoryMappedByteBufUnsafeTest extends GenericMemoryMappedByteBufTest {
    @Override
    public MemoryMappedByteBuf genByteBuf(MemoryView view, int offset, int size) throws Exception {
        return new MemoryMappedByteBufUnsafe(view, offset, size);
    }
}
