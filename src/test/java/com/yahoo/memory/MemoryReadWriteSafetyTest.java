/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class MemoryReadWriteSafetyTest {

  // Test various operations with read-only Memory

  final WritableMemory mem = (WritableMemory) Memory.wrap(new byte[8]);

  @Test(expectedExceptions = AssertionError.class)
  public void testPutByte() {
    mem.putByte(0, (byte) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutBoolean() {
    mem.putBoolean(0, true);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutShort() {
    mem.putShort(0, (short) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutChar() {
    mem.putChar(0, (char) 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutInt() {
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutLong() {
    mem.putLong(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutFloat() {
    mem.putFloat(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testPutDouble() {
    mem.putDouble(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutByteArray() {
    mem.putByteArray(0, new byte[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutBooleanArray() {
    mem.putBooleanArray(0, new boolean[] {true}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutShortArray() {
    mem.putShortArray(0, new short[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutCharArray() {
    mem.putCharArray(0, new char[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutIntArray() {
    mem.putIntArray(0, new int[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutLongArray() {
    mem.putLongArray(0, new long[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutFloatArray() {
    mem.putFloatArray(0, new float[] {1}, 0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testDoubleByteArray() {
    mem.putDoubleArray(0, new double[] {1}, 0, 1);
  }

  // Now, test that various ways to obtain a read-only memory produce a read-only memory indeed

  @Test(expectedExceptions = AssertionError.class)
  public void testWritableMemoryRegion() {
    WritableMemory mem = (WritableMemory) WritableMemory.allocate(8).region(0, 8);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testByteArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new byte[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testByteArrayWrapWithBO() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new byte[8], ByteOrder.nativeOrder());
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testByteArrayWrapWithOffsetsAndBO() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new byte[8], 0, 4, ByteOrder.nativeOrder());
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testBooleanArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new boolean[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testShortArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new short[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testCharArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new char[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testIntArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new int[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testLongArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new long[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testFloatArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new float[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testDoubleArrayWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(new double[8]);
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testByteBufferWrap() {
    WritableMemory mem = (WritableMemory) Memory.wrap(ByteBuffer.allocate(8));
    mem.putInt(0, 1);
  }

  @SuppressWarnings("resource")
  @Test(expectedExceptions = AssertionError.class)
  public void testMapFile() throws IOException {
    File tempFile = File.createTempFile("test", "test");
    tempFile.deleteOnExit();
    new RandomAccessFile(tempFile, "rw").setLength(8);
    try (MapHandle h = Memory.map(tempFile)) {
      ((WritableMemory) h.get()).putInt(0, 1);
    }
  }

  @SuppressWarnings("resource")
  @Test(expectedExceptions = AssertionError.class)
  public void testMapFileWithOffsetsAndBO() throws IOException {
    File tempFile = File.createTempFile("test", "test");
    tempFile.deleteOnExit();
    new RandomAccessFile(tempFile, "rw").setLength(8);
    try (MapHandle h = Memory.map(tempFile, 0, 4, ByteOrder.nativeOrder())) {
      ((WritableMemory) h.get()).putInt(0, 1);
    }
  }

  @SuppressWarnings("resource")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testMapFileBeyondTheFileSize() throws IOException {
    File tempFile = File.createTempFile("test", "test");
    tempFile.deleteOnExit();
    new RandomAccessFile(tempFile, "rw").setLength(8);
    try (MapHandle unused = Memory.map(tempFile, 0, 16, ByteOrder.nativeOrder())) {
    }
  }
}
