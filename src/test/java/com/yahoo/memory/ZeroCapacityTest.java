/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class ZeroCapacityTest {

  @SuppressWarnings("unused")
  @Test
  public void checkZeroCapacity() {
    WritableMemory wmem = WritableMemory.allocate(0);
    assertEquals(wmem.getCapacity(), 0);

    Memory mem1 = Memory.wrap(new byte[0]);
    Memory mem2 = Memory.wrap(ByteBuffer.allocate(0));
    Memory mem3 = Memory.wrap(ByteBuffer.allocateDirect(0));
    Memory reg = mem3.region(0, 0);
    try {
      WritableMemory.allocateDirect(0);
      Assert.fail();
    } catch (IllegalArgumentException ignore) {
      // expected
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
