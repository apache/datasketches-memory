/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.BaseState.DUPLICATE;
import static com.yahoo.memory.BaseState.REGION;
import static com.yahoo.memory.Util.nativeOrder;
import static com.yahoo.memory.Util.nonNativeOrder;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class SpecificLeafTest {

  @Test
  public void checkByteBufferLeafs() {
    int bytes = 128;
    ByteBuffer bb = ByteBuffer.allocate(bytes);
    bb.order(nativeOrder);

    Memory mem = Memory.wrap(bb).region(0, bytes, nativeOrder);
    Buffer buf = mem.asBuffer().region(0, bytes, nativeOrder);

    bb.order(nonNativeOrder);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, nonNativeOrder);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, nonNativeOrder);
    Buffer buf3 = buf2.duplicate();

    assertTrue((mem.getTypeId() & REGION) > 0);
    assertTrue((mem2.getTypeId() & REGION) > 0);
    assertTrue((buf.getTypeId() & REGION) > 0);
    assertTrue((buf2.getTypeId() & REGION) > 0);
    assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
  }

  @Test
  public void checkDirectLeafs() {
    int bytes = 128;
    try (WritableDirectHandle h = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wmem = h.get(); //native mem
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, nonNativeOrder);

      Memory mem = wmem.region(0, bytes, nativeOrder);
      Buffer buf = mem.asBuffer().region(0, bytes, nativeOrder);


      Memory mem2 = nnwmem.region(0, bytes, nonNativeOrder);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, nonNativeOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue((mem.getTypeId() & REGION) > 0);
      assertTrue((mem2.getTypeId() & REGION) > 0);
      assertTrue((buf.getTypeId() & REGION) > 0);
      assertTrue((buf2.getTypeId() & REGION) > 0);
      assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
    }
  }

  @Test
  public void checkMapLeafs() throws IOException {
    File file = new File("TestFile2.bin");
    if (file.exists()) {
      try {
        java.nio.file.Files.delete(file.toPath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    assertTrue(file.createNewFile());
    assertTrue(file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue(file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.

    final long bytes = 128;

    try (WritableMapHandle h = WritableMemory.map(file, 0L, bytes, Util.nativeOrder)) {
      WritableMemory wmem = h.get(); //native mem
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, nonNativeOrder);

      Memory mem = wmem.region(0, bytes, nativeOrder);
      Buffer buf = mem.asBuffer().region(0, bytes, nativeOrder);
      Buffer buf4 = buf.duplicate();


      Memory mem2 = nnwmem.region(0, bytes, nonNativeOrder);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, nonNativeOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue((mem.getTypeId() & REGION) > 0);
      assertTrue((mem2.getTypeId() & REGION) > 0);
      assertTrue((buf.getTypeId() & REGION) > 0);
      assertTrue((buf2.getTypeId() & REGION) > 0);
      assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
      assertTrue((buf4.getTypeId() & DUPLICATE) > 0);
    }
  }

  @Test
  public void checkHeapLeafs() {
    int bytes = 128;
    WritableMemory wmem = WritableMemory.allocate(bytes);
    WritableMemory nnwmem = wmem.writableRegion(0, bytes, nonNativeOrder);

    Memory mem = wmem.region(0, bytes, nativeOrder);
    Buffer buf = mem.asBuffer().region(0, bytes, nativeOrder);

    Memory mem2 = nnwmem.region(0, bytes, nonNativeOrder);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, nonNativeOrder);
    Buffer buf3 = buf2.duplicate();

    assertTrue((mem.getTypeId() & REGION) > 0);
    assertTrue((mem2.getTypeId() & REGION) > 0);
    assertTrue((buf.getTypeId() & REGION) > 0);
    assertTrue((buf2.getTypeId() & REGION) > 0);
    assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
  }


}
