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

import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NonNativeWritableMemoryImplTest {
  private byte[] bArr = new byte[8];
  private final WritableMemory wmem = WritableMemory.writableWrap(bArr, NON_NATIVE_BYTE_ORDER);

  //Check primitives

  @Test
  public void checkPutGetNonNativeCharacters() {
    char[] srcArray = { 'a','b','c','d','e','f','g','h' };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Character.BYTES, NON_NATIVE_BYTE_ORDER);
    //put
    wmem.putChar(0, srcArray[0]);                     //put*(add, value)
    wmem.putCharArray(1 * Character.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putChar(3 * Character.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putChar(i * Character.BYTES, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Character.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Character.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wmem2.getChar(i * Character.BYTES)));
    }
    //get
    char[] dstArray = new char[len];
    dstArray[0] = wmem.getChar(0);                    //get*(add)
    wmem.getCharArray(1 * Character.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getChar(3 * Character.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getChar(i * Character.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Double.BYTES, NON_NATIVE_BYTE_ORDER);
    //put
    wmem.putDouble(0, srcArray[0]);                     //put*(add, value)
    wmem.putDoubleArray(1 * Double.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putDouble(3 * Double.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putDouble(i * Double.BYTES, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Double.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Double.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.doubleReverseBytes(wmem2.getDouble(i * Double.BYTES)));
    }
    //get
    double[] dstArray = new double[len];
    dstArray[0] = wmem.getDouble(0);                    //get*(add)
    wmem.getDoubleArray(1 * Double.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getDouble(3 * Double.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getDouble(i * Double.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Float.BYTES, NON_NATIVE_BYTE_ORDER);
    //put
    wmem.putFloat(0, srcArray[0]);                     //put*(add, value)
    wmem.putFloatArray(1 * Float.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putFloat(3 * Float.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putFloat(i * Float.BYTES, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Float.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Float.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.floatReverseBytes(wmem2.getFloat(i * Float.BYTES)));
    }
    //get
    float[] dstArray = new float[len];
    dstArray[0] = wmem.getFloat(0);                    //get*(add)
    wmem.getFloatArray(1 * Float.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getFloat(3 * Float.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getFloat(i * Float.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Integer.BYTES, NON_NATIVE_BYTE_ORDER);
    //put
    wmem.putInt(0, srcArray[0]);                     //put*(add, value)
    wmem.putIntArray(1 * Integer.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putInt(3 * Integer.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putInt(i * Integer.BYTES, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Integer.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Integer.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wmem2.getInt(i * Integer.BYTES)));
    }
    //get
    int[] dstArray = new int[len];
    dstArray[0] = wmem.getInt(0);                    //get*(add)
    wmem.getIntArray(1 * Integer.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getInt(3 * Integer.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getInt(i * Integer.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Long.BYTES, NON_NATIVE_BYTE_ORDER);
    //put
    wmem.putLong(0, srcArray[0]);                     //put*(add, value)
    wmem.putLongArray(1 * Long.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putLong(3 * Long.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putLong(i * Long.BYTES, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Long.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Long.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wmem2.getLong(i * Long.BYTES)));
    }
    //get
    long[] dstArray = new long[len];
    dstArray[0] = wmem.getLong(0);                    //get*(add)
    wmem.getLongArray(1 * Long.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getLong(3 * Long.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getLong(i * Long.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Short.BYTES, NON_NATIVE_BYTE_ORDER);
    //put
    wmem.putShort(0, srcArray[0]);                     //put*(add, value)
    wmem.putShortArray(1 * Short.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putShort(3 * Short.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putShort(i * Short.BYTES, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * Short.BYTES, NATIVE_BYTE_ORDER);
    wmem.copyTo(0, wmem2, 0, len * Short.BYTES);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wmem2.getShort(i * Short.BYTES)));
    }
    //get
    short[] dstArray = new short[len];
    dstArray[0] = wmem.getShort(0);                    //get*(add)
    wmem.getShortArray(1 * Short.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getShort(3 * Short.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getShort(i * Short.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  //check Atomic Write Methods

  //check Region
  @Test
  public void checkRegion() {
    WritableMemory wreg = wmem.writableRegion(0, wmem.getCapacity());
    assertEquals(wreg.getTypeByteOrder(), NON_NATIVE_BYTE_ORDER);
  }

}
