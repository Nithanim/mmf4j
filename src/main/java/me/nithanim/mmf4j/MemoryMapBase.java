package me.nithanim.mmf4j;

import me.nithanim.mmf4j.buffers.MemoryMappedByteBufFactory;
import com.sun.jna.Pointer;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MemoryMapBase extends MemoryMap {

    protected final MemoryMappedByteBufFactory byteBufFactory;
    private final Set<MemoryView> views = new HashSet<MemoryView>();
    private String path;
    private long mapsize;

    public MemoryMapBase(MemoryMappedByteBufFactory byteBufFactory) {
        this.byteBufFactory = byteBufFactory;
    }

    @Override
    public void openFile(String path) throws IOException {
        _openFile(path);
        this.path = path;
    }

    protected abstract void _openFile(String path) throws IOException;

    @Override
    public void openMapping(long size) throws IOException {
        _openMapping(size);
        mapsize = size;
    }

    protected abstract void _openMapping(long size) throws IOException;

    @Override
    public ByteBuf mapView(long offset, int size) {
        long pageAlignment = getPageAlignment();
        long nativeOffset = (offset / pageAlignment) * pageAlignment;
        int pageOffset = (int) (offset - nativeOffset);
        try {
            MemoryView view = openView(nativeOffset, size + pageOffset);
            return byteBufFactory.getInstance(view, pageOffset, size);
        } catch (MemoryMappingException ex) {
            throw new MemoryMappingException("Unable to map a new buffer! Requested was: offset: " + offset + " size: " + size + " map-size: " + mapsize, ex);
        }
    }

    protected abstract long getPageAlignment();

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

        Pointer p = _getViewPointer(offset, size);
        if (p == Pointer.NULL) {
            throw new MemoryMappingException(
                "Unable to map view; offset:" + offset + " size: " + size + " map-size: " + mapsize);
        }
        return p;
    }

    protected abstract Pointer _getViewPointer(long offset, int size);

    @Override
    public void destroyView(MemoryView view) {
        _unmapView(view.getPointer(), view.getSize());
        views.remove(view);
    }

    @Override
    public void resize(long size) throws IOException {
        List<MemoryView> vs = _unmapViews();
        _resize(size);
        openMapping(size);
        _remapViews(vs);
    }

    protected abstract void _resize(long size);

    @Override
    public void truncateFile(long size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be greater or equal zero!");
        }

        List<MemoryView> vs = _unmapViews();
        try {
            _truncateFile(size);
        } finally {
            openMapping(size);
            _remapViews(vs);
        }
    }

    protected abstract void _truncateFile(long size) throws IOException;

    private List<MemoryView> _unmapViews() {
        List<MemoryView> vs = new ArrayList<MemoryView>(views.size()); //hash changes

        for (MemoryView view : views) {
            setViewValid(view, false);
            _unmapView(view.getPointer(), view.getSize());
            vs.add(view);
        }
        views.clear();
        return vs;
    }

    protected abstract void _unmapView(Pointer p, int size);

    private void _remapViews(List<MemoryView> vs) {
        for (MemoryView view : vs) {
            Pointer p = getViewPointer(view.getOffset(), view.getSize());
            view.setPointer(p);
            setViewValid(view, true);
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
        _close();
    }

    protected abstract void _close();
}
