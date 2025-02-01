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

import static org.apache.datasketches.memory.internal.NonNativeWritableMemoryImplTest.doubleReverseBytes;
import static org.apache.datasketches.memory.internal.NonNativeWritableMemoryImplTest.floatReverseBytes;
import static org.apache.datasketches.memory.internal.ResourceImpl.NATIVE_BYTE_ORDER;
import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NonNativeWritableBufferImplTest {

//Check primitives

  @Test
  public void checkPutGetNonNativeCharacters() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * Character.BYTES, NON_NATIVE_BYTE_ORDER).asWritableBuffer();
    wbuf.putCharArray(srcArray, 0, half);
    wbuf.putCharArray(srcArray, half, half);
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * Character.BYTES, NATIVE_BYTE_ORDER).asWritableBuffer();
    for (int i = 0; i < len * Character.BYTES; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wbuf2.getChar()));
    }
    wbuf2.resetPosition();
    //get
    char[] dstArray = new char[len];
    wbuf.getCharArray(dstArray, 0, half);
    wbuf.getCharArray(dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * Double.BYTES, NON_NATIVE_BYTE_ORDER).asWritableBuffer();
    wbuf.putDoubleArray(srcArray, 0, half);
    wbuf.putDoubleArray(srcArray, half, half);
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * Double.BYTES, NATIVE_BYTE_ORDER).asWritableBuffer();
    for (int i = 0; i < len * Double.BYTES; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == doubleReverseBytes(wbuf2.getDouble()));
    }
    wbuf2.resetPosition();
    //get
    double[] dstArray = new double[len];
    wbuf.getDoubleArray(dstArray, 0, half);
    wbuf.getDoubleArray(dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * Float.BYTES, NON_NATIVE_BYTE_ORDER).asWritableBuffer();
    wbuf.putFloatArray(srcArray, 0, half);
    wbuf.putFloatArray(srcArray, half, half);
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * Float.BYTES, NATIVE_BYTE_ORDER).asWritableBuffer();
    for (int i = 0; i < len * Float.BYTES; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == floatReverseBytes(wbuf2.getFloat()));
    }
    wbuf2.resetPosition();
    //get
    float[] dstArray = new float[len];
    wbuf.getFloatArray(dstArray, 0, half);
    wbuf.getFloatArray(dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * Integer.BYTES, NON_NATIVE_BYTE_ORDER).asWritableBuffer();
    wbuf.putIntArray(srcArray, 0, half);
    wbuf.putIntArray(srcArray, half, half);
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * Integer.BYTES, NATIVE_BYTE_ORDER).asWritableBuffer();
    for (int i = 0; i < len * Integer.BYTES; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wbuf2.getInt()));
    }
    wbuf2.resetPosition();
    //get
    int[] dstArray = new int[len];
    wbuf.getIntArray(dstArray, 0, half);
    wbuf.getIntArray(dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * Long.BYTES, NON_NATIVE_BYTE_ORDER).asWritableBuffer();
    wbuf.putLongArray(srcArray, 0, half);
    wbuf.putLongArray(srcArray, half, half);
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * Long.BYTES, NATIVE_BYTE_ORDER).asWritableBuffer();
    for (int i = 0; i < len * Long.BYTES; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wbuf2.getLong()));
    }
    wbuf2.resetPosition();
    //get
    long[] dstArray = new long[len];
    wbuf.getLongArray(dstArray, 0, half);
    wbuf.getLongArray(dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * Short.BYTES, NON_NATIVE_BYTE_ORDER).asWritableBuffer();
    wbuf.putShortArray(srcArray, 0, half);
    wbuf.putShortArray(srcArray, half, half);
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * Short.BYTES, NATIVE_BYTE_ORDER).asWritableBuffer();
    for (int i = 0; i < len * Short.BYTES; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wbuf2.getShort()));
    }
    wbuf2.resetPosition();
    //get
    short[] dstArray = new short[len];
    wbuf.getShortArray(dstArray, 0, half);
    wbuf.getShortArray(dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  //check Duplicate, Region
  @Test
  public void checkDuplicate() {
    byte[] bArr = new byte[8];
    WritableMemory wmem = WritableMemory.writableWrap(bArr, NON_NATIVE_BYTE_ORDER);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    WritableBuffer wdup = wbuf.writableDuplicate();
    assertEquals(wdup.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);

    WritableBuffer wreg = wbuf.writableRegion();
    assertEquals(wreg.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
  }

  @Test
  public void checkConversionByteOrder() {
    byte[] bArr = new byte[8];
    bArr[1] = 1;
    WritableMemory wmem = WritableMemory.writableWrap(bArr, NON_NATIVE_BYTE_ORDER);
    assertEquals(wmem.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
    assertEquals(wmem.getChar(0), 1);

    Buffer buf = wmem.asBuffer();
    assertEquals(buf.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER); //
    assertEquals(buf.getChar(0), 1);

    Buffer dup = buf.duplicate();
    assertEquals(dup.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
    assertEquals(dup.getChar(0), 1);

    Buffer reg = buf.region();
    assertEquals(reg.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
    assertEquals(reg.getChar(0), 1);

    Memory mem = reg.asMemory();
    assertEquals(mem.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
    assertEquals(mem.getChar(0), 1);

    Memory mreg = mem.region(0, 8);
    assertEquals(mreg.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
    assertEquals(mreg.getChar(0), 1);
  }

  @Test
  public void checkPutIntArray() {
    WritableMemory wmem = WritableMemory.allocate(12, NON_NATIVE_BYTE_ORDER);
    WritableBuffer wbuf = wmem.asWritableBuffer(NON_NATIVE_BYTE_ORDER);

    wbuf.putInt(1);
    int[] array = new int[] { 2 };
    wbuf.putIntArray(array, 0, 1);
    wbuf.putInt(3);

    Buffer buf = wmem.asWritableBuffer();
    assertEquals(buf.getInt(), 1);
    assertEquals(buf.getInt(), 2);
    assertEquals(buf.getInt(), 3);
  }

}
