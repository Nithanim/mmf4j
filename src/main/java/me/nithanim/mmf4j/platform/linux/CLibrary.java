package me.nithanim.mmf4j.platform.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public class CLibrary implements Library {
    //CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);
    //https://jna.java.net/javadoc/overview-summary.html#library-mapping
    //https://jna.java.net/javadoc/overview-summary.html#performance
    //https://github.com/twall/jna/blob/master/www/DirectMapping.md
    static {
        Native.register(Platform.C_LIBRARY_NAME);
    }

    public static native int getpid();

    public static native int getppid();

    public static native long time(long buf[]);

    /**
     * http://codewiki.wikidot.com/c:system-calls:open
     */
    public static native int open(String path, int flags, int mode);

    /**
     * http://linux.die.net/man/2/close
     */
    public static native int close(int fd);

    /**
     * http://man7.org/linux/man-pages/man2/mmap.2.html
     */
    public static native Pointer mmap(Pointer addr, int length, int prot, int flags, int fd, long offset);

    /**
     * http://linux.die.net/man/2/munmap
     */
    public static native int munmap(Pointer addr, int length);

    /**
     * http://man7.org/linux/man-pages/man3/memcpy.3.html
     */
    public static native long memcpy(long dest, long src, int size);

    public static native Pointer memcpy(Pointer dest, Pointer src, int size);

    /**
     * http://man7.org/linux/man-pages/man2/fallocate.2.html
     */
    public static native int fallocate(int fd, int mode, long offset, long len);

    /**
     * http://linux.die.net/man/2/truncate
     */
    public static native int ftruncate(int fd, long length);

    /**
     * http://www.retran.com/beej/perrorman.html
     */
    public static native String strerror(int errnum);

    /**
     * http://man7.org/linux/man-pages/man3/sysconf.3.html
     */
    public static native long sysconf(int name);
}
