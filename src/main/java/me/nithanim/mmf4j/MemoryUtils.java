package me.nithanim.mmf4j;

import com.sun.jna.Platform;
import me.nithanim.mmf4j.platform.windows.MemoryUtilsWindows;
import com.sun.jna.Pointer;
import me.nithanim.mmf4j.platform.linux.MemoryUtilsLinux;

/**
 * This is a utility class that provides access to some native operations
 * outside of the {@link MemoryMap} instance. Both of them are system dependent.
 *
 * <p>
 * The intention behind this class is that the
 * {@link me.nithanim.mmf4j.buffers.MemoryMappedByteBuf}s sometimes need native
 * calls too but should not be dependent on the {@link MemoryMap} itself.
 * However, MemoryUtils should not be responsible for all natives calls because
 * it would not be a util class anymore. Additionally, the handling of the
 * different OSs is so different on some platforms that every platform should
 * have its own MemoryMap implementation.</p>
 *
 * @see MemoryMap
 * @see me.nithanim.mmf4j.buffers.MemoryMappedByteBuf
 */
public abstract class MemoryUtils {
    public static final MemoryUtils INSTANCE;

    static {
        if(Platform.isWindows()) {
            INSTANCE = new MemoryUtilsWindows();
        } else if(Platform.isLinux()) {
            INSTANCE = new MemoryUtilsLinux();
        } else {
            throw new UnsatisfiedLinkError("No MemoryUtils for this OS found!");
        }
    }

    /**
     * Copies the specified amount of bytes from the source to the dest. No
     * boundary checks are made so use with caution! If the memory locations
     * overlaps the result is undefined!
     *
     * @param src the source to copy the bytes from
     * @param srcIndex the offset of the location the src pointer points to from
     * where start copying (like the offset in an array)
     * @param dest the destionation to copy the bytes to
     * @param destIndex the offset of the location the dest pointer points to
     * @param length the amount of bytes to copy from src to dest
     */
    public abstract void nativeCopy(Pointer src, long srcIndex, Pointer dest, long destIndex, int length);

    /**
     * Behaves exactly like
     * {@link #nativeCopy(com.sun.jna.Pointer, long, com.sun.jna.Pointer, long, int)}
     * except it uses raw addresses instead of Pointers.
     *
     * @param srcAddr the base address of the source to copy from
     * @param srcIndex the offset of the base address to copy from
     * @param destAddr the base address of the destination to write to
     * @param destIndex the offset of the base address to write to
     * @param length the number of bytes to copy from src to dest
     */
    public abstract void nativeCopy(long srcAddr, long srcIndex, long destAddr, long destIndex, int length);

    public abstract int getPageSize();
}
