package me.nithanim.mmf4j.platform.windows;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import me.nithanim.mmf4j.MemoryUtils;

public class MemoryUtilsWindows extends MemoryUtils {
    public static final int allocationGranularity;

    static {
        WinBase.SYSTEM_INFO si = new WinBase.SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(si);
        allocationGranularity = si.dwAllocationGranularity.intValue();
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
    @Override
    public void nativeCopy(Pointer src, long srcIndex, Pointer dest, long destIndex, int length) {
        src = src.share(srcIndex);
        dest = dest.share(destIndex);
        MMFNtDll.INSTANCE.RtlCopyMemory(dest, src, length);
    }

    /**
     * Behaves exactly like
     * {@link #nativeCopy(com.sun.jna.Pointer, long, com.sun.jna.Pointer, long, int)}
     * with the exception that it uses raw addresses instead of {@link Pointer}s.
     */
    @Override
    public void nativeCopy(long srcAddr, long srcIndex, long destAddr, long destIndex, int length) {
        long src = srcAddr + srcIndex;
        long dest = destAddr + destIndex;
        MMFNtDll.INSTANCE.RtlCopyMemory(dest, src, length);
    }

    @Override
    public int getPageSize() {
        return allocationGranularity;
    }
}
