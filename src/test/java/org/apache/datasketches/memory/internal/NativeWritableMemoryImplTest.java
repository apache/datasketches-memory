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

import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.foreign.Arena;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class NativeWritableMemoryImplTest {
  private static final MemoryRequestServer memReqSvr = Resource.defaultMemReqSvr;

  //Simple Native direct

  @Test
  public void checkNativeCapacityAndClose() throws Exception {
    int memCapacity = 64;
    WritableMemory wmem = WritableMemory.allocateDirect(
      memCapacity,
      1,
      ByteOrder.nativeOrder(),
      memReqSvr,
      Arena.ofConfined());
    assertEquals(memCapacity, wmem.getCapacity());

    wmem.getArena().close();
    assertFalse(wmem.isAlive());
    try { wmem.getArena().close(); } catch (IllegalStateException e) { }
  }

  //Simple Heap arrays

  @Test
  public void checkGetByteArray() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    byte[] dstArray = new byte[len];

    Memory mem = Memory.wrap(srcArray);
    mem.getByteArray(0, dstArray, 0, half);
    mem.getByteArray(half, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getByteArray(0, dstArray, 0, half);
    wmem.getByteArray(half, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutByteArray() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    byte[] dstArray = new byte[len];

    WritableMemory wmem = WritableMemory.allocate(len * Byte.BYTES);
    wmem.putByteArray(0, srcArray, 0, half);
    wmem.putByteArray(half * Byte.BYTES, srcArray, half, half);
    wmem.getByteArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetCharArray() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    char[] dstArray = new char[len];

    Memory mem = Memory.wrap(srcArray);
    mem.getCharArray(0, dstArray, 0, half);
    mem.getCharArray(half * Character.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getCharArray(0, dstArray, 0, half);
    wmem.getCharArray(half * Character.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutCharArray() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    char[] dstArray = new char[len];

    WritableMemory wmem = WritableMemory.allocate(len * Character.BYTES);
    wmem.putCharArray(0, srcArray, 0, half);
    wmem.putCharArray(half * Character.BYTES, srcArray, half, half);
    wmem.getCharArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetShortArray() {
    short[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    short[] dstArray = new short[len];

    Memory mem = Memory.wrap(srcArray);
    mem.getShortArray(0, dstArray, 0, half);
    mem.getShortArray(half * Short.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getShortArray(0, dstArray, 0, half);
    wmem.getShortArray(half * Short.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutShortArray() {
    short[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    short[] dstArray = new short[len];

    WritableMemory wmem = WritableMemory.allocate(len * Short.BYTES);
    wmem.putShortArray(0, srcArray, 0, half);
    wmem.putShortArray(half * Short.BYTES, srcArray, half, half);
    wmem.getShortArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetIntArray() {
    int[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    int[] dstArray = new int[len];

    Memory mem = Memory.wrap(srcArray);
    mem.getIntArray(0, dstArray, 0, half);
    mem.getIntArray(half * Integer.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getIntArray(0, dstArray, 0, half);
    wmem.getIntArray(half * Integer.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutIntArray() {
    int[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    int[] dstArray = new int[len];

    WritableMemory wmem = WritableMemory.allocate(len * Integer.BYTES);
    wmem.putIntArray(0, srcArray, 0, half);
    wmem.putIntArray(half * Integer.BYTES, srcArray, half, half);
    wmem.getIntArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetLongArray() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    long[] dstArray = new long[len];

    Memory mem = Memory.wrap(srcArray);
    mem.getLongArray(0, dstArray, 0, half);
    mem.getLongArray(half * Long.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getLongArray(0, dstArray, 0, half);
    wmem.getLongArray(half * Long.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutLongArray() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    long[] dstArray = new long[len];

    WritableMemory wmem = WritableMemory.allocate(len * Long.BYTES);
    wmem.putLongArray(0, srcArray, 0, half);
    wmem.putLongArray(half * Long.BYTES, srcArray, half, half);
    wmem.getLongArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }


  @Test
  public void checkGetFloatArray() {
    float[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    float[] dstArray = new float[len];

    Memory mem = Memory.wrap(srcArray);
    mem.getFloatArray(0, dstArray, 0, half);
    mem.getFloatArray(half * Float.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getFloatArray(0, dstArray, 0, half);
    wmem.getFloatArray(half * Float.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutFloatArray() {
    float[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    float[] dstArray = new float[len];

    WritableMemory wmem = WritableMemory.allocate(len * Float.BYTES);
    wmem.putFloatArray(0, srcArray, 0, half);
    wmem.putFloatArray(half * Float.BYTES, srcArray, half, half);
    wmem.getFloatArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkGetDoubleArray() {
    double[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    double[] dstArray = new double[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getDoubleArray(0, dstArray, 0, half);
    mem.getDoubleArray(half * Double.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    wmem.getDoubleArray(0, dstArray, 0, half);
    wmem.getDoubleArray(half * Double.BYTES, dstArray, half, half);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkPutDoubleArray() {
    double[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    double[] dstArray = new double[len];

    WritableMemory wmem = WritableMemory.allocate(len * Double.BYTES);
    wmem.putDoubleArray(0, srcArray, 0, half);
    wmem.putDoubleArray(half * Double.BYTES, srcArray, half, half);
    wmem.getDoubleArray(0, dstArray, 0, len);
    assertEquals(dstArray, srcArray);
  }

  @Test
  public void checkNativeBaseBound() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      wmem.toString("Force Assertion Error", memCapacity, 8, false);
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void checkNativeSrcArrayBound() throws Exception {
    long memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      byte[] srcArray = { 1, -2, 3, -4 };
      wmem.putByteArray(0L, srcArray, 0, 5);
    } catch (IndexOutOfBoundsException e) {
      //pass
    }
  }

  //Copy Within tests

  @Test
  public void checkDegenerateCopyTo() {
    WritableMemory wmem = WritableMemory.allocate(64);
    wmem.copyTo(0, wmem, 0, 64);
  }

  @Test
  public void checkCopyWithinNativeSmall() throws Exception {
    int memCapacity = 64;
    int half = memCapacity/2;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      wmem.clear();

      for (int i=0; i<half; i++) { //fill first half
        wmem.putByte(i, (byte) i);
      }

      wmem.copyTo(0, wmem, half, half);

      for (int i=0; i<half; i++) {
        assertEquals(wmem.getByte(i+half), (byte) i);
      }
    }
  }

  @Test
  public void checkCopyWithinNativeLarge() throws Exception {
    int memCapacity = (2 << 20) + 64;
    int memCapLongs = memCapacity / 8;
    int halfBytes = memCapacity / 2;
    int halfLongs = memCapLongs / 2;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      wmem.clear();

      for (int i=0; i < halfLongs; i++) {
        wmem.putLong(i*8, i);
      }

      wmem.copyTo(0, wmem, halfBytes, halfBytes);

      for (int i=0; i < halfLongs; i++) {
        assertEquals(wmem.getLong((i + halfLongs)*8), i);
      }
    }
  }

  @Test
  public void checkCopyWithinNativeSrcBound() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      wmem.copyTo(32, wmem, 32, 33);  //hit source bound check
      fail("Did Not Catch Assertion Error: source bound");
    }
    catch (IndexOutOfBoundsException e) {
      //pass
    }
  }

  @Test
  public void checkCopyWithinNativeDstBound() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      wmem.copyTo(0, wmem, 32, 33);  //hit dst bound check
      fail("Did Not Catch Assertion Error: dst bound");
    }
    catch (IndexOutOfBoundsException e) {
      //pass
    }
  }

  @Test
  public void checkCopyCrossNativeSmall() throws Exception {
    int memCapacity = 64;

    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem1 = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableMemory wmem2 = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);

      for (int i=0; i < memCapacity; i++) {
        wmem1.putByte(i, (byte) i);
      }
      wmem2.clear();
      wmem1.copyTo(0, wmem2, 0, memCapacity);

      for (int i=0; i<memCapacity; i++) {
        assertEquals(wmem2.getByte(i), (byte) i);
      }
    }
  }

  @Test
  public void checkCopyCrossNativeLarge() throws Exception {
    int memCapacity = (2<<20) + 64;
    int memCapLongs = memCapacity / 8;

    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem1 = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableMemory wmem2 = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);

      for (int i=0; i < memCapLongs; i++) {
        wmem1.putLong(i*8, i);
      }
      wmem2.clear();

      wmem1.copyTo(0, wmem2, 0, memCapacity);

      for (int i=0; i<memCapLongs; i++) {
        assertEquals(wmem2.getLong(i*8), i);
      }
    }
  }

  @Test
  public void checkCopyCrossNativeAndByteArray() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);

      for (int i= 0; i < wmem.getCapacity(); i++) {
        wmem.putByte(i, (byte) i);
      }

      WritableMemory wmem2 = WritableMemory.allocate(memCapacity);
      wmem.copyTo(8, wmem2, 16, 16);

      for (int i=0; i<16; i++) {
        assertEquals(wmem.getByte(8+i), wmem2.getByte(16+i));
      }
      //println(mem2.toHexString("Mem2", 0, (int)mem2.getCapacity()));
    }
  }

  @Test
  public void checkCopyCrossRegionsSameNative() throws Exception {
    int memCapacity = 128;

    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem1 = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);

      for (int i= 0; i < wmem1.getCapacity(); i++) {
        wmem1.putByte(i, (byte) i);
      }
      //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));

      Memory reg1 = wmem1.region(8, 16);
      //println(reg1.toHexString("Reg1", 0, (int)reg1.getCapacity()));

      WritableMemory reg2 = wmem1.writableRegion(24, 16);
      //println(reg2.toHexString("Reg2", 0, (int)reg2.getCapacity()));
      reg1.copyTo(0, reg2, 0, 16);

      for (int i=0; i<16; i++) {
        assertEquals(reg1.getByte(i), reg2.getByte(i));
        assertEquals(wmem1.getByte(8+i), wmem1.getByte(24+i));
      }
      //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));
    }
  }

  @Test
  public void checkCopyCrossNativeArrayAndHierarchicalRegions() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem1 = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);

      for (int i= 0; i < wmem1.getCapacity(); i++) { //fill with numbers
        wmem1.putByte(i, (byte) i);
      }
      //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));

      WritableMemory wmem2 = WritableMemory.allocate(memCapacity);

      Memory reg1 = wmem1.region(8, 32);
      Memory reg1B = reg1.region(8, 16);
      //println(reg1.toHexString("Reg1", 0, (int)reg1.getCapacity()));
      //println(reg1B.toHexString("Reg1B", 0, (int)reg1B.getCapacity()));

      WritableMemory reg2 = wmem2.writableRegion(32, 16);
      reg1B.copyTo(0, reg2, 0, 16);
      //println(reg2.toHexString("Reg2", 0, (int)reg2.getCapacity()));

      //println(mem2.toHexString("Mem2", 0, (int)mem2.getCapacity()));
      for (int i = 32, j = 16; i < 40; i++, j++) {
        assertEquals(wmem2.getByte(i), j);
      }
    }

  }

  @Test(expectedExceptions = IndexOutOfBoundsException.class)
  public void checkRegionBounds() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      wmem.writableRegion(1, 64);
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

    WritableMemory wmem = WritableMemory.writableWrap(byteBuf);

    for (int i=0; i<memCapacity; i++) {
      assertEquals(wmem.getByte(i), byteBuf.get(i));
    }

    assertTrue(wmem.hasByteBuffer());
    ByteBuffer byteBuf2 = wmem.toByteBuffer(ByteOrder.nativeOrder());
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

    Memory mem = WritableMemory.writableWrap(byteBuf);

    for (int i = 0; i < memCapacity; i++) {
      assertEquals(mem.getByte(i), byteBuf.get(i));
    }

    //println(mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkWrapWithBBReadonly2() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocate(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();

    WritableMemory.writableWrap(byteBufRO);
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

    Memory mem = Memory.wrap(byteBufRO);

    for (int i = 0; i < memCapacity; i++) {
      assertEquals(mem.getByte(i), byteBuf.get(i));
    }

    //println(mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkWrapWithDirectBBReadonlyPut() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(memCapacity);
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();
    byteBufRO.order(ByteOrder.nativeOrder());

    WritableMemory.writableWrap(byteBufRO);
  }

  @Test
  public void checkByteBufferWrapDirectAccess() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());

    for (int i=0; i<memCapacity; i++) {
      byteBuf.put(i, (byte) i);
    }

    Memory mem = Memory.wrap(byteBuf);

    for (int i=0; i<memCapacity; i++) {
      assertEquals(mem.getByte(i), byteBuf.get(i));
    }

    //println( mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test
  public void checkIsDirect() throws Exception {
    int memCapacity = 64;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory mem = WritableMemory.allocate(memCapacity);
      assertFalse(mem.isDirect());
      WritableMemory wmem = WritableMemory.allocateDirect(memCapacity, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      assertTrue(wmem.isDirect());
    }
  }

  @Test
  public void checkIsReadOnly() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };

    WritableMemory wmem = WritableMemory.writableWrap(srcArray);
    assertFalse(wmem.isReadOnly());

    Memory memRO = wmem;
    assertFalse(memRO.isReadOnly());

    for (int i = 0; i < wmem.getCapacity(); i++) {
      assertEquals(wmem.getByte(i), memRO.getByte(i));
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
  public void checkAsWritableBufferWithBB() {
    ByteBuffer byteBuf = ByteBuffer.allocate(64);
    byteBuf.position(16);
    byteBuf.limit(48);
    WritableMemory wmem = WritableMemory.writableWrap(byteBuf);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    assertEquals(wbuf.getCapacity(), 64);
    assertEquals(wbuf.getPosition(), 0);
    assertEquals(wbuf.getEnd(), 64);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkAsWritableRegionRO() {
    ByteBuffer byteBuf = ByteBuffer.allocate(64);
    WritableMemory wmem = (WritableMemory) Memory.wrap(byteBuf);
    wmem.writableRegion(0, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkAsWritableBufferRO() {
    ByteBuffer byteBuf = ByteBuffer.allocate(64);
    WritableMemory wmem = (WritableMemory) Memory.wrap(byteBuf);
    wmem.asWritableBuffer();
  }

  @Test void checkZeroMemory() {
    WritableMemory wmem = WritableMemory.allocate(8);
    WritableMemory reg = wmem.writableRegion(0, 0);
    assertEquals(reg.getCapacity(), 0);
  }

  @Test
  public void checkAsBufferNonNative() {
    WritableMemory wmem = WritableMemory.allocate(64);
    wmem.putShort(0, (short) 1);
    Buffer buf = wmem.asBuffer(NON_NATIVE_BYTE_ORDER);
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
