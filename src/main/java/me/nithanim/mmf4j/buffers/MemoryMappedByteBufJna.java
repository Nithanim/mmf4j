package me.nithanim.mmf4j.buffers;

import com.sun.jna.Pointer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import me.nithanim.mmf4j.MemoryUtils;
import me.nithanim.mmf4j.MemoryView;

public class MemoryMappedByteBufJna extends MemoryMappedByteBuf {
    private final MemoryView view;
    private Pointer pointer;

    MemoryMappedByteBufJna(MemoryView view, final int pageOffset, int size) {
        super(size);
        this.view = view;

        MemoryView.PointerChangeListener pcl = new MemoryView.PointerChangeListener() {
            @Override
            public void onPointerChange(Pointer p) {
                pointer = p.share(pageOffset);
            }
        };
        view.setPointerChangeListener(pcl);
        pcl.onPointerChange(view.getPointer());
    }

    @Override
    public byte _getByte(int index) {
        return pointer.getByte(index);
    }

    @Override
    public short _getShort(int index) {
        return pointer.getShort(index);
    }

    @Override
    protected int _getInt(int index) {
        return pointer.getInt(index);
    }

    @Override
    protected long _getLong(int index) {
        return pointer.getLong(index);
    }

    @Override
    public void _setByte(int index, int value) {
        pointer.setByte(index, (byte) value);
    }

    @Override
    protected void _setShort(int index, int value) {
        pointer.setShort(index, (short) value);
    }

    @Override
    protected void _setInt(int index, int value) {
        pointer.setInt(index, value);
    }

    @Override
    protected void _setLong(int index, long value) {
        pointer.setLong(index, value);
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
        pointer.read(index, dst, dstIndex, length);
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
    protected ByteBuf setBytesNatively(int index, long srcAddr, int srcIndex, int length) {
        MemoryUtils.INSTANCE.nativeCopy(srcAddr, srcIndex, Pointer.nativeValue(pointer), index, length);
        return this;
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        pointer.write(index, src, srcIndex, length);
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
        Pointer src = pointer;
        Pointer dest = new Pointer(bb.memoryAddress()); //Native.getDirectBufferPointer(bb.nioBuffer());
        MemoryUtils.INSTANCE.nativeCopy(src, index, dest, 0, length);
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
        return Pointer.nativeValue(pointer);
    }

    @Override
    protected void deallocate() {
        view.release();
    }
}
