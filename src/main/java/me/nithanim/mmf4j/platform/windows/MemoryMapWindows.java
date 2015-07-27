package me.nithanim.mmf4j.platform.windows;

import me.nithanim.mmf4j.buffers.MemoryMappedByteBufFactory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import java.io.IOException;
import me.nithanim.mmf4j.MemoryMapBase;

public class MemoryMapWindows extends MemoryMapBase {
    private static final int FILE_MAP_COPY = 0x1;
    private static final int FILE_MAP_WRITE = 0x2;
    private static final int FILE_MAP_READ = 0x4;

    private WinNT.HANDLE file;
    private WinNT.HANDLE mapping;

    public MemoryMapWindows(MemoryMappedByteBufFactory byteBufFactory) {
        super(byteBufFactory);
    }
    
    @Override
    protected long getPageAlignment() {
        return MemoryUtilsWindows.allocationGranularity;
    }

    @Override
    protected void _openFile(String path) throws IOException {
        if (file != null) {
            throw new IllegalStateException("The file is already open!");
        }

        file = Kernel32.INSTANCE.CreateFile(
            path,
            WinNT.GENERIC_WRITE + WinNT.GENERIC_READ,
            WinNT.FILE_SHARE_READ,
            null,
            WinNT.CREATE_ALWAYS,
            WinNT.FILE_ATTRIBUTE_NORMAL,
            null);
        if (WinNT.INVALID_HANDLE_VALUE.equals(file)) {
            throw new IOException("Unable to open file: " + Kernel32.INSTANCE.GetLastError());
        }
    }

    @Override
    protected void _openMapping(long size) throws IOException {
        if (mapping != null) {
            throw new IllegalStateException("File is already mapped!");
        }

        mapping = Kernel32.INSTANCE.CreateFileMapping(
            file,
            null,
            WinNT.PAGE_READWRITE,
            (int) (size >> 8 * 4),
            (int) (size & 0xFFFFFFFFL),
            null);

        if (mapping == null || WinNT.INVALID_HANDLE_VALUE.equals(mapping)) {
            throw new IOException("Unable to map file: " + Kernel32.INSTANCE.GetLastError());
        } else if (Kernel32.INSTANCE.GetLastError() == WinError.ERROR_ALREADY_EXISTS) {
            //File mapping already existing, TODO how to care about?
            throw new IOException("ERROR_ALREADY_EXISTS, don't know how to handle that!");
        }
    }

    @Override
    protected Pointer _getViewPointer(long offset, int size) {
        return Kernel32.INSTANCE.MapViewOfFile(
            mapping,
            FILE_MAP_READ | FILE_MAP_WRITE,
            (int) (offset >> 8 * 4),
            (int) (offset & 0xFFFFFFFFL),
            size);
    }

    @Override
    protected void _destroyView(Pointer p) {
        Kernel32.INSTANCE.UnmapViewOfFile(p);
    }

    @Override
    protected void _resize(long size) {
        Kernel32.INSTANCE.CloseHandle(mapping);
        mapping = null;
    }
    
    @Override
    protected void _truncateFile(long size) throws IOException {
        // TODO Handle lp of size
        int resSFP = MMFKernel32.INSTANCE.SetFilePointer(file, size, Pointer.NULL, 1);
        if (WinBase.INVALID_SET_FILE_POINTER == resSFP) {
            throw new IOException("INVALID_SET_FILE_POINTER: " + Kernel32.INSTANCE.GetLastError());
        }

        Kernel32.INSTANCE.CloseHandle(mapping);
        mapping = null;

        boolean resSEOF = MMFKernel32.INSTANCE.SetEndOfFile(file);
        if (!resSEOF) {
            throw new IOException("Unable to SetEndOfFile: " + Kernel32.INSTANCE.GetLastError());
        }
    }

    @Override
    protected void _unmapView(Pointer p) {
        Kernel32.INSTANCE.UnmapViewOfFile(p);
    }

    @Override
    protected void _close() {
        Kernel32.INSTANCE.CloseHandle(mapping);
        Kernel32.INSTANCE.CloseHandle(file);
    }
}
