/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class AllocateDirectBufferTest {

  @Test
  public void checkAllocateDirect() {
    int longs = 32;
    int bytes = longs << 3;
    try (WritableBufferDirectHandler wh = WritableBuffer.allocateDirect(bytes)) {
      WritableBuffer wBuf = wh.get();
      for (int i = 0; i<longs; i++) {
        wBuf.putLong(i);
      }
      wBuf.resetPos();
      for (int i = 0; i<longs; i++) {
        assertEquals(wBuf.getLong(), i);
      }
      wBuf.toHexString("Test", 0, 32 * 8);
    }
  }

  private static class DummyMemReq implements MemoryRequest {
    @Override public WritableMemory request(long capacityBytes) {
      return null;
    }
    @Override public WritableMemory request(WritableMemory origMem, long copyToBytes,
        long capacityBytes) {
      return null;
    }
    @Override public void closeRequest(WritableMemory mem) {}
    @Override public void closeRequest(WritableMemory memToFree, WritableMemory newMem) {}
  }

  @Test
  public void checkAllocateDirectWithMemReq() {
    MemoryRequest req = new DummyMemReq();
    try (WritableMemoryDirectHandler wh = WritableMemory.allocateDirect(8, req)) {
      WritableMemory wMem = wh.get();
      assertTrue(req.equals(wMem.getMemoryRequest()));
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

}
