package me.nithanim.mmf4j.platform.linux;

import com.sun.jna.Pointer;
import me.nithanim.mmf4j.MemoryUtils;

public class MemoryUtilsLinux extends MemoryUtils {
    private static final int PAGESIZE;
    
    static {
        PAGESIZE = (int) CLibrary.sysconf(ConstantsLinux._SC_PAGESIZE);
    }
    
    @Override
    public void nativeCopy(Pointer src, long srcIndex, Pointer dest, long destIndex, int length) {
        CLibrary.memcpy(dest.share(destIndex), src.share(srcIndex), length);
    }

    @Override
    public void nativeCopy(long srcAddr, long srcIndex, long destAddr, long destIndex, int length) {
        CLibrary.memcpy(destAddr + destIndex, srcAddr + srcIndex, length);
    }

    @Override
    public int getPageSize() {
        return PAGESIZE;
    }

}
