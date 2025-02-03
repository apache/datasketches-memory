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
    WritableBuffer wbuf = WritableMemory.allocate(len * CB, NNBO).asWritableBuffer();
    //put
    wbuf.putChar(srcArray[0]);                        //put*(value)
    wbuf.putCharArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putChar(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putChar(i * CB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * CB, NBO).asWritableBuffer();
    for (int i = 0; i < len * CB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Character.reverseBytes(wbuf2.getChar()));
    }
    //get
    wbuf.resetPosition();
    char[] dstArray = new char[len];
    dstArray[0] = wbuf.getChar();                     //get*()
    wbuf.getCharArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getChar();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getChar(i * CB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * DB, NNBO).asWritableBuffer();
    //put
    wbuf.putDouble(srcArray[0]);                        //put*(value)
    wbuf.putDoubleArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putDouble(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putDouble(i * DB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * DB, NBO).asWritableBuffer();
    for (int i = 0; i < len * DB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.doubleReverseBytes(wbuf2.getDouble()));
    }
    //get
    wbuf.resetPosition();
    double[] dstArray = new double[len];
    dstArray[0] = wbuf.getDouble();                     //get*()
    wbuf.getDoubleArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getDouble();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getDouble(i * DB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * FB, NNBO).asWritableBuffer();
    //put
    wbuf.putFloat(srcArray[0]);                        //put*(value)
    wbuf.putFloatArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putFloat(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putFloat(i * FB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * FB, NBO).asWritableBuffer();
    for (int i = 0; i < len * FB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == UtilForTest.floatReverseBytes(wbuf2.getFloat()));
    }
    //get
    wbuf.resetPosition();
    float[] dstArray = new float[len];
    dstArray[0] = wbuf.getFloat();                     //get*()
    wbuf.getFloatArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getFloat();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getFloat(i * FB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * IB, NNBO).asWritableBuffer();
    //put
    wbuf.putInt(srcArray[0]);                        //put*(value)
    wbuf.putIntArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putInt(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putInt(i * IB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * IB, NBO).asWritableBuffer();
    for (int i = 0; i < len * IB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Integer.reverseBytes(wbuf2.getInt()));
    }
    //get
    wbuf.resetPosition();
    int[] dstArray = new int[len];
    dstArray[0] = wbuf.getInt();                     //get*()
    wbuf.getIntArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getInt();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getInt(i * IB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * LB, NNBO).asWritableBuffer();
    //put
    wbuf.putLong(srcArray[0]);                        //put*(value)
    wbuf.putLongArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putLong(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putLong(i * LB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * LB, NBO).asWritableBuffer();
    for (int i = 0; i < len * LB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Long.reverseBytes(wbuf2.getLong()));
    }
    //get
    wbuf.resetPosition();
    long[] dstArray = new long[len];
    dstArray[0] = wbuf.getLong();                     //get*()
    wbuf.getLongArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getLong();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getLong(i * LB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNonNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * SB, NNBO).asWritableBuffer();
    //put
    wbuf.putShort(srcArray[0]);                        //put*(value)
    wbuf.putShortArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putShort(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putShort(i * SB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //confirm
    WritableBuffer wbuf2 = WritableMemory.allocate(len * SB, NBO).asWritableBuffer();
    for (int i = 0; i < len * SB; i++) { wbuf2.putByte(wbuf.getByte()); }
    wbuf2.resetPosition();
    for (int i = 0; i < len; i++) {
      assertTrue(srcArray[i] == Short.reverseBytes(wbuf2.getShort()));
    }
    //get
    wbuf.resetPosition();
    short[] dstArray = new short[len];
    dstArray[0] = wbuf.getShort();                     //get*()
    wbuf.getShortArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getShort();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getShort(i * SB); } //get*(add)
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
