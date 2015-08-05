package me.nithanim.mmf4j.platform.linux;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import io.netty.util.CharsetUtil;
import java.io.IOException;
import me.nithanim.mmf4j.MemoryMappingException;
import static me.nithanim.mmf4j.platform.linux.ConstantsLinux.*;

public class Linux {
    public static void main(String[] args) throws IOException {
        String path = "/home/nithanim/testlinux.txt";
        int fd = CLibrary.open(path, O_RDWR | O_CREAT | O_APPEND, 0600);
        if (fd == -1) {
            throw new IOException("Unable to open file \"" + path + "\":" + CLibrary.strerror(Native.getLastError()));
        }

        int size = 20;
        CLibrary.fallocate(fd, 0, 0, size);
        Pointer p = CLibrary.mmap(Pointer.NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
        if (Pointer.nativeValue(p) == -1) {
            throw new MemoryMappingException("Unable to map file: " + CLibrary.strerror(Native.getLastError()));
        }
        //p.setInt(0, 3452);
        //System.out.println(p.getInt(0));
        p.write(0, "Hallo".getBytes(CharsetUtil.UTF_8), 0, 5);

        if (CLibrary.munmap(p, size) == -1) {
            throw new MemoryMappingException("Unable to unmap file: " + CLibrary.strerror(Native.getLastError()));
        }

        CLibrary.ftruncate(fd, 5);

        CLibrary.close(fd);
    }
}
