package me.nithanim.mmf4j.buffers;

import com.sun.jna.Pointer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import me.nithanim.mmf4j.MemoryView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import sun.misc.Unsafe;

public abstract class GenericMemoryMappedByteBufTest {
    private static final int MEMORY_SIZE = 50;

    private static Unsafe unsafe;
    private static long defaultMemory;

    private long workingMemory;
    private MemoryView view;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
        singleoneInstanceField.setAccessible(true);
        unsafe = (Unsafe) singleoneInstanceField.get(null);

        defaultMemory = unsafe.allocateMemory(MEMORY_SIZE);
        for (byte i = 0; i < MEMORY_SIZE; i++) {
            unsafe.putByte(defaultMemory + i, i);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        unsafe.freeMemory(defaultMemory);
    }

    @Before
    public void setUp() throws Exception {
        workingMemory = unsafe.allocateMemory(MEMORY_SIZE);
        unsafe.copyMemory(defaultMemory, workingMemory, MEMORY_SIZE);
        view = MemoryView.getInstance(new MemoryMapDummy(), new Pointer(workingMemory), 20, 30);
    }

    @After
    public void tearDown() throws Exception {
        unsafe.freeMemory(workingMemory);
    }

    public abstract MemoryMappedByteBuf genByteBuf(MemoryView view, int offset, int size) throws Exception;

    @Test
    public void testGetAndSetByte() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 11, 3);
        assertEquals(11, bb.getByte(0));
        assertEquals(13, bb.getByte(2));
    }

    @Test
    public void testSetByte() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 15, 5);
        bb.setByte(0, 4);
        assertEquals(4, bb.getByte(0));
        bb.setByte(4, 18);
        assertEquals(18, bb.getByte(4));
    }

    @Test
    public void testGetShort() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 5);
        assertEquals(NativeOrderHelper.nativeShort((short) ((10 << 8) | 11)), bb.getShort(0));
        assertEquals(NativeOrderHelper.nativeShort((short) ((13 << 8) | 14)), bb.getShort(3));
    }

    @Test
    public void testSetShort() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 15, 5);
        bb.setShort(0, 25);
        assertEquals(25, bb.getShort(0));
        bb.setShort(3, 86);
        assertEquals(86, bb.getShort(3));
    }

    @Test
    public void testGetInt() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 7);
        assertEquals(
            NativeOrderHelper.nativeInt(
                (10 << 8 * 3) | (11 << 8 * 2) | (12 << 8) | 13),
            bb.getInt(0));
        assertEquals(
            NativeOrderHelper.nativeInt(
                (13 << 8 * 3) | (14 << 8 * 2) | (15 << 8) | 16),
            bb.getInt(3));
    }

    @Test
    public void testSetInt() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 15, 6);
        bb.setInt(0, 0xF5B97BA4);
        assertEquals(0xF5B97BA4, bb.getInt(0));
        bb.setInt(2, 0xB9D449C6);
        assertEquals(0xB9D449C6, bb.getInt(2));
    }

    @Test
    public void testGetLong() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 12);
        assertEquals(
            NativeOrderHelper.nativeLong(
                (10L << 7 * 8) | (11L << 6 * 8) | (12L << 5 * 8) | (13L << 4 * 8)
                | (14L << 3 * 8) | (15L << 2 * 8) | (16L << 1 * 8) | 17L),
            bb.getLong(0));
        assertEquals(
            NativeOrderHelper.nativeLong(
                (14L << 7 * 8) | (15L << 6 * 8) | (16L << 5 * 8) | (17L << 4 * 8)
                | (18L << 3 * 8) | (19L << 2 * 8) | (20L << 1 * 8) | 21L),
            bb.getLong(4));
    }

    @Test
    public void testSetLong() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 15, 12);
        bb.setLong(0, 0xBF7411E4F8BB564BL);
        assertEquals(0xBF7411E4F8BB564BL, bb.getLong(0));
        bb.setLong(4, 0xC35374295590A2C9L);
        assertEquals(0xC35374295590A2C9L, bb.getLong(4));
    }

    @Test
    public void testGetBytesArray() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 10);
        byte[] expected;
        byte[] actual;

        expected = new byte[] {10, 11, 12, 13, 14};
        actual = new byte[5];
        bb.getBytes(0, actual);
        assertArrayEquals(expected, actual);

        expected = new byte[] {13, 14, 15, 16, 17};
        bb.getBytes(3, actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSetBytesArray() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 10);
        byte[] expected;
        byte[] actual;

        expected = new byte[] {45, 23, 58, 7, 24};
        actual = new byte[5];
        bb.setBytes(0, expected);
        bb.getBytes(0, actual);
        assertEquals(45, bb.getByte(0));
        assertArrayEquals(expected, actual);

        expected = new byte[] {58, 7, 24, 25, 3};
        bb.setBytes(2, expected);
        bb.getBytes(2, actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSetBytesByteBuf() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 10);
        ByteBuf source = Unpooled.wrappedBuffer(new byte[] {45, 63, 27, 9, 12, 35});
        bb.setBytes(2, source);

        byte[] actual = new byte[10];
        bb.getBytes(0, actual);
        assertArrayEquals(new byte[] {10, 11, 45, 63, 27, 9, 12, 35, 18, 19}, actual);
    }

    @Test
    public void testCopy() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 10);
        ByteBuf copy = bb.copy(2, 6);

        bb.setByte(2, 65).setByte(3, 53);
        copy.setByte(0, 23).setByte(1, 66);
        assertEquals(65, bb.getByte(2));
        assertEquals(53, bb.getByte(3));
        assertEquals(23, copy.getByte(0));
        assertEquals(66, copy.getByte(1));
    }

    @Test
    public void testMemoryAddressBegin() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 0, 10);
        assertEquals(workingMemory, bb.memoryAddress());
    }

    @Test
    public void testMemoryAddressArbitrary() throws Exception {
        MemoryMappedByteBuf bb = genByteBuf(view, 10, 10);
        assertEquals(workingMemory + 10, bb.memoryAddress());
    }
}
