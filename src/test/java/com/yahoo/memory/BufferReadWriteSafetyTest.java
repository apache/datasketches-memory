/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

public class BufferReadWriteSafetyTest {

  // Test various operations with read-only Buffer

  private final WritableBuffer buf = (WritableBuffer) Buffer.wrap(ByteBuffer.allocate(8));

  @Test(expectedExceptions = AssertionError.class)
  public void testPutByte() {
    buf.putByte(0, (byte) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutBytePositional() {
    buf.putByte((byte) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutBoolean() {
    buf.putBoolean(0, true);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutBooleanPositional() {
    buf.putBoolean(true);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutShort() {
    buf.putShort(0, (short) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutShortPositional() {
    buf.putShort((short) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutChar() {
    buf.putChar(0, (char) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutCharPositional() {
    buf.putChar((char) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutInt() {
    buf.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutIntPositional() {
    buf.putInt(1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutLong() {
    buf.putLong(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutLongPositional() {
    buf.putLong(1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutFloat() {
    buf.putFloat(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutFloatPositional() {
    buf.putFloat(1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutDouble() {
    buf.putDouble(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutDoublePositional() {
    buf.putDouble(1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutByteArray() {
    buf.putByteArray(new byte[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutBooleanArray() {
    buf.putBooleanArray(new boolean[] {true}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutShortArray() {
    buf.putShortArray(new short[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutCharArray() {
    buf.putCharArray(new char[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutIntArray() {
    buf.putIntArray(new int[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutLongArray() {
    buf.putLongArray(new long[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutFloatArray() {
    buf.putFloatArray(new float[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testDoubleByteArray() {
    buf.putDoubleArray(new double[] {1}, 0, 1);
  }

  // Now, test that various ways to obtain a read-only buffer produce a read-only buffer indeed

  @Test(expectedExceptions = AssertionError.class)
  public void testWritableMemoryAsBuffer() {
    WritableBuffer buf = (WritableBuffer) WritableMemory.allocate(8).asBuffer();
    buf.putInt(1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testWritableBufferRegion() {
    WritableBuffer buf = (WritableBuffer) WritableMemory.allocate(8).asWritableBuffer().region();
    buf.putInt(1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testWritableBufferDuplicate() {
    WritableBuffer buf = (WritableBuffer) WritableMemory.allocate(8).asWritableBuffer().duplicate();
    buf.putInt(1);
  }
}
