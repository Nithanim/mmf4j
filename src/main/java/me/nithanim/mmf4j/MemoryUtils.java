package me.nithanim.mmf4j;

import com.sun.jna.Pointer;

public abstract class MemoryUtils {
    public static final MemoryUtils INSTANCE;

    static {
        //if(Platform.isWindows()) {
        INSTANCE = new MemoryUtilsWindows();
        //}
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
}
