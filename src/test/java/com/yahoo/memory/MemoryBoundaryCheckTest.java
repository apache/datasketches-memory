/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import org.testng.annotations.Test;

public class MemoryBoundaryCheckTest {

  private final WritableMemory writableMemory = WritableMemory.allocate(8);

  @Test
  public void testGetBoolean() {
    writableMemory.getBoolean(7);
    try {
      writableMemory.getBoolean(8);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutBoolean() {
    writableMemory.putBoolean(7, true);
    try {
      writableMemory.putBoolean(8, true);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetByte() {
    writableMemory.getByte(7);
    try {
      writableMemory.getByte(8);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutByte() {
    writableMemory.putByte(7, (byte) 1);
    try {
      writableMemory.putByte(8, (byte) 1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetChar() {
    writableMemory.getChar(6);
    try {
      writableMemory.getChar(7);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutChar() {
    writableMemory.putChar(6, 'a');
    try {
      writableMemory.putChar(7, 'a');
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetShort() {
    writableMemory.getShort(6);
    try {
      writableMemory.getShort(7);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutShort() {
    writableMemory.putShort(6, (short) 1);
    try {
      writableMemory.putShort(7, (short) 1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetInt() {
    writableMemory.getInt(4);
    try {
      writableMemory.getInt(5);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutInt() {
    writableMemory.putInt(4, 1);
    try {
      writableMemory.putInt(5, 1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetFloat() {
    writableMemory.getFloat(4);
    try {
      writableMemory.getFloat(5);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutFloat() {
    writableMemory.putFloat(4, 1f);
    try {
      writableMemory.putFloat(5, 1f);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetLong() {
    writableMemory.getLong(0);
    try {
      writableMemory.getLong(1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutLong() {
    writableMemory.putLong(0, 1L);
    try {
      writableMemory.putLong(1, 1L);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetDouble() {
    writableMemory.getDouble(0);
    try {
      writableMemory.getDouble(1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutDouble() {
    writableMemory.putDouble(0, 1d);
    try {
      writableMemory.putDouble(1, 1d);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetAndAddLong() {
    writableMemory.getAndAddLong(0, 1L);
    try {
      writableMemory.getAndAddLong(1, 1L);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetAndSetLong() {
    writableMemory.getAndSetLong(0, 1L);
    try {
      writableMemory.getAndSetLong(1, 1L);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testCompareAndSwapLong() {
    writableMemory.compareAndSwapLong(0, 0L, 1L);
    try {
      writableMemory.compareAndSwapLong(1, 0L, 1L);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }
}
