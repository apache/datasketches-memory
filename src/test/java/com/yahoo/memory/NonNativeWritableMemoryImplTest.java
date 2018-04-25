/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;

import java.nio.ByteOrder;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NonNativeWritableMemoryImplTest {

  @Test
  public void checkCharArray() {
    byte[] srcArray = { 0,1, 0,2, 0,3, 0,4, 0,5, 0,6, 0,7, 0,8 };
    char[] dstArray = new char[8];

    Memory mem = Memory.wrap(srcArray, ByteOrder.BIG_ENDIAN);
    mem.getCharArray(0, dstArray, 0, 8);
    for (int i = 0; i < 8; i++) {
      assertEquals(dstArray[i], i + 1);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray, ByteOrder.BIG_ENDIAN);
    wmem.getCharArray(0, dstArray, 0, 8);
    for (int i = 0; i < 8; i++) {
      assertEquals(dstArray[i], i + 1);
    }
  }



}
