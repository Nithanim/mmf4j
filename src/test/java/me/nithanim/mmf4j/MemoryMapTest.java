package me.nithanim.mmf4j;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MemoryMapTest {
    private static final String RESOURCE_BASE_PATH;
    
    static {
        URL u = MemoryMapTest.class.getResource("/pathhelper");
        File f = new File(u.getFile());
        System.out.println(f.getParent() + "/");
        RESOURCE_BASE_PATH = f.getParent() + "/";
    }
    
    private static String getFile(String file) {
        return RESOURCE_BASE_PATH + file;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testOpenFile() throws Exception {
        String path = getFile("open.txt");
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(path);
        mm.close();
        assertTrue(new File(path).exists());
    }
    
    @Test
    public void testOpenMapping() throws IOException {
        String path = getFile("mapping.txt");
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(path);
        mm.openMapping(20);
        mm.close();
        
        assertTrue(new File(path).exists());
        long size = new File(path).length();
        if(size != 0 && size != 20) { //automatically resized on win/not on unix
            fail("File has as size of " + size + " but either 0 or 20 expected!");
        }
    }
    
    @Test
    public void testTruncate() throws IOException {
        String path = getFile("truncate.txt");
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(path);
        mm.openMapping(20);
        mm.truncateFile(10);
        mm.close();
        
        assertTrue(new File(path).exists());
        long size = new File(path).length();
        assertEquals("File has as a wrong size!", 10, size);
    }
    
    @Test
    public void testWriteInNewFile() throws Exception {
        String path = getFile("write.txt");
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(path);
        mm.openMapping(20);
        ByteBuf buf = mm.mapView(10, 10);
        buf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        mm.close();
        
        FileTestHelper fth = new FileTestHelper(path);
        assertEquals("Hello", fth.getContentsAsString(10, 5));
        fth.close();
    }
    
    @Test
    public void testWriteInExistingFile() throws Exception {
        String path = getFile("writeExisting.txt");
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(path);
        mm.openMapping(20);
        ByteBuf buf = mm.mapView(8, 10);
        buf.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
        mm.close();
        
        FileTestHelper fth = new FileTestHelper(path);
        assertEquals("Hello", fth.getContentsAsString(8, 5));
        fth.close();
    }
    
    @Test
    public void testReadFile() throws Exception {
        String path = getFile("read.txt");
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(path);
        mm.openMapping(31);
        ByteBuf buf = mm.mapView(8, 12);
        byte[] bytes = new byte[12];
        buf.writerIndex(12);
        buf.readBytes(bytes, 0, 12);
        String read = new String(bytes, CharsetUtil.UTF_8);
        mm.close();
        assertEquals("some content", read);
    }
}
