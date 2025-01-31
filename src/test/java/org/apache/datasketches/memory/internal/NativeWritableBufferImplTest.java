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
import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;
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

  //Simple Heap arrays

  @Test
  public void checkGetByteArray() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    byte[] dstArray = new byte[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getByteArray(dstArray, 0, half);
    buf.getByteArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getByteArray(dstArray, 0, half);
    wbuf.getByteArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutByteArray() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    byte[] dstArray = new byte[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Byte.BYTES).asWritableBuffer();
    wbuf.putByteArray(srcArray, 0, half);
    wbuf.putByteArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getByteArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetCharArray() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    char[] dstArray = new char[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getCharArray(dstArray, 0, half);
    buf.getCharArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getCharArray(dstArray, 0, half);
    wbuf.getCharArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutCharArray() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    char[] dstArray = new char[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Character.BYTES).asWritableBuffer();
    wbuf.putCharArray(srcArray, 0, half);
    wbuf.putCharArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getCharArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetShortArray() {
    short[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    short[] dstArray = new short[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getShortArray(dstArray, 0, half);
    buf.getShortArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getShortArray(dstArray, 0, half);
    wbuf.getShortArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutShortArray() {
    short[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    short[] dstArray = new short[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Short.BYTES).asWritableBuffer();
    wbuf.putShortArray(srcArray, 0, half);
    wbuf.putShortArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getShortArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetIntArray() {
    int[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    int[] dstArray = new int[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getIntArray(dstArray, 0, half);
    buf.getIntArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getIntArray(dstArray, 0, half);
    wbuf.getIntArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutIntArray() {
    int[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    int[] dstArray = new int[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Integer.BYTES).asWritableBuffer();
    wbuf.putIntArray(srcArray, 0, half);
    wbuf.putIntArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getIntArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetLongArray() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    long[] dstArray = new long[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getLongArray(dstArray, 0, half);
    buf.getLongArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getLongArray(dstArray, 0, half);
    wbuf.getLongArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutLongArray() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    long[] dstArray = new long[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Long.BYTES).asWritableBuffer();
    wbuf.putLongArray(srcArray, 0, half);
    wbuf.putLongArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getLongArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetFloatArray() {
    float[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    float[] dstArray = new float[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getFloatArray(dstArray, 0, half);
    buf.getFloatArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);


    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getFloatArray(dstArray, 0, half);
    wbuf.getFloatArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutFloatArray() {
    float[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    float[] dstArray = new float[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Float.BYTES).asWritableBuffer();
    wbuf.putFloatArray(srcArray, 0, half);
    wbuf.putFloatArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getFloatArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetDoubleArray() {
    double[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    double[] dstArray = new double[len];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getDoubleArray(dstArray, 0, half);
    buf.getDoubleArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);


    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getDoubleArray(dstArray, 0, half);
    wbuf.getDoubleArray(dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutDoubleArray() {
    double[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    double[] dstArray = new double[len];

    WritableBuffer wbuf = WritableMemory.allocate(len * Double.BYTES).asWritableBuffer();
    wbuf.putDoubleArray(srcArray, 0, half);
    wbuf.putDoubleArray(srcArray, half, half);
    wbuf.resetPosition();
    wbuf.getDoubleArray(dstArray, 0, len);
    assertEquals(dstArray, srcArray);
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
    Buffer buf = wmem.asWritableBuffer().duplicate(NON_NATIVE_BYTE_ORDER);
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
