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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.ReadOnlyException;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NativeWritableBufferImplTest {

  //Simple Native direct

  @Test
  public void checkNativeCapacityAndClose() throws Exception {
    int memCapacity = 64;
    WritableHandle wmh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory wmem = wmh.getWritable();
    WritableBuffer wbuf = wmem.asWritableBuffer();
    assertEquals(wbuf.getCapacity(), memCapacity);

    wmh.close(); //intentional
    assertFalse(wbuf.isValid());

    wmh.close(); //intentional, nothing to free
  }

  //Simple Heap arrays

  @Test
  public void checkBooleanArray() {
    boolean[] srcArray = { true, false, true, false, false, true, true, false };
    boolean[] dstArray = new boolean[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getBooleanArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getBooleanArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
    assertTrue(buf.hasArray());
  }

  @Test
  public void checkByteArray() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    byte[] dstArray = new byte[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getByteArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getByteArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkCharArray() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    char[] dstArray = new char[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getCharArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getCharArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkShortArray() {
    short[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    short[] dstArray = new short[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getShortArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getShortArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkIntArray() {
    int[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    int[] dstArray = new int[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getIntArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getIntArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkLongArray() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    long[] dstArray = new long[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getLongArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getLongArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkFloatArray() {
    float[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    float[] dstArray = new float[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getFloatArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getFloatArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkDoubleArray() {
    double[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    double[] dstArray = new double[8];

    Buffer buf = Memory.wrap(srcArray).asBuffer();
    buf.getDoubleArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableBuffer wbuf = WritableMemory.writableWrap(srcArray).asWritableBuffer();
    wbuf.getDoubleArray(dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkNativeBaseBound() throws Exception {
    int memCapacity = 64;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory wmem = wrh.getWritable();
      WritableBuffer wbuf = wmem.asWritableBuffer();
      wbuf.toHexString("Force Assertion Error", memCapacity, 8);
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void checkNativeSrcArrayBound() throws Exception {
    long memCapacity = 64;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory wmem = wrh.getWritable();
      WritableBuffer wbuf = wmem.asWritableBuffer();
      byte[] srcArray = { 1, -2, 3, -4 };
      wbuf.putByteArray(srcArray, 0, 5); //wrong!
    } catch (IllegalArgumentException e) {
      //pass
    }
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkRegionBounds() throws Exception {
    int memCapacity = 64;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory wmem = wrh.getWritable();
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
    ByteBuffer byteBuf2 = wbuf.getByteBuffer();
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

  @Test(expectedExceptions = ReadOnlyException.class)
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

  @Test(expectedExceptions = ReadOnlyException.class)
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
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem2 = wrh.getWritable();
      WritableBuffer wbuf = mem2.asWritableBuffer();
      assertTrue(wbuf.isDirect());
      wrh.close(); //immediate close
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
    UnsafeUtil.checkBounds(50, 50, 100);
  }

  @Test
  public void checkCompareToHeap() {
    byte[] arr1 = new byte[] {0, 1, 2, 3};
    byte[] arr2 = new byte[] {0, 1, 2, 4};
    byte[] arr3 = new byte[] {0, 1, 2, 3, 4};

    Buffer buf1 = Memory.wrap(arr1).asBuffer();
    Buffer buf2 = Memory.wrap(arr2).asBuffer();
    Buffer buf3 = Memory.wrap(arr3).asBuffer();

    int comp = buf1.compareTo(0, 3, buf2, 0, 3);
    assertEquals(comp, 0);
    comp = buf1.compareTo(0, 4, buf2, 0, 4);
    assertEquals(comp, -1);
    comp = buf2.compareTo(0, 4, buf1, 0, 4);
    assertEquals(comp, 1);
    //different lengths
    comp = buf1.compareTo(0, 4, buf3, 0, 5);
    assertEquals(comp, -1);
    comp = buf3.compareTo(0, 5, buf1, 0, 4);
    assertEquals(comp, 1);
  }

  @Test
  public void checkCompareToDirect() throws Exception {
    byte[] arr1 = new byte[] {0, 1, 2, 3};
    byte[] arr2 = new byte[] {0, 1, 2, 4};
    byte[] arr3 = new byte[] {0, 1, 2, 3, 4};

    try (WritableHandle h1 = WritableMemory.allocateDirect(4);
        WritableHandle h2 = WritableMemory.allocateDirect(4);
        WritableHandle h3 = WritableMemory.allocateDirect(5))
    {
      WritableMemory mem1 = h1.getWritable();
      mem1.putByteArray(0, arr1, 0, 4);

      WritableMemory mem2 = h2.getWritable();
      mem2.putByteArray(0, arr2, 0, 4);

      WritableMemory mem3 = h3.getWritable();
      mem3.putByteArray(0, arr3, 0, 5);

      Buffer buf1 = mem1.asBuffer();
      Buffer buf2 = mem2.asBuffer();
      Buffer buf3 = mem3.asBuffer();

      int comp = buf1.compareTo(0, 3, buf2, 0, 3);
      assertEquals(comp, 0);
      comp = buf1.compareTo(0, 4, buf2, 0, 4);
      assertEquals(comp, -1);
      comp = buf2.compareTo(0, 4, buf1, 0, 4);
      assertEquals(comp, 1);
      //different lengths
      comp = buf1.compareTo(0, 4, buf3, 0, 5);
      assertEquals(comp, -1);
      comp = buf3.compareTo(0, 5, buf1, 0, 4);
      assertEquals(comp, 1);
    }
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
    wbuf.checkValidAndBounds(0, 64);
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
    WritableMemory wmem3 = wbuf.asWritableMemory();
    wmem3.checkValidAndBounds(0, 64);
  }

  @Test
  public void checkCumAndRegionOffset() {
    WritableMemory wmem = WritableMemory.allocate(64);
    WritableMemory reg = wmem.writableRegion(32, 32);
    WritableBuffer buf = reg.asWritableBuffer();
    assertEquals(buf.getRegionOffset(), 32);
    assertEquals(buf.getRegionOffset(0), 32);
    assertEquals(buf.getCumulativeOffset(0), 32 + 16);
  }

  @Test
  public void checkIsSameResource() {
    byte[] byteArr = new byte[64];
    WritableBuffer wbuf1 = WritableMemory.writableWrap(byteArr).asWritableBuffer();
    WritableBuffer wbuf2 = WritableMemory.writableWrap(byteArr).asWritableBuffer();
    assertTrue(wbuf1.isSameResource(wbuf2));
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
    WritableMemory wmem = wbuf.asWritableMemory(); //OK
    assertNotNull(wmem);

    try {
      Buffer buf = Buffer.wrap(bb.asReadOnlyBuffer());
      wbuf = (WritableBuffer) buf;
      wmem = wbuf.asWritableMemory();
      Assert.fail();
    } catch (ReadOnlyException expected) {
      // expected
    }
  }

  @Test
  public void checkWritableDuplicateRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);
    @SuppressWarnings("unused")
    WritableBuffer wdup = wbuf.writableDuplicate();

    try {
      Buffer buf = Buffer.wrap(bb);
      wbuf = (WritableBuffer) buf;
      @SuppressWarnings("unused")
      WritableBuffer wdup2 = wbuf.writableDuplicate();
      Assert.fail();
    } catch (ReadOnlyException expected) {
      // ignore
    }
  }

  @Test
  public void checkWritableRegionRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);
    @SuppressWarnings("unused")
    WritableBuffer wreg = wbuf.writableRegion();

    try {
      Buffer buf = Buffer.wrap(bb);
      wbuf = (WritableBuffer) buf;
      @SuppressWarnings("unused")
      WritableBuffer wreg2 = wbuf.writableRegion();
      Assert.fail();
    } catch (ReadOnlyException expected) {
      // ignore
    }
  }

  @Test
  public void checkWritableRegionWithParamsRO() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.writableWrap(bb);
    @SuppressWarnings("unused")
    WritableBuffer wreg = wbuf.writableRegion(0, 1, wbuf.getTypeByteOrder());

    try {
      Buffer buf = Buffer.wrap(bb);
      wbuf = (WritableBuffer) buf;
      @SuppressWarnings("unused")
      WritableBuffer wreg2 = wbuf.writableRegion(0, 1, wbuf.getTypeByteOrder());
      Assert.fail();
    } catch (ReadOnlyException expected) {
      // ignore
    }
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
    Buffer buf = wmem.asWritableBuffer().duplicate(Util.NON_NATIVE_BYTE_ORDER);
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
