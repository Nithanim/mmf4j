package me.nithanim.mmf4j.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.NtDll;

public interface MMFNtDll extends NtDll {
    public static MMFNtDll INSTANCE = (MMFNtDll) Native.loadLibrary("kernel32", MMFNtDll.class);

    /**
     * The RtlCopyMemory routine copies the contents of a source memory block to
     * a destination memory block.
     *
     * @param destination A pointer to the destination memory block to copy the
     * bytes to.
     * @param source A pointer to the destination memory block to copy the bytes
     * to.
     * @param length A pointer to the destination memory block to copy the bytes
     * to.
     *
     * @see
     * <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ff561808%28v=vs.85%29.aspx">MSDN</a>
     */
    void RtlCopyMemory(Pointer destination, Pointer source, int length);

    /**
     * Same as
     * {@link #RtlCopyMemory(com.sun.jna.Pointer, com.sun.jna.Pointer, int)},
     * only with raw addresses instead of {@link Pointer}s.
     *
     * @param destination
     * @param source
     * @param length
     */
    void RtlCopyMemory(long destination, long source, int length);

    /**
     * The RtlMoveMemory routine copies the contents of a source memory block to
     * a destination memory block, and supports overlapping source and
     * destination memory blocks.
     *
     * @param destination A pointer to the destination memory block to copy the
     * bytes to.
     * @param source A pointer to the source memory block to copy the bytes
     * from.
     * @param length The number of bytes to copy from the source to the
     * destination.
     *
     * @see
     * <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ff562030%28v=vs.85%29.aspx">MSDN</a>
     */
    void RtlMoveMemory(Pointer destination, Pointer source, int length);
}
