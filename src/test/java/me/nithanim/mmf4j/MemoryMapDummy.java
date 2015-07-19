package me.nithanim.mmf4j;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

public class MemoryMapDummy extends MemoryMap {

    @Override
    public void close() {
    }

    @Override
    public ByteBuf mapView(long offset, int size) {
        return null;
    }

    @Override
    public void openFile(String path) throws IOException {
    }

    @Override
    public void openMapping(long size) throws IOException {
    }

    @Override
    public void resize(long size) throws IOException {
    }

    @Override
    public void truncateFile(long size) throws IOException {
    }

    @Override
    void destroyView(MemoryView view) {
    }
}
