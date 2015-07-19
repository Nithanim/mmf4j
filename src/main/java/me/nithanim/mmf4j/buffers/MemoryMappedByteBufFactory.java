package me.nithanim.mmf4j.buffers;

import me.nithanim.mmf4j.MemoryView;

public class MemoryMappedByteBufFactory {
    public static final MemoryMappedByteBufFactory INSTANCE = new MemoryMappedByteBufFactory();

    /**
     * Returns a ByteBuf to conveniently access and modify the contents of
     * mapped memory. Since a (native) view must be aligned to the page
     * allocation granularity, an offset must be specified which contains the
     * offset from this alignment.
     *
     * <p>
     * A quick example: Let us assume that the page allocation granularity is
     * 64kb and we want to map an arbitrary file to memory. We are interested to
     * get a view starting at position 10 with the length of 20 bytes.<br/>
     * We cannot simply natively create a view from 10 to 30 because it would
     * result in an error during the native allocation because our mapping needs
     * to start at a multiple of 64kb.<br/>
     * To work around this problem the start address will be the previous
     * multiple of 64kb - zero in this case. To be able to reach the requested
     * buffer-size afterwards, we need to add the remainder of the requested
     * starting position (10) to the native view-size, which changes 40
     * therefore.<br/>
     * We have successfully mapped our native view now but we still need to map
     * the requested buffer view onto the native one. So our pageOffset contains
     * the offset to the last allocation granularity (10) and the originally
     * requested size (not the new one because we got the page offset). These
     * two values are sufficient to create the wequested ByteBuf.</p>
     *
     *
     * @param view the view to create the ByteBuf on
     * @param pageOffset the offset in the native view
     * @param size the size of the buffer starting from the given offset
     * @return a ByteBuf that allowes access to the requested area inside the view
     */
    public MemoryMappedByteBuf getInstance(MemoryView view, final int pageOffset, int size) {
        if (MemoryMappedByteBufUnsafe.unsafe != null) {
            return new MemoryMappedByteBufUnsafe(view, pageOffset, size);
        } else {
            return new MemoryMappedByteBufJna(view, pageOffset, size);
        }
    }

    private MemoryMappedByteBufFactory() {
    }
}
