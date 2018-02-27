/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class AllocateDirectMemoryTest {

  @Test
  public void simpleAllocateDirect() {
    int longs = 32;
    try (WritableDirectHandle wh = WritableMemory.allocateDirect(longs << 3)) {
      WritableMemory wMem1 = wh.get();
      for (int i = 0; i<longs; i++) {
        wMem1.putLong(i << 3, i);
        assertEquals(wMem1.getLong(i << 3), i);
      }
    }
  }

  @Test
  public void simpleMemoryRequestServer() {
    int longs = 32;
    int bytes = longs << 3;
    try (WritableDirectHandle wh = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wMem1 = wh.get();
      for (int i = 0; i<longs; i++) {
        wMem1.putLong(i << 3, i);
        assertEquals(wMem1.getLong(i << 3), i);
      }
      wMem1.toHexString("Test", 0, 32 * 8);

      int longs2 = 64;
      int bytes2 = longs2 << 3;
      MemoryRequestServer memReqSvr = wMem1.getMemoryRequestServer();
      WritableMemory wMem2 = memReqSvr.request(bytes2); //on heap
      for (int i = 0; i < longs2; i++) {
          wMem2.putLong(i << 3, i);
          assertEquals(wMem2.getLong(i << 3), i);
      }
      memReqSvr.requestClose(wMem1, wMem2);
    }
  }

  @Test
  public void checkClose() {
    try (WritableDirectHandle wdh = WritableMemory.allocateDirect(128)) {
      WritableMemory wmem = wdh.get();
      ResourceState state = wmem.getResourceState();
      state.setInvalid();//intentional before end of scope
      wdh.close(); //checks that AllocateDirect.close is called even when invalid.
      //Adjust the metric tracking
      ResourceState.currentDirectMemoryAllocations_.decrementAndGet();
      ResourceState.currentDirectMemoryAllocated_.addAndGet(-state.getCapacity());
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
