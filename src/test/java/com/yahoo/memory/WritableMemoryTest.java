/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class WritableMemoryTest {

  @Test
  public void wrapBigEndian() {
    ByteBuffer bb = ByteBuffer.allocate(64); //big endian
    WritableMemory wmem = WritableMemory.wrap(bb);
    assertTrue(wmem.swapBytes());
    assertEquals(wmem.getResourceOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void wrapBigEndian2() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.wrap(bb);
    assertTrue(wbuf.swapBytes());
    assertEquals(wbuf.getResourceOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkGetArray() {
    byte[] byteArr = new byte[64];
    WritableMemory wmem = WritableMemory.wrap(byteArr);
    assertTrue(wmem.getArray() == byteArr);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    assertTrue(wbuf.getArray() == byteArr);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkSelfArrayCopy() {
    byte[] srcAndDst = new byte[128];
    WritableMemory wmem = WritableMemory.wrap(srcAndDst);
    wmem.getByteArray(0, srcAndDst, 64, 64);  //non-overlapping
  }

}
