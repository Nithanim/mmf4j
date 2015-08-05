package me.nithanim.mmf4j;

import me.nithanim.mmf4j.platform.windows.MemoryMapWindows;
import me.nithanim.mmf4j.buffers.MemoryMappedByteBufFactory;
import com.sun.jna.Platform;
import me.nithanim.mmf4j.platform.linux.MemoryMapLinux;

/**
 * This factory creates instances of {@link MemoryMap}s for the current OS.
 */
public class MemoryMapFactory {
    public static MemoryMap getInstance() {
        if (Platform.isWindows()) {
            return new MemoryMapWindows(MemoryMappedByteBufFactory.INSTANCE);
        } else if (Platform.isLinux()) {
            return new MemoryMapLinux(MemoryMappedByteBufFactory.INSTANCE);
        } else {
            throw new UnsatisfiedLinkError("OS is not supported!");
        }
    }

    private MemoryMapFactory() {
    }
}
