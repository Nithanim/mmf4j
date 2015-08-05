package me.nithanim.mmf4j.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

public interface MMFKernel32 extends Kernel32 {
    public static MMFKernel32 INSTANCE = (MMFKernel32) Native.loadLibrary("kernel32", MMFKernel32.class, W32APIOptions.UNICODE_OPTIONS);

    boolean SetEndOfFile(WinNT.HANDLE hFile);

    /**
     * @see
     * <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/aa365541%28v=vs.85%29.aspx">MSDN</a>
     */
    int SetFilePointer(WinNT.HANDLE hFile, long lDistaceToMove, Pointer lpDistanceToMoveHigh, int dwMoveMethod);
}
