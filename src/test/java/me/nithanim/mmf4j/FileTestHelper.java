package me.nithanim.mmf4j;

import io.netty.util.CharsetUtil;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileTestHelper {
    private final RandomAccessFile raf;

    public FileTestHelper(String file) throws IOException {
        raf = new RandomAccessFile(file, "r");
    }
    
    public String getContentsAsString(long offset, int size) throws IOException {
        return new String(getContents(offset, size), CharsetUtil.UTF_8);
    }
    
    public byte[] getContents(long offset, int size) throws IOException {
        byte[] arr = new byte[size];
        raf.seek(offset);
        raf.readFully(arr);
        return arr;
    }
    
    public void close() throws IOException {
        raf.close();
    }
}
