package me.nithanim.mmf4j;

import com.sun.jna.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        MemoryMap mm = MemoryMapFactory.getInstance();
        mm.openFile(Platform.isWindows() ? "./test.txt" : System.getProperty("user.home") + "/test.txt");
        mm.openMapping(1000);
        ByteBuf b = mm.mapView(20, 30);
        b.writeBytes("Hallo".getBytes(CharsetUtil.UTF_8));
        //mm.truncateFile(30);
        b.writerIndex(0);
        b.writeBytes("World".getBytes(CharsetUtil.UTF_8));
        b.setBytes(0, "Java".getBytes(CharsetUtil.UTF_8));

        ByteBuf c = b.copy();
        c.setBytes(0, "AAAA".getBytes(CharsetUtil.UTF_8));

        byte[] bytes = new byte[5];
        b.getBytes(0, bytes);
        System.out.println(new String(bytes, CharsetUtil.UTF_8));

        b.release();
        mm.truncateFile(25);
        mm.resize(50);
        mm.close();
    }
}
