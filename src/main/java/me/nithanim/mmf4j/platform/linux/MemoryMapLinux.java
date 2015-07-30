package me.nithanim.mmf4j.platform.linux;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.IOException;
import me.nithanim.mmf4j.MemoryMapBase;
import me.nithanim.mmf4j.MemoryMappingException;
import me.nithanim.mmf4j.buffers.MemoryMappedByteBufFactory;
import static me.nithanim.mmf4j.platform.linux.ConstantsLinux.*;

public class MemoryMapLinux extends MemoryMapBase {
    private int fd = -1;

    public MemoryMapLinux(MemoryMappedByteBufFactory byteBufFactory) {
        super(byteBufFactory);
    }
    
    @Override
    protected void _openFile(String path) throws IOException {
        fd = CLibrary.open(path, O_RDWR | O_CREAT | O_APPEND, 0600);
        if(fd == -1) {
            throw new IOException("Unable to open file \"" + path + "\":" + CLibrary.strerror(Native.getLastError()));
        }
    }

    @Override
    protected void _openMapping(long size) throws IOException {
        int result = CLibrary.fallocate(fd, 0, 0, size);
        if(result == -1) {
            throw new IOException("Unable to reserve space:" + CLibrary.strerror(Native.getLastError()));
        }
    }

    @Override
    protected long getPageAlignment() {
        return MemoryUtilsLinux.INSTANCE.getPageSize();
    }

    @Override
    protected Pointer _getViewPointer(long offset, int size) {
        Pointer p = CLibrary.mmap(Pointer.NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
        if(Pointer.nativeValue(p) == -1) {
            throw new MemoryMappingException("Unable to map file: " + CLibrary.strerror(Native.getLastError()));
        }
        return p;
    }

    /*
     * Overwrite resize to try to resize it natively with a call to mremap
     */
    //@Override
    //public void resize(long size) throws IOException {
    //}
    
    @Override
    protected void _resize(long size) {
        //no work here for linux
    }

    @Override
    protected void _truncateFile(long size) throws IOException {
        if(CLibrary.ftruncate(fd, size) == -1) {
            throw new MemoryMappingException("Unable to truncate file: " + CLibrary.strerror(Native.getLastError()));
        }
    }

    @Override
    protected void _unmapView(Pointer p, int size) {
        if(CLibrary.munmap(p, size) == -1) {
            throw new MemoryMappingException("Unable to unmap file: " + CLibrary.strerror(Native.getLastError()));
        }
    }

    @Override
    protected void _close() {
        if(CLibrary.close(fd) == -1) {
            throw new MemoryMappingException("Unable to close file: " + CLibrary.strerror(Native.getLastError()));
        }
    }
    
}
