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

import static org.apache.datasketches.memory.internal.UtilForTest.CB;
import static org.apache.datasketches.memory.internal.UtilForTest.DB;
import static org.apache.datasketches.memory.internal.UtilForTest.FB;
import static org.apache.datasketches.memory.internal.UtilForTest.IB;
import static org.apache.datasketches.memory.internal.UtilForTest.LB;
import static org.apache.datasketches.memory.internal.UtilForTest.NBO;
import static org.apache.datasketches.memory.internal.UtilForTest.NNBO;
import static org.apache.datasketches.memory.internal.UtilForTest.SB;
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
  private final WritableMemory wmem = WritableMemory.writableWrap(bArr, NNBO);

  //Check primitives

  @Test
  public void checkPutGetNonNativeCharacters() {
    char[] srcArray = { 'a','b','c','d','e','f','g','h' };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * CB, NNBO);
    //put
    wmem.putChar(0, srcArray[0]);                     //put*(add, value)
    wmem.putCharArray(1 * CB, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putChar(3 * CB, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putChar(i * CB, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * CB, NBO);
    wmem.copyTo(0, wmem2, 0, len * CB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wmem2.getChar(i * CB)));
    }
    //get
    char[] dstArray = new char[len];
    dstArray[0] = wmem.getChar(0);                    //get*(add)
    wmem.getCharArray(1 * CB, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getChar(3 * CB);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getChar(i * CB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * DB, NNBO);
    //put
    wmem.putDouble(0, srcArray[0]);                     //put*(add, value)
    wmem.putDoubleArray(1 * DB, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putDouble(3 * DB, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putDouble(i * DB, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * DB, NBO);
    wmem.copyTo(0, wmem2, 0, len * DB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.doubleReverseBytes(wmem2.getDouble(i * DB)));
    }
    //get
    double[] dstArray = new double[len];
    dstArray[0] = wmem.getDouble(0);                    //get*(add)
    wmem.getDoubleArray(1 * DB, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getDouble(3 * DB);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getDouble(i * DB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * FB, NNBO);
    //put
    wmem.putFloat(0, srcArray[0]);                     //put*(add, value)
    wmem.putFloatArray(1 * FB, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putFloat(3 * FB, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putFloat(i * FB, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * FB, NBO);
    wmem.copyTo(0, wmem2, 0, len * FB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.floatReverseBytes(wmem2.getFloat(i * FB)));
    }
    //get
    float[] dstArray = new float[len];
    dstArray[0] = wmem.getFloat(0);                    //get*(add)
    wmem.getFloatArray(1 * FB, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getFloat(3 * FB);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getFloat(i * FB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * IB, NNBO);
    //put
    wmem.putInt(0, srcArray[0]);                     //put*(add, value)
    wmem.putIntArray(1 * IB, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putInt(3 * IB, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putInt(i * IB, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * IB, NBO);
    wmem.copyTo(0, wmem2, 0, len * IB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wmem2.getInt(i * IB)));
    }
    //get
    int[] dstArray = new int[len];
    dstArray[0] = wmem.getInt(0);                    //get*(add)
    wmem.getIntArray(1 * IB, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getInt(3 * IB);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getInt(i * IB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * LB, NNBO);
    //put
    wmem.putLong(0, srcArray[0]);                     //put*(add, value)
    wmem.putLongArray(1 * LB, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putLong(3 * LB, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putLong(i * LB, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * LB, NBO);
    wmem.copyTo(0, wmem2, 0, len * LB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wmem2.getLong(i * LB)));
    }
    //get
    long[] dstArray = new long[len];
    dstArray[0] = wmem.getLong(0);                    //get*(add)
    wmem.getLongArray(1 * LB, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getLong(3 * LB);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getLong(i * LB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * SB, NNBO);
    //put
    wmem.putShort(0, srcArray[0]);                     //put*(add, value)
    wmem.putShortArray(1 * SB, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putShort(3 * SB, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putShort(i * SB, srcArray[i]); } //put*(add, value)
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * SB, NBO);
    wmem.copyTo(0, wmem2, 0, len * SB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wmem2.getShort(i * SB)));
    }
    //get
    short[] dstArray = new short[len];
    dstArray[0] = wmem.getShort(0);                    //get*(add)
    wmem.getShortArray(1 * SB, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getShort(3 * SB);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getShort(i * SB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  //check Atomic Write Methods

  //check Region
  @Test
  public void checkRegion() {
    WritableMemory wreg = wmem.writableRegion(0, wmem.getCapacity());
    assertEquals(wreg.getTypeByteOrder(), NNBO);
  }

}
