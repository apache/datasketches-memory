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

import static org.apache.datasketches.memory.internal.ResourceImpl.NATIVE_BYTE_ORDER;
import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NonNativeWritableMemoryImplTest {
  private byte[] bArr = new byte[8];
  private final WritableMemory wmem = WritableMemory.writableWrap(bArr, ByteOrder.BIG_ENDIAN);

  //Check primitives

  @Test
  public void checkPutGetNonNativeCharacters() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Character.BYTES, NON_NATIVE_BYTE_ORDER);
    wmem.putCharArray(0, srcArray, 0, half);
    wmem.putCharArray(half * Character.BYTES, srcArray, half, half);
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Character.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Character.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wmem2.getChar(i * Character.BYTES)));
    }
    //get
    char[] dstArray = new char[len];
    wmem.getCharArray(0, dstArray, 0, half);
    wmem.getCharArray(half * Character.BYTES, dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Double.BYTES, NON_NATIVE_BYTE_ORDER);
    wmem.putDoubleArray(0, srcArray, 0, half);
    wmem.putDoubleArray(half * Double.BYTES, srcArray, half, half);
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Double.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Double.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == doubleReverseBytes(wmem2.getDouble(i * Double.BYTES)));
    }
    //get
    double[] dstArray = new double[len];
    wmem.getDoubleArray(0, dstArray, 0, half);
    wmem.getDoubleArray(half * Double.BYTES, dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Float.BYTES, NON_NATIVE_BYTE_ORDER);
    wmem.putFloatArray(0, srcArray, 0, half);
    wmem.putFloatArray(half * Float.BYTES, srcArray, half, half);
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Float.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Float.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == floatReverseBytes(wmem2.getFloat(i * Float.BYTES)));
    }
    //get
    float[] dstArray = new float[len];
    wmem.getFloatArray(0, dstArray, 0, half);
    wmem.getFloatArray(half * Float.BYTES, dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Integer.BYTES, NON_NATIVE_BYTE_ORDER);
    wmem.putIntArray(0, srcArray, 0, half);
    wmem.putIntArray(half * Integer.BYTES, srcArray, half, half);
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Integer.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Integer.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wmem2.getInt(i * Integer.BYTES)));
    }
    //get
    int[] dstArray = new int[len];
    wmem.getIntArray(0, dstArray, 0, half);
    wmem.getIntArray(half * Integer.BYTES, dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Long.BYTES, NON_NATIVE_BYTE_ORDER);
    wmem.putLongArray(0, srcArray, 0, half);
    wmem.putLongArray(half * Long.BYTES, srcArray, half, half);
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Long.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Long.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wmem2.getLong(i * Long.BYTES)));
    }
    //get
    long[] dstArray = new long[len];
    wmem.getLongArray(0, dstArray, 0, half);
    wmem.getLongArray(half * Long.BYTES, dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Short.BYTES, NON_NATIVE_BYTE_ORDER);
    wmem.putShortArray(0, srcArray, 0, half);
    wmem.putShortArray(half * Short.BYTES, srcArray, half, half);
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Short.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Short.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wmem2.getShort(i * Short.BYTES)));
    }
    //get
    short[] dstArray = new short[len];
    wmem.getShortArray(0, dstArray, 0, half);
    wmem.getShortArray(half * Short.BYTES, dstArray, half, half);
    assertEquals(srcArray, dstArray);
  }

  //check Atomic Write Methods

  //check Region
  @Test
  public void checkRegion() {
    WritableMemory wreg = wmem.writableRegion(0, wmem.getCapacity());
    assertEquals(wreg.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  //Java does not provide reverse bytes on doubles or floats

  static double doubleReverseBytes(double value) {
    long longIn = Double.doubleToRawLongBits(value);
    long longOut = Long.reverseBytes(longIn);
    return Double.longBitsToDouble(longOut);
  }

  static float floatReverseBytes(float value) {
    int intIn = Float.floatToRawIntBits(value);
    int intOut = Integer.reverseBytes(intIn);
    return Float.intBitsToFloat(intOut);
  }

}
