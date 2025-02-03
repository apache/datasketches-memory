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

import java.lang.foreign.Arena;
import static org.apache.datasketches.memory.internal.UtilForTest.CB;
import static org.apache.datasketches.memory.internal.UtilForTest.DB;
import static org.apache.datasketches.memory.internal.UtilForTest.FB;
import static org.apache.datasketches.memory.internal.UtilForTest.IB;
import static org.apache.datasketches.memory.internal.UtilForTest.LB;
import static org.apache.datasketches.memory.internal.UtilForTest.NBO;
import static org.apache.datasketches.memory.internal.UtilForTest.NNBO;
import static org.apache.datasketches.memory.internal.UtilForTest.SB;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NativeWritableBufferImplTest {
  private static final MemoryRequestServer memReqSvr = Resource.defaultMemReqSvr;

  //Simple Native direct

  @Test
  public void checkNativeCapacityAndClose() throws Exception {
    int memCapacity = 64;
    WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, Arena.ofConfined());
    WritableBuffer wbuf = wmem.asWritableBuffer();
    assertEquals(wbuf.getCapacity(), memCapacity);

    wmem.getArena().close();
    assertFalse(wbuf.isAlive());
    try { wmem.getArena().close(); } catch (IllegalStateException e) { }
  }

  //Check primitives

  @Test
  public void checkPutGetBooleans() {
    boolean[] srcArray = {true, false, true, false, false, true, false, true};
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len, NBO).asWritableBuffer();
    //put
    for (int i = 0; i < half; i++) { wbuf.putBoolean(srcArray[i]); }       //put*(value)
    for (int i = half; i < len; i++) { wbuf.putBoolean(i, srcArray[i]); }  //put*(add, value)
    wbuf.resetPosition();
    //get
    boolean[] dstArray = new boolean[len];
    for (int i = 0; i < half; i++) { dstArray[i] = wbuf.getBoolean(); }    //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getBoolean(i); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetBytes() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len, NBO).asWritableBuffer();
    //put
    wbuf.putByte(srcArray[0]);                        //put*(value)
    wbuf.putByteArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putByte(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putByte(i, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //get
    byte[] dstArray = new byte[len];
    dstArray[0] = wbuf.getByte();                     //get*()
    wbuf.getByteArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getByte();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getByte(i); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeCharacters() {
    char[] srcArray = { 'a','b','c','d','e','f','g','h' };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * CB, NBO).asWritableBuffer();
    //put
    wbuf.putChar(srcArray[0]);                        //put*(value)
    wbuf.putCharArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putChar(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putChar(i * CB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //get
    char[] dstArray = new char[len];
    dstArray[0] = wbuf.getChar();                     //get*()
    wbuf.getCharArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getChar();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getChar(i * CB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * DB, NBO).asWritableBuffer();
    //put
    wbuf.putDouble(srcArray[0]);                        //put*(value)
    wbuf.putDoubleArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putDouble(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putDouble(i * DB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //get
    double[] dstArray = new double[len];
    dstArray[0] = wbuf.getDouble();                     //get*()
    wbuf.getDoubleArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getDouble();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getDouble(i * DB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * FB, NBO).asWritableBuffer();
    //put
    wbuf.putFloat(srcArray[0]);                        //put*(value)
    wbuf.putFloatArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putFloat(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putFloat(i * FB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //get
    float[] dstArray = new float[len];
    dstArray[0] = wbuf.getFloat();                     //get*()
    wbuf.getFloatArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getFloat();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getFloat(i * FB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * IB, NBO).asWritableBuffer();
    //put
    wbuf.putInt(srcArray[0]);                        //put*(value)
    wbuf.putIntArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putInt(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putInt(i * IB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //get
    int[] dstArray = new int[len];
    dstArray[0] = wbuf.getInt();                     //get*()
    wbuf.getIntArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getInt();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getInt(i * IB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeLongs() {
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
    //get
    long[] dstArray = new long[len];
    dstArray[0] = wbuf.getLong();                     //get*()
    wbuf.getLongArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getLong();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getLong(i * LB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableBuffer wbuf = WritableMemory.allocate(len * SB, NBO).asWritableBuffer();
    //put
    wbuf.putShort(srcArray[0]);                        //put*(value)
    wbuf.putShortArray(srcArray, 1, 2);                //put*Array(src[], srcOff, len)
    wbuf.putShort(srcArray[3]);                        //put*(value)
    for (int i = half; i < len; i++) { wbuf.putShort(i * SB, srcArray[i]); } //put*(add, value)
    wbuf.resetPosition();
    //get
    short[] dstArray = new short[len];
    dstArray[0] = wbuf.getShort();                     //get*()
    wbuf.getShortArray(dstArray, 1, 2);                //get*Array(dst[], dstOff, len)
    dstArray[3] = wbuf.getShort();                     //get*()
    for (int i = half; i < len; i++) { dstArray[i] = wbuf.getShort(i * SB); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkNativeBaseBound() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableBuffer wbuf = wmem.asWritableBuffer();
      wbuf.toString("Force Assertion Error", memCapacity, 8, false);
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void checkNativeSrcArrayBound() throws Exception {
    long memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableBuffer wbuf = wmem.asWritableBuffer();
      byte[] srcArray = { 1, -2, 3, -4 };
      wbuf.putByteArray(srcArray, 0, 5); //wrong!
    } catch (IndexOutOfBoundsException e) {
      //pass
    }
  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void checkRegionBounds() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableBuffer wbuf = wmem.asWritableBuffer();
      wbuf.writableRegion(1, 64, wbuf.getTypeByteOrder()); //wrong!
    }
  }

  @Test
  public void checkByteBufferWrap() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocate(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());

    for (int i=0; i<memCapacity; i++) {
      byteBuf.put(i, (byte) i);
    }

    WritableBuffer wbuf = WritableBuffer.writableWrap(byteBuf);

    for (int i=0; i<memCapacity; i++) {
      assertEquals(wbuf.getByte(), byteBuf.get(i));
    }

    assertTrue(wbuf.hasByteBuffer());
    ByteBuffer byteBuf2 = wbuf.toByteBuffer(ByteOrder.nativeOrder());
    assertEquals(byteBuf2, byteBuf);
    //println( mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test
  public void checkWrapWithBBReadonly1() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocate(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());

    for (int i = 0; i < memCapacity; i++) {
      byteBuf.put(i, (byte) i);
    }

    Buffer buf = WritableBuffer.writableWrap(byteBuf);

    for (int i = 0; i < memCapacity; i++) {
      assertEquals(buf.getByte(), byteBuf.get(i));
    }

    //println(mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkWrapWithBBReadonly2() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocate(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();
    byteBufRO.order(ByteOrder.nativeOrder());
    assertTrue(true);
    WritableBuffer wbuf = WritableBuffer.writableWrap(byteBufRO);
    assertTrue(wbuf.isReadOnly());
  }

  @Test
  public void checkWrapWithDirectBBReadonly() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());

    for (int i = 0; i < memCapacity; i++) {
      byteBuf.put(i, (byte) i);
    }
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();
    byteBufRO.order(ByteOrder.nativeOrder());

    Buffer buf = Buffer.wrap(byteBufRO);

    for (int i = 0; i < memCapacity; i++) {
      assertEquals(buf.getByte(), byteBuf.get(i));
    }

    //println(mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkWrapWithDirectBBReadonlyPut() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(memCapacity);
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();
    byteBufRO.order(ByteOrder.nativeOrder());

    WritableBuffer.writableWrap(byteBufRO);
  }

  @Test
  public void checkByteBufferWrapDirectAccess() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());

    for (int i=0; i<memCapacity; i++) {
      byteBuf.put(i, (byte) i);
    }

    Buffer buf = Buffer.wrap(byteBuf);

    for (int i=0; i<memCapacity; i++) {
      assertEquals(buf.getByte(), byteBuf.get(i));
    }

    //println( mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test
  public void checkIsDirect() throws Exception {
    int memCapacity = 64;
    WritableBuffer mem = WritableMemory.allocate(memCapacity).asWritableBuffer();
    assertFalse(mem.isDirect());
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableBuffer wbuf = wmem.asWritableBuffer();
      assertTrue(wbuf.isDirect());
    }
  }

  @Test
  public void checkIsReadOnly() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    assertFalse(wbuf.isReadOnly());

    Buffer buf = wbuf;
    assertFalse(buf.isReadOnly());

    for (int i = 0; i < srcArray.length; i++) {
      assertEquals(buf.getLong(), srcArray[i]);
    }
  }

  @Test
  public void checkGoodBounds() {
   ResourceImpl.checkBounds(50, 50, 100);
  }

  @Test
  public void checkAsBuffer() {
    WritableMemory wmem = WritableMemory.allocate(64);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    wbuf.setPosition(32);
    for (int i = 32; i < 64; i++) { wbuf.putByte((byte)i); }
    //println(wbuf.toHexString("Buf", 0, (int)wbuf.getCapacity()));

    Buffer buf = wmem.asBuffer();
    buf.setPosition(32);
    for (int i = 32; i < 64; i++) {
      assertEquals(buf.getByte(), i);
    }
  }

  @Test
  public void checkDuplicate() {
    WritableMemory wmem = WritableMemory.allocate(64);
    for (int i = 0; i < 64; i++) { wmem.putByte(i, (byte)i); }

    WritableBuffer wbuf = wmem.asWritableBuffer().writableDuplicate();

    for (int i = 0; i < 64; i++) {
      assertEquals(wbuf.getByte(), i);
    }
    Buffer buf = wmem.asBuffer().duplicate();
    for (int i = 0; i < 64; i++) {
      assertEquals(buf.getByte(), i);
    }

    WritableMemory wmem2 = wbuf.asWritableMemory();
    for (int i = 0; i < 64; i++) {
      assertEquals(wmem2.getByte(i), i);
    }
    wbuf.asWritableMemory();

  }

  @Test
  public void checkDegenerateRegionReturn() {
    Memory mem = Memory.wrap(new byte[0]);
    Buffer buf = mem.asBuffer();
    Buffer reg = buf.region();
    assertEquals(reg.getCapacity(), 0);
  }

  @Test
  public void checkAsWritableMemoryRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);

    try {
      Buffer buf = Buffer.wrap(bb.asReadOnlyBuffer());
      wbuf = (WritableBuffer) buf;
      wbuf.asWritableMemory();
      Assert.fail();
    }
    catch (IllegalArgumentException expected) { }
  }

  @Test
  public void checkWritableDuplicateRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);

    try {
      Buffer buf = Buffer.wrap(bb);
      wbuf = (WritableBuffer) buf;
      wbuf.writableDuplicate();
      Assert.fail();
    } catch (IllegalArgumentException expected) { }
  }

  @Test
  public void checkWritableRegionRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);

    try {
      Buffer buf = Buffer.wrap(bb);
      wbuf = (WritableBuffer) buf;
      wbuf.writableRegion();
      Assert.fail();
    } catch (IllegalArgumentException expected) { }
  }

  @Test
  public void checkWritableRegionWithParamsRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);

    try {
      Buffer buf = Buffer.wrap(bb);
      wbuf = (WritableBuffer) buf;
      wbuf.writableRegion(0, 1, wbuf.getTypeByteOrder());
      Assert.fail();
    } catch (IllegalArgumentException expected) { }
  }

  @Test
  public void checkZeroBuffer() {
    WritableMemory wmem = WritableMemory.allocate(8);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    WritableBuffer reg = wbuf.writableRegion(0, 0, wbuf.getTypeByteOrder());
    assertEquals(reg.getCapacity(), 0);
  }

  @Test
  public void checkDuplicateNonNative() {
    WritableMemory wmem = WritableMemory.allocate(64);
    wmem.putShort(0, (short) 1);
    Buffer buf = wmem.asWritableBuffer().duplicate(NNBO);
    assertEquals(buf.getShort(0), 256);
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

}
