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
    wmem.putCharArray(0, srcArray, 0, half);             //put*Array(add, src[], srcOff, len)
    for (int i = 0; i < half; i++) {
      wmem.putChar((half + i) * CB, srcArray[half + i]); //put*(add, value)
    }
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * CB, NBO);
    wmem.copyTo(0, wmem2, 0, len * CB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wmem2.getChar(i * CB)));
    }
    //get
    char[] dstArray = new char[len];
    wmem.getCharArray(0, dstArray, 0, half);             //get*Array(add, dst[], dstOff, len)
    for (int i = half; i < len; i++) {
      dstArray[i] = wmem.getChar(i * CB);                //get*(add)
    }
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * DB, NNBO);
    wmem.putDoubleArray(0, srcArray, 0, half);             //put*Array(add, src[], srcOff, len)
    for (int i = 0; i < half; i++) {
      wmem.putDouble((half + i) * DB, srcArray[half + i]); //put*(add, value)
    }
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * DB, NBO);
    wmem.copyTo(0, wmem2, 0, len * DB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.doubleReverseBytes(wmem2.getDouble(i * DB)));
    }
    //get
    double[] dstArray = new double[len];
    wmem.getDoubleArray(0, dstArray, 0, half);             //get*Array(add, dst[], dstOff, len)
    for (int i = half; i < len; i++) {
      dstArray[i] = wmem.getDouble(i * DB);                //get*(add)
    }
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * FB, NNBO);
    wmem.putFloatArray(0, srcArray, 0, half);             //put*Array(add, src[], srcOff, len)
    for (int i = 0; i < half; i++) {
      wmem.putFloat((half + i) * FB, srcArray[half + i]); //put*(add, value)
    }
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * FB, NBO);
    wmem.copyTo(0, wmem2, 0, len * FB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.floatReverseBytes(wmem2.getFloat(i * FB)));
    }
    //get
    float[] dstArray = new float[len];
    wmem.getFloatArray(0, dstArray, 0, half);             //get*Array(add, dst[], dstOff, len)
    for (int i = half; i < len; i++) {
      dstArray[i] = wmem.getFloat(i * FB);                //get*(add)
    }
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * IB, NNBO);
    wmem.putIntArray(0, srcArray, 0, half);             //put*Array(add, src[], srcOff, len)
    for (int i = 0; i < half; i++) {
      wmem.putInt((half + i) * IB, srcArray[half + i]); //put*(add, value)
    }
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * IB, NBO);
    wmem.copyTo(0, wmem2, 0, len * IB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wmem2.getInt(i * IB)));
    }
    //get
    int[] dstArray = new int[len];
    wmem.getIntArray(0, dstArray, 0, half);             //get*Array(add, dst[], dstOff, len)
    for (int i = half; i < len; i++) {
      dstArray[i] = wmem.getInt(i * IB);                //get*(add)
    }
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * LB, NNBO);
    wmem.putLongArray(0, srcArray, 0, half);             //put*Array(add, src[], srcOff, len)
    for (int i = 0; i < half; i++) {
      wmem.putLong((half + i) * LB, srcArray[half + i]); //put*(add, value)
    }
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * LB, NBO);
    wmem.copyTo(0, wmem2, 0, len * LB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wmem2.getLong(i * LB)));
    }
    //get
    long[] dstArray = new long[len];
    wmem.getLongArray(0, dstArray, 0, half);             //get*Array(add, dst[], dstOff, len)
    for (int i = half; i < len; i++) {
      dstArray[i] = wmem.getLong(i *LB);                 //get*(add)
    }
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * SB, NNBO);
    wmem.putShortArray(0, srcArray, 0, half);             //put*Array(add, src[], srcOff, len)
    for (int i = 0; i < half; i++) {
      wmem.putShort((half + i) * SB, srcArray[half + i]); //put*(add, value)
    }
    //confirm
    WritableMemory wmem2 = WritableMemory.allocate(len * SB, NBO);
    wmem.copyTo(0, wmem2, 0, len * SB);
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wmem2.getShort(i * SB)));
    }
    //get
    short[] dstArray = new short[len];
    wmem.getShortArray(0, dstArray, 0, half);             //get*Array(add, dst[], dstOff, len)
    for (int i = half; i < len; i++) {
      dstArray[i] = wmem.getShort(i *SB);                 //get*(add)
    }
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
