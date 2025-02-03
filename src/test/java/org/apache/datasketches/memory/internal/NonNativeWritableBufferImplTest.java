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
    char[] srcArray = { 'a','b','c','d','e','f','g','h' };
    final int len = srcArray.length;
    final int half = len / 2;
    final int qtr = len / 4;
    WritableBuffer wbuf = WritableMemory.allocate(len * CB, NNBO).asWritableBuffer();
    //put
    wbuf.putCharArray(srcArray, 0, qtr);              //put*Array(src[], srcOff, len)
    wbuf.putChar(qtr * CB, srcArray[qtr]);            //put*(add, value)
    wbuf.putChar((qtr + 1) * CB, srcArray[qtr + 1]);  //put*(add, value)
    wbuf.setPosition(half * CB);
    for (int i = half; i < len; i++) { wbuf.putChar(srcArray[i]); } //put*(value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * CB, NBO).asWritableBuffer();
    for (int i = 0; i < len * CB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wbuf2.getChar()));
    }
    //get
    wbuf2.resetPosition();
    char[] dstArray = new char[len];
    wbuf.getCharArray(dstArray, 0, qtr);              //get*Array(dst[], dstOff, len)
    dstArray[qtr] = wbuf.getChar(qtr * CB);           //get*(add)
    dstArray[qtr + 1] = wbuf.getChar((qtr + 1) * CB); //get*(add)
    wbuf.setPosition(half * CB);
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getChar(); } //get*()
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    final int qtr = len / 4;
    WritableBuffer wbuf = WritableMemory.allocate(len * DB, NNBO).asWritableBuffer();
    //put
    wbuf.putDoubleArray(srcArray, 0, qtr);              //put*Array(src[], srcOff, len)
    wbuf.putDouble(qtr * DB, srcArray[qtr]);            //put*(add, value)
    wbuf.putDouble((qtr + 1) * DB, srcArray[qtr + 1]);  //put*(add, value)
    wbuf.setPosition(half * DB);
    for (int i = half; i < len; i++) { wbuf.putDouble(srcArray[i]); } //put*(value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * DB, NBO).asWritableBuffer();
    for (int i = 0; i < len * DB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.doubleReverseBytes(wbuf2.getDouble()));
    }
    //get
    wbuf2.resetPosition();
    double[] dstArray = new double[len];
    wbuf.getDoubleArray(dstArray, 0, qtr);              //get*Array(dst[], dstOff, len)
    dstArray[qtr] = wbuf.getDouble(qtr * DB);           //get*(add)
    dstArray[qtr + 1] = wbuf.getDouble((qtr + 1) * DB); //get*(add)
    wbuf.setPosition(half * DB);
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getDouble(); } //get*()
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    final int qtr = len / 4;
    WritableBuffer wbuf = WritableMemory.allocate(len * FB, NNBO).asWritableBuffer();
    //put
    wbuf.putFloatArray(srcArray, 0, qtr);              //put*Array(src[], srcOff, len)
    wbuf.putFloat(qtr * FB, srcArray[qtr]);            //put*(add, value)
    wbuf.putFloat((qtr + 1) * FB, srcArray[qtr + 1]);  //put*(add, value)
    wbuf.setPosition(half * FB);
    for (int i = half; i < len; i++) { wbuf.putFloat(srcArray[i]); } //put*(value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * FB, NBO).asWritableBuffer();
    for (int i = 0; i < len * FB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.floatReverseBytes(wbuf2.getFloat()));
    }
    //get
    wbuf2.resetPosition();
    float[] dstArray = new float[len];
    wbuf.getFloatArray(dstArray, 0, qtr);              //get*Array(dst[], dstOff, len)
    dstArray[qtr] = wbuf.getFloat(qtr * FB);           //get*(add)
    dstArray[qtr + 1] = wbuf.getFloat((qtr + 1) * FB); //get*(add)
    wbuf.setPosition(half * FB);
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getFloat(); } //get*()
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    final int qtr = len / 4;
    WritableBuffer wbuf = WritableMemory.allocate(len * IB, NNBO).asWritableBuffer();
    //put
    wbuf.putIntArray(srcArray, 0, qtr);              //put*Array(src[], srcOff, len)
    wbuf.putInt(qtr * IB, srcArray[qtr]);            //put*(add, value)
    wbuf.putInt((qtr + 1) * IB, srcArray[qtr + 1]);  //put*(add, value)
    wbuf.setPosition(half * IB);
    for (int i = half; i < len; i++) { wbuf.putInt(srcArray[i]); } //put*(value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * IB, NBO).asWritableBuffer();
    for (int i = 0; i < len * IB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wbuf2.getInt()));
    }
    //get
    wbuf2.resetPosition();
    int[] dstArray = new int[len];
    wbuf.getIntArray(dstArray, 0, qtr);              //get*Array(dst[], dstOff, len)
    dstArray[qtr] = wbuf.getInt(qtr * IB);           //get*(add)
    dstArray[qtr + 1] = wbuf.getInt((qtr + 1) * IB); //get*(add)
    wbuf.setPosition(half * IB);
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getInt(); } //get*()
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    final int qtr = len / 4;
    WritableBuffer wbuf = WritableMemory.allocate(len * LB, NNBO).asWritableBuffer();
    //put
    wbuf.putLongArray(srcArray, 0, qtr);              //put*Array(src[], srcOff, len)
    wbuf.putLong(qtr * LB, srcArray[qtr]);            //put*(add, value)
    wbuf.putLong((qtr + 1) * LB, srcArray[qtr + 1]);  //put*(add, value)
    wbuf.setPosition(half * LB);
    for (int i = half; i < len; i++) { wbuf.putLong(srcArray[i]); } //put*(value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * LB, NBO).asWritableBuffer();
    for (int i = 0; i < len * LB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wbuf2.getLong()));
    }
    //get
    wbuf2.resetPosition();
    long[] dstArray = new long[len];
    wbuf.getLongArray(dstArray, 0, qtr);              //get*Array(dst[], dstOff, len)
    dstArray[qtr] = wbuf.getLong(qtr * LB);           //get*(add)
    dstArray[qtr + 1] = wbuf.getLong((qtr + 1) * LB); //get*(add)
    wbuf.setPosition(half * LB);
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getLong(); } //get*()
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    final int qtr = len / 4;
    WritableBuffer wbuf = WritableMemory.allocate(len * SB, NNBO).asWritableBuffer();
    //put
    wbuf.putShortArray(srcArray, 0, qtr);              //put*Array(src[], srcOff, len)
    wbuf.putShort(qtr * SB, srcArray[qtr]);            //put*(add, value)
    wbuf.putShort((qtr + 1) * SB, srcArray[qtr + 1]);  //put*(add, value)
    wbuf.setPosition(half * SB);
    for (int i = half; i < len; i++) { wbuf.putShort(srcArray[i]); } //put*(value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * SB, NBO).asWritableBuffer();
    for (int i = 0; i < len * SB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf.resetPosition();
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wbuf2.getShort()));
    }
    //get
    wbuf2.resetPosition();
    short[] dstArray = new short[len];
    wbuf.getShortArray(dstArray, 0, qtr);              //get*Array(dst[], dstOff, len)
    dstArray[qtr] = wbuf.getShort(qtr * SB);           //get*(add)
    dstArray[qtr + 1] = wbuf.getShort((qtr + 1) * SB); //get*(add)
    wbuf.setPosition(half * SB);
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getShort(); } //get*()
    assertEquals(srcArray, dstArray);
  }

  //check Duplicate, Region
  @Test
  public void checkDuplicate() {
    byte[] bArr = new byte[8];
    WritableMemory wmem = WritableMemory.writableWrap(bArr, NNBO);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    WritableBuffer wdup = wbuf.writableDuplicate();
    assertEquals(wdup.getTypeByteOrder(), NNBO);

    WritableBuffer wreg = wbuf.writableRegion();
    assertEquals(wreg.getTypeByteOrder(), NNBO);
  }

  @Test
  public void checkConversionByteOrder() {
    byte[] bArr = new byte[8];
    bArr[1] = 1;
    WritableMemory wmem = WritableMemory.writableWrap(bArr, NNBO);
    assertEquals(wmem.getTypeByteOrder(), NNBO);
    assertEquals(wmem.getChar(0), 1);

    Buffer buf = wmem.asBuffer();
    assertEquals(buf.getTypeByteOrder(), NNBO); //
    assertEquals(buf.getChar(0), 1);

    Buffer dup = buf.duplicate();
    assertEquals(dup.getTypeByteOrder(), NNBO);
    assertEquals(dup.getChar(0), 1);

    Buffer reg = buf.region();
    assertEquals(reg.getTypeByteOrder(), NNBO);
    assertEquals(reg.getChar(0), 1);

    Memory mem = reg.asMemory();
    assertEquals(mem.getTypeByteOrder(), NNBO);
    assertEquals(mem.getChar(0), 1);

    Memory mreg = mem.region(0, 8);
    assertEquals(mreg.getTypeByteOrder(), NNBO);
    assertEquals(mreg.getChar(0), 1);
  }

  @Test
  public void checkPutIntArray() {
    WritableMemory wmem = WritableMemory.allocate(12, NNBO);
    WritableBuffer wbuf = wmem.asWritableBuffer(NNBO);

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
