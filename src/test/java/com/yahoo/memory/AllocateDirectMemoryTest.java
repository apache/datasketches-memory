/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class AllocateDirectMemoryTest {

  @Test
  public void checkAllocateDirect() {
    int longs = 32;
    int bytes = longs << 3;
    try (WritableMemoryDirectHandler wh = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wMem1 = wh.get();
      for (int i = 0; i<longs; i++) {
        wMem1.putLong(i << 3, i);
        assertEquals(wMem1.getLong(i << 3), i);
      }
      wMem1.toHexString("Test", 0, 32 * 8);
      
      int longs2 = 64;
      int bytes2 = longs2 << 3;
      WritableMemory wMem2 = wMem1.getMemoryRequest().request(bytes2); //on heap
      for (int i = 0; i<longs2; i++) { 
          wMem2.putLong(i << 3, i);
          assertEquals(wMem2.getLong(i << 3), i);
        }
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
