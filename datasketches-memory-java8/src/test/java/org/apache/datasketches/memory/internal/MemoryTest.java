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

/*
 * Note: Lincoln's Gettysburg Address is in the public domain. See LICENSE.
 */

package org.apache.datasketches.memory.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.datasketches.memory.DefaultMemoryRequestServer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryInvalidException;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class MemoryTest {
  private static final String LS = Util.LS;

  @BeforeClass
  public void setReadOnly() {
    UtilTest.setGettysburgAddressFileToReadOnly();
  }

  @Test
  public void checkDirectRoundTrip() throws Exception {
    int n = 1024; //longs
    try (WritableMemory mem = WritableMemory.allocateDirect(n * 8)) {
      for (int i = 0; i < n; i++) {
        mem.putLong(i * 8, i);
      }
      for (int i = 0; i < n; i++) {
        long v = mem.getLong(i * 8);
        assertEquals(v, i);
      }
    }
  }

  @Test
  public void checkAutoHeapRoundTrip() {
    int n = 1024; //longs
    WritableMemory wmem = WritableMemory.allocate(n * 8);
    for (int i = 0; i < n; i++) {
      wmem.putLong(i * 8, i);
    }
    for (int i = 0; i < n; i++) {
      long v = wmem.getLong(i * 8);
      assertEquals(v, i);
    }
  }

  @Test
  public void checkArrayWrap() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    WritableMemory wmem = WritableMemory.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      wmem.putLong(i * 8, i);
    }
    for (int i = 0; i < n; i++) {
      long v = wmem.getLong(i * 8);
      assertEquals(v, i);
    }
    Memory mem = Memory.wrap(arr, ByteOrder.nativeOrder());
    for (int i = 0; i < n; i++) {
      long v = mem.getLong(i * 8);
      assertEquals(v, i);
    }
    // check 0 length array wraps
    Memory memZeroLengthArrayBoolean = WritableMemory.writableWrap(new boolean[0]);
    Memory memZeroLengthArrayByte = WritableMemory.writableWrap(new byte[0]);
    Memory memZeroLengthArrayChar = WritableMemory.writableWrap(new char[0]);
    Memory memZeroLengthArrayShort = WritableMemory.writableWrap(new short[0]);
    Memory memZeroLengthArrayInt = WritableMemory.writableWrap(new int[0]);
    Memory memZeroLengthArrayLong = WritableMemory.writableWrap(new long[0]);
    Memory memZeroLengthArrayFloat = WritableMemory.writableWrap(new float[0]);
    Memory memZeroLengthArrayDouble = WritableMemory.writableWrap(new double[0]);
    assertEquals(memZeroLengthArrayBoolean.getCapacity(), 0);
    assertEquals(memZeroLengthArrayByte.getCapacity(), 0);
    assertEquals(memZeroLengthArrayChar.getCapacity(), 0);
    assertEquals(memZeroLengthArrayShort.getCapacity(), 0);
    assertEquals(memZeroLengthArrayInt.getCapacity(), 0);
    assertEquals(memZeroLengthArrayLong.getCapacity(), 0);
    assertEquals(memZeroLengthArrayFloat.getCapacity(), 0);
    assertEquals(memZeroLengthArrayDouble.getCapacity(), 0);

    // check 0 length array wraps
    List<Memory> memoryToCheck = Lists.newArrayList();
    memoryToCheck.add(WritableMemory.allocate(0));
    memoryToCheck.add(WritableMemory.writableWrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(WritableMemory.writableWrap(new boolean[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new byte[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new char[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new short[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new int[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new long[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new float[0]));
    memoryToCheck.add(WritableMemory.writableWrap(new double[0]));
    memoryToCheck.add(Memory.wrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(Memory.wrap(new boolean[0]));
    memoryToCheck.add(Memory.wrap(new byte[0]));
    memoryToCheck.add(Memory.wrap(new char[0]));
    memoryToCheck.add(Memory.wrap(new short[0]));
    memoryToCheck.add(Memory.wrap(new int[0]));
    memoryToCheck.add(Memory.wrap(new long[0]));
    memoryToCheck.add(Memory.wrap(new float[0]));
    memoryToCheck.add(Memory.wrap(new double[0]));
    //Check the Memory lengths
    for (Memory memory : memoryToCheck) {
      assertEquals(memory.getCapacity(), 0);
    }
  }

  @Test
  public void checkByteBufHeap() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    ByteBuffer bb = ByteBuffer.wrap(arr);
    bb.order(ByteOrder.nativeOrder());
    WritableMemory wmem = WritableMemory.writableWrap(bb);
    for (int i = 0; i < n; i++) { //write to wmem
      wmem.putLong(i * 8, i);
    }
    for (int i = 0; i < n; i++) { //read from wmem
      long v = wmem.getLong(i * 8);
      assertEquals(v, i);
    }
    for (int i = 0; i < n; i++) { //read from BB
      long v = bb.getLong(i * 8);
      assertEquals(v, i);
    }
    Memory mem1 = Memory.wrap(arr);
    for (int i = 0; i < n; i++) { //read from wrapped arr
      long v = mem1.getLong(i * 8);
      assertEquals(v, i);
    }
    //convert to RO
    Memory mem = wmem;
    for (int i = 0; i < n; i++) {
      long v = mem.getLong(i * 8);
      assertEquals(v, i);
    }
  }

  @Test
  public void checkByteBufDirect() {
    int n = 1024; //longs
    ByteBuffer bb = ByteBuffer.allocateDirect(n * 8);
    bb.order(ByteOrder.nativeOrder());
    WritableMemory wmem = WritableMemory.writableWrap(bb);
    for (int i = 0; i < n; i++) { //write to wmem
      wmem.putLong(i * 8, i);
    }
    for (int i = 0; i < n; i++) { //read from wmem
      long v = wmem.getLong(i * 8);
      assertEquals(v, i);
    }
    for (int i = 0; i < n; i++) { //read from BB
      long v = bb.getLong(i * 8);
      assertEquals(v, i);
    }
    Memory mem1 = Memory.wrap(bb);
    for (int i = 0; i < n; i++) { //read from wrapped bb RO
      long v = mem1.getLong(i * 8);
      assertEquals(v, i);
    }
    //convert to RO
    Memory mem = wmem;
    for (int i = 0; i < n; i++) {
      long v = mem.getLong(i * 8);
      assertEquals(v, i);
    }
  }

  @Test
  public void checkByteBufWrongOrder() {
    int n = 1024; //longs
    ByteBuffer bb = ByteBuffer.allocate(n * 8);
    bb.order(ByteOrder.BIG_ENDIAN);
    Memory mem = Memory.wrap(bb);
    assertFalse(mem.getByteOrder() == ByteOrder.nativeOrder());
    assertEquals(mem.getByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkReadOnlyHeapByteBuffer() {
    ByteBuffer bb = ByteBuffer.allocate(128);
    bb.order(ByteOrder.nativeOrder());
    for (int i = 0; i < 128; i++) { bb.put(i, (byte)i); }
    bb.position(64);
    ByteBuffer slice = bb.slice().asReadOnlyBuffer();
    slice.order(ByteOrder.nativeOrder());
    Memory mem = Memory.wrap(slice);
    for (int i = 0; i < 64; i++) {
      assertEquals(mem.getByte(i), 64 + i);
    }
    mem.toHexString("slice", 0, slice.capacity());
    //println(s);
  }

  @Test
  public void checkPutGetArraysHeap() {
    int n = 1024; //longs
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    WritableMemory wmem = WritableMemory.allocate(n * 8);
    wmem.putLongArray(0, arr, 0, n);
    long[] arr2 = new long[n];
    wmem.getLongArray(0, arr2, 0, n);
    for (int i = 0; i < n; i++) {
      assertEquals(arr2[i], i);
    }
  }

  @Test
  public void checkRORegions() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    Memory mem = Memory.wrap(arr);
    Memory reg = mem.region(n2 * 8, n2 * 8); //top half
    for (int i = 0; i < n2; i++) {
      long v = reg.getLong(i * 8);
      long e = i + n2;
      assertEquals(v, e);
    }
  }

  @Test
  public void checkRORegionsReverseBO() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    Memory mem = Memory.wrap(arr);
    Memory reg = mem.region(n2 * 8, n2 * 8, Util.NON_NATIVE_BYTE_ORDER); //top half
    for (int i = 0; i < n2; i++) {
      long v = Long.reverseBytes(reg.getLong(i * 8));
      long e = i + n2;
      assertEquals(v, e);
    }
  }

  @Test
  public void checkWRegions() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    WritableMemory wmem = WritableMemory.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      assertEquals(wmem.getLong(i * 8), i);
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    WritableMemory reg = wmem.writableRegion(n2 * 8, n2 * 8);
    for (int i = 0; i < n2; i++) { reg.putLong(i * 8, i); }
    for (int i = 0; i < n; i++) {
      assertEquals(wmem.getLong(i * 8), i % 8);
      //println("" + wmem.getLong(i * 8));
    }
  }

  @Test
  public void checkWRegionsReverseBO() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    WritableMemory wmem = WritableMemory.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      assertEquals(wmem.getLong(i * 8), i);
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    WritableMemory reg = wmem.writableRegion(n2 * 8, n2 * 8, Util.NON_NATIVE_BYTE_ORDER);
    for (int i = 0; i < n2; i++) { reg.putLong(i * 8, i); }
    for (int i = 0; i < n; i++) {
      long v = wmem.getLong(i * 8);
      if (i < n2) {
        assertEquals(v, i % 8);
      } else {
        assertEquals(Long.reverseBytes(v), i % 8);
      }
      //println("" + wmem.getLong(i * 8));
    }
  }

  @Test(expectedExceptions = MemoryInvalidException.class)
  public void checkParentUseAfterFree() throws Exception {
    int bytes = 64 * 8;
    WritableMemory wmem = WritableMemory.allocateDirect(bytes);
    wmem.close();
    wmem.getLong(0);
  }

  @Test(expectedExceptions = MemoryInvalidException.class)
  public void checkRegionUseAfterFree() throws Exception {
    int bytes = 64;
    Memory mem = WritableMemory.allocateDirect(bytes);
    Memory region = mem.region(0L, bytes);
    mem.close();
    region.getByte(0);
  }

  @Test
  public void checkMemReqSvr() throws Exception {
    WritableMemory wmem;
    WritableBuffer wbuf;
    if (Resource.defaultMemReqSvr == null) { //This is a policy choice
      //ON HEAP
      wmem = WritableMemory.writableWrap(new byte[16]);
      assertNull(wmem.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNull(wbuf.getMemoryRequestServer());
      //OFF HEAP
      try (WritableMemory wmem2 = WritableMemory.allocateDirect(16)) { //OFF HEAP
        assertNull(wmem2.getMemoryRequestServer());
        wbuf = wmem2.asWritableBuffer();
        assertNull(wbuf.getMemoryRequestServer());
      }
      //ByteBuffer
      ByteBuffer bb = ByteBuffer.allocate(16);
      wmem = WritableMemory.writableWrap(bb);
      assertNull(wmem.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNull(wbuf.getMemoryRequestServer());
    }

    MemoryRequestServer memReqSvr = new DefaultMemoryRequestServer();

    //ON HEAP
    wmem = WritableMemory.writableWrap(new byte[16], 0, 16, Util.NATIVE_BYTE_ORDER, memReqSvr);
    assertNotNull(wmem.getMemoryRequestServer());
    wbuf = wmem.asWritableBuffer();
    assertNotNull(wbuf.getMemoryRequestServer());
    //OFF HEAP
    try (WritableMemory wmem3 = WritableMemory.allocateDirect(16, Util.NATIVE_BYTE_ORDER, memReqSvr)) {
      assertNotNull(wmem3.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNotNull(wbuf.getMemoryRequestServer());
    }
    //ByteBuffer
    ByteBuffer bb = ByteBuffer.allocate(16);
    wmem = WritableMemory.writableWrap(bb, Util.NATIVE_BYTE_ORDER, memReqSvr);
    assertNotNull(wmem.getMemoryRequestServer());
    wbuf = wmem.asWritableBuffer();
    assertNotNull(wbuf.getMemoryRequestServer());

  }

  @Test
  public void checkSelfEqualsToAndCompareTo() {
    int len = 64;
    WritableMemory wmem = WritableMemory.allocate(len);
    for (int i = 0; i < len; i++) { wmem.putByte(i, (byte) i); }
    assertTrue(wmem.equalTo(0, wmem, 0, len));
    assertFalse(wmem.equalTo(0, wmem, len / 2, len / 2));
    assertEquals(wmem.compareTo(0, len, wmem, 0, len), 0);
    assertTrue(wmem.compareTo(0, 0, wmem, len / 2, len / 2) < 0);
  }

  @Test
  public void wrapBigEndianAsLittle() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    bb.putChar(0, (char)1); //as NNO
    Memory mem = Memory.wrap(bb, ByteOrder.LITTLE_ENDIAN);
    assertEquals(mem.getChar(0), 256);
  }

  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
  }

  static void println(final Object o) {
    if (o == null) { print(LS); }
    else { print(o.toString() + LS); }
  }

  /**
   * @param o value to print
   */
  static void print(final Object o) {
    if (o != null) {
      //System.out.print(o.toString()); //disable here
    }
  }

}
