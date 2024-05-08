/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.ReadOnlyException;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class MemoryReadWriteSafetyTest {

  // Test various operations with read-only Memory

  final WritableMemory mem = (WritableMemory) Memory.wrap(new byte[8]);

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutByte() {
    mem.putByte(0, (byte) 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutBoolean() {
    mem.putBoolean(0, true);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutShort() {
    mem.putShort(0, (short) 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutChar() {
    mem.putChar(0, (char) 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutInt() {
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutLong() {
    mem.putLong(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testPutFloat() {
    mem.putFloat(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
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

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testWritableMemoryRegion() {
    WritableMemory mem1 = (WritableMemory) WritableMemory.allocate(8).region(0, 8);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testByteArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new byte[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testByteArrayWrapWithBO() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new byte[8], ByteOrder.nativeOrder());
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testByteArrayWrapWithOffsetsAndBO() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new byte[8], 0, 4, ByteOrder.nativeOrder());
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testBooleanArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new boolean[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testShortArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new short[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testCharArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new char[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testIntArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new int[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testLongArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new long[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testFloatArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new float[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testDoubleArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new double[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testByteBufferWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(ByteBuffer.allocate(8));
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testMapFile() throws Exception {
    File tempFile;
    try {
      tempFile = File.createTempFile("test", ".tmp", null);
      Files.write(tempFile.toPath(), "ipsum".getBytes(), StandardOpenOption.APPEND);
      //tempFile.setReadOnly();
    } catch (IllegalArgumentException | IOException | SecurityException e) { throw new RuntimeException(e); }
    try (Memory mem = Memory.map(tempFile)) { //Memory is RO
      ((WritableMemory) mem).putInt(0, 1);
    }
    tempFile.delete();
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testWritableMapWithROFile() {
    File tempFile;
    try {
      tempFile = File.createTempFile("test", ".tmp", null);
      Files.write(tempFile.toPath(), "ipsum".getBytes(), StandardOpenOption.APPEND);
      tempFile.setReadOnly();
    } catch (IllegalArgumentException | IOException | SecurityException e) { throw new RuntimeException(e); }
    try (WritableMemory mem = WritableMemory.writableMap(tempFile)) { //File is RO
      mem.putInt(0, 1);
    }
    tempFile.delete();
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void testMapFileWithOffsetsAndBO() {
    File tempFile;
    try {
      tempFile = File.createTempFile("test", ".tmp", null);
      Files.write(tempFile.toPath(), "ipsum".getBytes(), StandardOpenOption.APPEND);
      //tempFile.setReadOnly();
    } catch (IllegalArgumentException | IOException | SecurityException e) { throw new RuntimeException(e); }
    try (Memory mem = Memory.map(tempFile, 0, 4, ByteOrder.nativeOrder())) { //Memory is RO
      ((WritableMemory) mem).putInt(0, 1);
    }
    tempFile.delete();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMapFileBeyondTheFileSize() {
    File tempFile;
    try {
      tempFile = File.createTempFile("test", ".tmp", null);
      Files.write(tempFile.toPath(), "ipsum".getBytes(), StandardOpenOption.APPEND);
      //tempFile.setReadOnly();
    } catch (IllegalArgumentException | IOException | SecurityException e) { throw new RuntimeException(e); }
    try (Memory mem = Memory.map(tempFile, 0, 16, ByteOrder.nativeOrder())) {
      //Read-only mode and requested map length is greater than current file length:
      //  Requested Length = 16, Current File Length = 5
    }
    tempFile.delete();
  }
}
