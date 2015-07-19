package me.nithanim.mmf4j;

import me.nithanim.mmf4j.buffers.MemoryMappedByteBufFactory;
import com.sun.jna.Platform;

public class MemoryMapFactory {
    public static MemoryMap getInstance() {
        if(Platform.isWindows()) {
            return new MemoryMapWindows(MemoryMappedByteBufFactory.INSTANCE);
        } else {
            throw new UnsupportedOperationException("OS is not supported!");
        }
    }
    
    private MemoryMapFactory() {
    }
}
