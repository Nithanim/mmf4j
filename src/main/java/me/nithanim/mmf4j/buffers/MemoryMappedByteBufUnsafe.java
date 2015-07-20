package me.nithanim.mmf4j.buffers;

import com.sun.jna.Pointer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import me.nithanim.mmf4j.MemoryView;
import sun.misc.Unsafe;

public class MemoryMappedByteBufUnsafe extends MemoryMappedByteBuf {
    static final Unsafe unsafe;
    static {
        Unsafe u = null;
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            u = (Unsafe) singleoneInstanceField.get(null);
        } catch(Throwable throwable) {
        }
        unsafe = u;
    }
    
    private final MemoryView view;
    private long addr;

    MemoryMappedByteBufUnsafe(MemoryView view, final int pageOffset, int size) {
        super(size);
        this.view = view;

        MemoryView.PointerChangeListener pcl = new MemoryView.PointerChangeListener() {
            @Override
            public void onPointerChange(Pointer p) {
                addr = Pointer.nativeValue(p) + pageOffset;
            }
        };
        view.setPointerChangeListener(pcl);
        pcl.onPointerChange(view.getPointer());
    }

    @Override
    public byte _getByte(int index) {
        return unsafe.getByte(addr + index);
    }

    @Override
    public short _getShort(int index) {
        return unsafe.getShort(addr + index);
    }

    @Override
    protected int _getInt(int index) {
        return unsafe.getInt(addr + index);
    }

    @Override
    protected long _getLong(int index) {
        return unsafe.getLong(addr + index);
    }

    @Override
    public void _setByte(int index, int value) {
        unsafe.putByte(addr + index, (byte) value);
    }

    @Override
    protected void _setShort(int index, int value) {
        unsafe.putShort(addr + index, (short) value);
    }

    @Override
    protected void _setInt(int index, int value) {
        unsafe.putInt(addr + index, value);
    }

    @Override
    protected void _setLong(int index, long value) {
        unsafe.putLong(addr + index, value);
    }

    @Override
    protected int _getUnsignedMedium(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void _setMedium(int index, int value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int capacity() {
        return maxCapacity();
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ByteBufAllocator alloc() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }

    @Override
    public ByteBuf unwrap() {
        return null;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        // TODO probably not so efficient; copy native if possible
        int longs = length / 8;
        int bytes = length - (longs * 8);

        while (longs > 0) {
            dst.setLong(dstIndex, getLong(index));
            index += Long.SIZE / 8;
            dstIndex += Long.SIZE / 8;
            longs--;
        }
        while (bytes > 0) {
            dst.setByte(dstIndex, getByte(index));
            index++;
            dstIndex++;
            bytes--;
        }

        return this;
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        long offset = addr + index;
        for(int i = 0; i < length; i++) {
            dst[dstIndex + i] = unsafe.getByte(offset + i);
        }
        return this;
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        // TODO probably not so efficient; copy native if possible
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

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        long offset = addr + index;
        for(int i = 0; i < length; i++) {
            unsafe.putByte(offset + i, src[srcIndex + i]);
        }
        return this;
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ByteBuf copy(int index, int length) {
        checkIndex(index, length);
        ByteBuf bb = Unpooled.directBuffer(length);
        long src = addr;
        long dest = bb.memoryAddress();
        unsafe.copyMemory(src, dest, length);
        return bb;
    }

    @Override
    public int nioBufferCount() {
        return 0;
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return null;
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public byte[] array() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int arrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMemoryAddress() {
        return true;
    }

    @Override
    public long memoryAddress() {
        return addr;
    }

    @Override
    protected void deallocate() {
        view.release();
    }
}
