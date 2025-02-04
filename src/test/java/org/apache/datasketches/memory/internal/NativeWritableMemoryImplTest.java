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

  //Check primitives

  @Test
  public void checkPutGetBooleans() {
    boolean[] srcArray = {true, false, true, false, false, true, false, true};
    final int len = srcArray.length;
    WritableMemory wmem = WritableMemory.allocate(len, NATIVE_BYTE_ORDER);
    //put
    for (int i = 0; i < len; i++) { wmem.putBoolean(i, srcArray[i]); }  //put*(add, value)
    //get
    boolean[] dstArray = new boolean[len];
    for (int i = 0; i < len; i++) { dstArray[i] = wmem.getBoolean(i); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetBytes() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len, NATIVE_BYTE_ORDER);
    //put
    wmem.putByte(0, srcArray[0]);                     //put*(add, value)
    wmem.putByteArray(1, srcArray, 1, 2);             //put*Array(add, src[], srcOff, len)
    wmem.putByte(3, srcArray[3]);                     //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putByte(i, srcArray[i]); } //put*(add, value)
    //get
    byte[] dstArray = new byte[len];
    dstArray[0] = wmem.getByte(0);                    //get*(add)
    wmem.getByteArray(1, dstArray, 1, 2);             //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getByte(3);                    //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getByte(i); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeCharacters() {
    char[] srcArray = { 'a','b','c','d','e','f','g','h' };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Character.BYTES, NATIVE_BYTE_ORDER);
    //put
    wmem.putChar(0, srcArray[0]);                     //put*(add, value)
    wmem.putCharArray(1 * Character.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putChar(3 * Character.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putChar(i * Character.BYTES, srcArray[i]); } //put*(add, value)
    //get
    char[] dstArray = new char[len];
    dstArray[0] = wmem.getChar(0);                    //get*(add)
    wmem.getCharArray(1 * Character.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getChar(3 * Character.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getChar(i * Character.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeDoubles() {
    double[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Double.BYTES, NATIVE_BYTE_ORDER);
    //put
    wmem.putDouble(0, srcArray[0]);                     //put*(add, value)
    wmem.putDoubleArray(1 * Double.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putDouble(3 * Double.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putDouble(i * Double.BYTES, srcArray[i]); } //put*(add, value)
    //get
    double[] dstArray = new double[len];
    dstArray[0] = wmem.getDouble(0);                    //get*(add)
    wmem.getDoubleArray(1 * Double.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getDouble(3 * Double.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getDouble(i * Double.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeFloats() {
    float[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Float.BYTES, NATIVE_BYTE_ORDER);
    //put
    wmem.putFloat(0, srcArray[0]);                     //put*(add, value)
    wmem.putFloatArray(1 * Float.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putFloat(3 * Float.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putFloat(i * Float.BYTES, srcArray[i]); } //put*(add, value)
    //get
    float[] dstArray = new float[len];
    dstArray[0] = wmem.getFloat(0);                    //get*(add)
    wmem.getFloatArray(1 * Float.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getFloat(3 * Float.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getFloat(i * Float.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeInts() {
    int[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Integer.BYTES, NATIVE_BYTE_ORDER);
    //put
    wmem.putInt(0, srcArray[0]);                     //put*(add, value)
    wmem.putIntArray(1 * Integer.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putInt(3 * Integer.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putInt(i * Integer.BYTES, srcArray[i]); } //put*(add, value)
    //get
    int[] dstArray = new int[len];
    dstArray[0] = wmem.getInt(0);                    //get*(add)
    wmem.getIntArray(1 * Integer.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getInt(3 * Integer.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getInt(i * Integer.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeLongs() {
    long[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Long.BYTES, NATIVE_BYTE_ORDER);
    //put
    wmem.putLong(0, srcArray[0]);                     //put*(add, value)
    wmem.putLongArray(1 * Long.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putLong(3 * Long.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putLong(i * Long.BYTES, srcArray[i]); } //put*(add, value)
    //get
    long[] dstArray = new long[len];
    dstArray[0] = wmem.getLong(0);                    //get*(add)
    wmem.getLongArray(1 * Long.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getLong(3 * Long.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getLong(i * Long.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
  }

  @Test
  public void checkPutGetNativeShorts() {
    short[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    final int len = srcArray.length;
    final int half = len / 2;
    WritableMemory wmem = WritableMemory.allocate(len * Short.BYTES, NATIVE_BYTE_ORDER);
    //put
    wmem.putShort(0, srcArray[0]);                     //put*(add, value)
    wmem.putShortArray(1 * Short.BYTES, srcArray, 1, 2);        //put*Array(add, src[], srcOff, len)
    wmem.putShort(3 * Short.BYTES, srcArray[3]);                //put*(add, value)
    for (int i = half; i < len; i++) { wmem.putShort(i * Short.BYTES, srcArray[i]); } //put*(add, value)
    //get
    short[] dstArray = new short[len];
    dstArray[0] = wmem.getShort(0);                    //get*(add)
    wmem.getShortArray(1 * Short.BYTES, dstArray, 1, 2);        //get*Array(add, dst[], dstOff, len)
    dstArray[3] = wmem.getShort(3 * Short.BYTES);               //get*(add)
    for (int i = half; i < len; i++) { dstArray[i] = wmem.getShort(i * Short.BYTES); } //get*(add)
    assertEquals(srcArray, dstArray);
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
