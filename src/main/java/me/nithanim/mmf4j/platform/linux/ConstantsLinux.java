package me.nithanim.mmf4j.platform.linux;

public abstract class ConstantsLinux {
    //http://linux.die.net/include/bits/fcntl.h
    public static int O_RDONLY = 00;
    public static int O_WRONLY = 01;
    public static int O_RDWR = 02;
    public static int O_CREAT = 0100;
    public static int O_APPEND = 02000;
    
    //http://linux.die.net/include/bits/mman.h
    public static int PROT_READ = 0x1;
    public static int PROT_WRITE = 0x2;
    public static int PROT_EXEC = 0x4;
    public static int MAP_SHARED = 0x01;
    
    //http://osxr.org/linux/source/include/uapi/linux/falloc.h
    public static final int FALLOC_FL_KEEP_SIZE = 0x01;
    
    //http://gel.sourceforge.net/examples/unistd_8h-source.php
    public static int _SC_PAGESIZE = 47;
    
    private ConstantsLinux() {
    }
}
