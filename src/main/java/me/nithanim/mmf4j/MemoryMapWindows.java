package me.nithanim.mmf4j;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemoryMapWindows implements MemoryMap {
    private static final int FILE_MAP_COPY = 0x1;
    private static final int FILE_MAP_WRITE = 0x2;
    private static final int FILE_MAP_READ = 0x4;

    public static final long allocationGranularity;

    static {
        WinBase.SYSTEM_INFO si = new WinBase.SYSTEM_INFO();
        Kernel32.INSTANCE.GetSystemInfo(si);
        allocationGranularity = si.dwAllocationGranularity.longValue();
    }

    private String path;
    private WinNT.HANDLE file;
    private WinNT.HANDLE mapping;
    private long mapsize;

    private final Set<MemoryView> views = new HashSet<MemoryView>();

    @Override
    public void openFile(String path) throws IOException {
        if (file != null) {
            throw new IllegalStateException("The file is already open!");
        }

        this.path = path;
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
    public void openMapping(long size) throws IOException {
        if (mapping != null) {
            throw new IllegalStateException("File is already mapped!");
        }

        this.mapsize = size;
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

    /**
     * Creates a new {@link MemoryMappedByteBuf} that represents and allows
     * access to the file starting at the given offset for the given size in
     * bytes.
     *
     * @param offset the position in the file where the first index of the
     * buffer starts
     * @param size the size in bytes that the buffer gives access to starting at
     * the offset
     * @return a new {@link MemoryMappedByteBuf} that gives access to size bytes
     * of the file starting at offset
     */
    @Override
    public ByteBuf mapView(long offset, int size) {
        long nativeOffset = (offset / allocationGranularity) * allocationGranularity;
        int pageOffset = (int) (offset - nativeOffset);
        try {
            MemoryView view = openView(nativeOffset, size + pageOffset);
            MemoryMappedByteBufImpl bb = new MemoryMappedByteBufImpl(view, pageOffset, size);
            return bb;
        } catch (MemoryMappingException ex) {
            throw new MemoryMappingException("Unable to map a new buffer! Requested was: offset: " + offset + " size: " + size + " map-size: " + mapsize, ex);
        }
    }

    void destroyView(MemoryView view) {
        Kernel32.INSTANCE.UnmapViewOfFile(view.getPointer());
        views.remove(view);
    }

    private MemoryView openView(long offset, int size) {
        Pointer p = getViewPointer(offset, size);
        MemoryView view = MemoryView.getInstance(this, p, offset, size);
        views.add(view);
        return view;
    }

    private Pointer getViewPointer(long offset, int size) {
        if (offset + size > mapsize) {
            throw new MemoryMappingException("View is out of bounds: offset:" + offset + " size: " + size + " map-size: " + mapsize);
        }

        Pointer p = Kernel32.INSTANCE.MapViewOfFile(
            mapping,
            FILE_MAP_READ | FILE_MAP_WRITE,
            (int) (offset >> 8 * 4),
            (int) (offset & 0xFFFFFFFFL),
            size);
        if (p == Pointer.NULL) {
            throw new MemoryMappingException(
                "Unable to map view; offset:" + offset + " size: " + size + " map-size: " + mapsize);
        }
        return p;
    }

    @Override
    public void resize(long size) throws IOException {
        List<MemoryView> vs = _unmapViews();
        Kernel32.INSTANCE.CloseHandle(mapping);
        mapping = null;
        openMapping(size);
        _remapViews(vs);
    }

    /**
     * Truncates the file to the specified size. All views that exceeds the new
     * size need to be unmapped beforehand!
     *
     * @param size the new size of the file
     * @throws IOException if it is not possible to truncate the file or to
     * remap the previously open views
     */
    @Override
    public void truncateFile(long size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be greater or equal zero!");
        }

        List<MemoryView> vs = _unmapViews();
        try {
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
        } finally {
            openMapping(size);
            _remapViews(vs);
        }
    }

    private List<MemoryView> _unmapViews() {
        List<MemoryView> vs = new ArrayList<MemoryView>(views.size()); //hash changes

        for (MemoryView view : views) {
            view.setValid(false);
            Kernel32.INSTANCE.UnmapViewOfFile(view.getPointer());
            vs.add(view);
        }
        views.clear();
        return vs;
    }

    private void _remapViews(List<MemoryView> vs) {
        for (MemoryView view : vs) {
            Pointer p = getViewPointer(view.getOffset(), view.getSize());
            view.setPointer(p);
            view.setValid(true);
            views.add(view);
        }
    }

    /**
     * Closes the memory map completely by freeing all views and closing the
     * file.
     */
    @Override
    public void close() {
        _unmapViews();
        Kernel32.INSTANCE.CloseHandle(mapping);
        Kernel32.INSTANCE.CloseHandle(file);
    }
}
