package me.nithanim.mmf4j.platform.windows;

import me.nithanim.mmf4j.buffers.MemoryMappedByteBufFactory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import java.io.IOException;
import me.nithanim.mmf4j.MemoryMapBase;
import me.nithanim.mmf4j.MemoryMappingException;

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
    protected void _openFile(String path) throws IOException {
        if (file != null) {
            throw new IllegalStateException("The file is already open!");
        }

        file = Kernel32.INSTANCE.CreateFile(
            path,
            WinNT.GENERIC_WRITE + WinNT.GENERIC_READ,
            WinNT.FILE_SHARE_READ,
            null,
            WinNT.OPEN_ALWAYS,
            WinNT.FILE_ATTRIBUTE_NORMAL,
            null);
        if (WinNT.INVALID_HANDLE_VALUE.equals(file)) {
            throw new IOException("Unable to open file: " + getLastErrorAsString());
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
            throw new IOException("Unable to map file: " + getLastErrorAsString());
        } else if (Kernel32.INSTANCE.GetLastError() == WinError.ERROR_ALREADY_EXISTS) {
            //File mapping already existing, TODO how to care about?
            throw new IOException("ERROR_ALREADY_EXISTS, don't know how to handle that!");
        }
    }

    @Override
    protected Pointer _getViewPointer(long offset, int size) {
        Pointer p = Kernel32.INSTANCE.MapViewOfFile(
            mapping,
            FILE_MAP_READ | FILE_MAP_WRITE,
            (int) (offset >> 8 * 4),
            (int) (offset & 0xFFFFFFFFL),
            size);
        if(p == Pointer.NULL) {
            throw new MemoryMappingException(getLastErrorAsString());
        } else {
            return p;
        }
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
            throw new IOException(
                "Unable to SetEndOfFile: " + getLastErrorAsString());
        }
    }

    @Override
    protected void _unmapView(Pointer p, int size) {
        Kernel32.INSTANCE.UnmapViewOfFile(p);
    }

    @Override
    protected void _close() {
        Kernel32.INSTANCE.CloseHandle(mapping);
        Kernel32.INSTANCE.CloseHandle(file);
    }
    
    @Override
    protected long getPageAlignment() {
        return MemoryUtilsWindows.allocationGranularity;
    }

    private static String getLastErrorAsString() {
        int lastError = Kernel32.INSTANCE.GetLastError();
        return "LastError: " + lastError + ": " + getErrorString(lastError);
    }

    private static String getErrorString(int errorcode) {
        // Thanks to Shog9@http://stackoverflow.com/a/455533/2060704
        PointerByReference str = new PointerByReference();
        int ret = Kernel32.INSTANCE.FormatMessage(
            WinBase.FORMAT_MESSAGE_FROM_SYSTEM | WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER | WinBase.FORMAT_MESSAGE_IGNORE_INSERTS,
            Pointer.NULL,
            errorcode,
            // Can't use other locales for some reason sometimes. Rust does seem to have the same problem
            // https://github.com/rust-lang/rust/commit/5e8e1b515a9ef1cd38ee0c71f032415906a7f42d
            // using system default for now
            0x0800 /* system default */, // https://msdn.microsoft.com/en-us/library/windows/desktop/dd318693%28v=vs.85%29.aspx
            str,
            0,
            Pointer.NULL);
        if (ret == 0) {
            // ret == 15100 when locale was not found.
            // show the code at least in any case
            return "(FormatMessage returned with " + Kernel32.INSTANCE.GetLastError() + ", no error string could be fetched)";
        }
        if (str.getPointer() != Pointer.NULL) {
            // Might be extremely wrong for older, unsupported systems according to
            // http://bytes.com/topic/c/answers/680878-sizeof-tchar
            // or http://wiki.mcneel.com/developer/sdksamples/countof
            // TCHAR is either char (1 byte) or wchar_t (2 byte); using wchar_t for now
            String string = str.getValue().getWideString(0);
            Kernel32.INSTANCE.LocalFree(str.getValue());
            return string;
        } else {
            throw new RuntimeException("Unable to obtain errorstring for errorcode " + errorcode);
        }
    }
}
