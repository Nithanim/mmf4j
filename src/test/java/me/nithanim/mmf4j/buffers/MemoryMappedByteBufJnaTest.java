package me.nithanim.mmf4j.buffers;

import me.nithanim.mmf4j.MemoryView;

public class MemoryMappedByteBufJnaTest extends GenericMemoryMappedByteBufTest {
    @Override
    public MemoryMappedByteBuf genByteBuf(MemoryView view, int offset, int size) throws Exception {
        return new MemoryMappedByteBufJna(view, offset, size);
    }
}
