# MemoryMappedFiles4Java

## Description
This library aims to bring memory mapped files to java. The goal is to give better control over the creation, modification and destruction in contrast to already present MappedByteBuffer. It tries to unify the interface for using such maps on different operating systems which means that a lot of details are not possible to implement. Furthermore, there might be some cases where you might need to do tedious work that may not be required on your target platform.

This library supports windows and linux.

## Example
For each file you need to create a new MemoryMap object and give it a path to a file:

```java
MemoryMap mm = MemoryMapFactory.getInstance();
mm.openFile("./test.txt");
```

At least on windows you need to open a mapping with the estimated filesize:
```java
mm.openMapping(100);
```

Now we are done with our preparation and we can start with mapping parts of the file (called a view) to the memory. The memory is presented as a simple buffer where you can read and write from. The first argument is the offset from the beginning of the file and the second one is the size you want to be able to access.
```java
ByteBuf b = mm.mapView(20, 10);
```

In this example we write the string "Hello" at position 20 in the file.
```java
b.writeBytes("Hello".getBytes(CharsetUtil.UTF_8));
```

Of course you can overwrite already written content easily:
```java
b.writerIndex(0);
b.writeBytes("World".getBytes(CharsetUtil.UTF_8));
```

Or if you prefer the direct way:
```java
b.setBytes(0, "Java".getBytes(CharsetUtil.UTF_8));
```

We can also read from the map:
```java
byte[] bytes = new byte[5];
b.getBytes(0, bytes);
System.out.println(new String(bytes, CharsetUtil.UTF_8));
```

When the work of this buffer is done we need to release it:
```java
b.release();
```

When we are done with all file operations we need to truncate the file to the expected size and close it:
```java
mm.truncateFile(25);
mm.close();
```

## Legal
This library proudly uses the buffers of the [Netty](http://netty.io/) library.<br/>
This library proudly uses [JNA](https://github.com/twall/jna) for making native calls.<br/>
This library is released under the Apache License Version 2.0.
