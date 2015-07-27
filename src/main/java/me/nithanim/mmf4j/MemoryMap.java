package me.nithanim.mmf4j;

import io.netty.buffer.ByteBuf;
import java.io.IOException;

/**
 * A class that abstracts accessing a file as a buffer in memory rather than
 * direct operations on a file. Accessing and modifying may not only be easier
 * but also yield more performance if you are jumping and overwriting certain
 * parts a lot of times. Since memory mapped files are heavily dependent on the
 * current operating system only the features in common might be available.
 * Furthermore, you might need to call functions that you might no be used to or
 * not even needed on your primary OS.<br/>
 * As an example, opening a mapping on windows automatically resized the file to
 * the requested size. You need to do this because you can't resize a map
 * natively. On unix on the other hand the file is only expanded when writing to
 * the specified offset. Therefore, you need to call truncate before closing to
 * set the file to the desired size which is not needed on unix but mandatory on
 * windows.<br/>
 *
 * <h1>Size of a memory mapped file</h1>
 * Opening a mapping on windows automatically enlarges the file on the disc to
 * the requested size automatically if the specified size is larger than the
 * file. That means, if you want to write to append to a file, you need a good
 * guess of the estimated size of the file. The problem here is not the speed of
 * allocation (it is fast) but rather the free space on the disc and the
 * cropping afterwards. That means that you need to remember the final size of
 * the file and not forget to truncate it.
 *
 * On unix on the other hand the file is only expanded when writing to the
 * specified offset. Therefore, you would not need it there but for the sake of
 * compatibility there is no way around that.<br/>
 *
 * <h1>Resize the memory mapped file</h1>
 * It might not be possible to resize an opened memory map. For windows this
 * library has a mechanism implemented that automatically unmaps and the remaps
 * everything when resizing the map. This is a costly operation and should be
 * used only if neccessary.
 */
public abstract class MemoryMap {

    /**
     * Closes the memory map completely by freeing all views and closing the
     * file.
     */
    public abstract void close();

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
    public abstract ByteBuf mapView(long offset, int size);

    /**
     * Initializes this MemoryMap by opening the the given file. This step is
     * followed by {@link #openMapping(long)}.
     *
     * @param path The path to the file
     * @throws IOException on any error during opening the file
     */
    public abstract void openFile(String path) throws IOException;

    /**
     * Ensures access to the specified size starting at the beginning of the
     * file. This is the area that is mappable to memory later on. If you only
     * want to read you can simply specify the size of the file as the size. If
     * you need write-access beyond the current file-size you need a good guess
     * of the target size that you must specify now. As soon as the mapping is
     * established it cannot be resized anymore. This means that you should not
     * guess it too small since you cannot create a view of the data outside the
     * mapping. BEWARE that the file is extended to the specified size and must
     * be trimmed to the right size afterwards.
     *
     * @param size the amount of bytes that sould be accessible as views
     * @throws IOException
     */
    public abstract void openMapping(long size) throws IOException;

    /**
     * Attempts to resize the currently open mapping. This operation might be
     * costly since it is not possible to resize a memorymap. If it is no
     * possible to resize with a native call, the memorymap and all its views
     * are closed and then rebuilt. This operation is completely transparent to
     * the application using this library because all buffers are updated and
     * remapped. In other words: Every existing buffer before the resize
     * operation is completely functioning as nothing had happened.
     *
     * @param size the new size the file should be resized to
     * @throws IOException
     */
    public abstract void resize(long size) throws IOException;

    /**
     * Truncates the file to the specified size. All views that exceeds the new
     * size need to be unmapped beforehand. This operation is completely
     * transparent to the code using this library since every existing buffer
     * beforehand is still valid after the truncation. This operation needs to
     * be done before closing te file to set it to the desired file size.
     *
     * @param size the new size of the file
     * @throws IOException if it is not possible to truncate the file or to
     * remap the previously open views
     */
    public abstract void truncateFile(long size) throws IOException;

    protected abstract void destroyView(MemoryView view);

    protected void setViewValid(MemoryView view, boolean valid) {
        view.setValid(valid);
    }
}
