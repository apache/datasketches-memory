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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

public class MemoryReadWriteSafetyTest {

  // Test various operations with read-only Memory

  final WritableMemory mem = (WritableMemory) Memory.wrap(new byte[8]);

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutByte() {
    mem.putByte(0, (byte) 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutShort() {
    mem.putShort(0, (short) 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutChar() {
    mem.putChar(0, (char) 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutInt() {
    mem.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutLong() {
    mem.putLong(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutFloat() {
    mem.putFloat(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutDouble() {
    mem.putDouble(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutByteArray() {
    mem.putByteArray(0, new byte[] {1}, 0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutShortArray() {
    mem.putShortArray(0, new short[] {1}, 0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutCharArray() {
    mem.putCharArray(0, new char[] {1}, 0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutIntArray() {
    mem.putIntArray(0, new int[] {1}, 0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutLongArray() {
    mem.putLongArray(0, new long[] {1}, 0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testPutFloatArray() {
    mem.putFloatArray(0, new float[] {1}, 0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDoubleByteArray() {
    mem.putDoubleArray(0, new double[] {1}, 0, 1);
  }

  // Now, test that various ways to obtain a read-only memory produce a read-only memory indeed

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testWritableMemoryRegion() {
    WritableMemory mem1 = (WritableMemory) WritableMemory.allocate(8).region(0, 8);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testByteArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new byte[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testByteArrayWrapWithBO() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new byte[8], ByteOrder.nativeOrder());
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testByteArrayWrapWithOffsetsAndBO() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new byte[8], 0, 4, ByteOrder.nativeOrder());
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testShortArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new short[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCharArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new char[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testIntArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new int[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testLongArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new long[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testFloatArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new float[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDoubleArrayWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(new double[8]);
    mem1.putInt(0, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testByteBufferWrap() {
    WritableMemory mem1 = (WritableMemory) Memory.wrap(ByteBuffer.allocate(8));
    mem1.putInt(0, 1);
  }

  //@SuppressWarnings("resource")
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testMapFile() throws Exception {
    File tempFile = File.createTempFile("test", null);
    tempFile.deleteOnExit();
    try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
      raf.setLength(8);
      Memory mem = null;
      //System.out.println(UtilTest.getFileAttributes(tempFile));
      try (ResourceScope scope = (mem = Memory.map(tempFile)).scope()) {
        ((WritableMemory) mem).putInt(0, 1);
      }
    }
  }

  @SuppressWarnings("resource")
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testMapFileWithOffsetsAndBO() throws Exception {
    File tempFile = File.createTempFile("test", "test");
    tempFile.deleteOnExit();
    new RandomAccessFile(tempFile, "rw").setLength(8);
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      Memory mem = Memory.map(tempFile, 0, 4, scope, ByteOrder.nativeOrder());
      ((WritableMemory) mem).putInt(0, 1);
    }
  }

  @Test(expectedExceptions = IOException.class)
  public void testMapFileBeyondTheFileSize() throws Exception {
    File tempFile = File.createTempFile("test", "test");
    tempFile.deleteOnExit();
    try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
      raf.setLength(8);
      try (ResourceScope scope = ResourceScope.newConfinedScope()) {
        Memory.map(tempFile, 0, 16, scope, ByteOrder.nativeOrder());
      }
    }
  }
}
