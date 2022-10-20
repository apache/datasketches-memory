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

import static org.apache.datasketches.memory.internal.TestUtils.getResourceFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.datasketches.memory.MmapHandle;
import org.apache.datasketches.memory.DefaultMemoryFactory;
import org.apache.datasketches.memory.DefaultMemoryRequestServer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class MemoryTest {
  private static final String LS = System.getProperty("line.separator");

  @BeforeClass
  public void setReadOnly() {
    TestUtils.setGettysburgAddressFileToReadOnly();
  }

  @Test
  public void checkDirectRoundTrip() throws Exception {
    int n = 1024; //longs
    try (WritableHandle wh = DefaultMemoryFactory.DEFAULT.allocateDirect(n * 8)) {
      WritableMemory mem = wh.getWritable();
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(n * 8);
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      wmem.putLong(i * 8, i);
    }
    for (int i = 0; i < n; i++) {
      long v = wmem.getLong(i * 8);
      assertEquals(v, i);
    }
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(arr, ByteOrder.nativeOrder());
    for (int i = 0; i < n; i++) {
      long v = mem.getLong(i * 8);
      assertEquals(v, i);
    }
    // check 0 length array wraps
    Memory memZeroLengthArrayBoolean = DefaultMemoryFactory.DEFAULT.writableWrap(new boolean[0]);
    Memory memZeroLengthArrayByte = DefaultMemoryFactory.DEFAULT.writableWrap(new byte[0]);
    Memory memZeroLengthArrayChar = DefaultMemoryFactory.DEFAULT.writableWrap(new char[0]);
    Memory memZeroLengthArrayShort = DefaultMemoryFactory.DEFAULT.writableWrap(new short[0]);
    Memory memZeroLengthArrayInt = DefaultMemoryFactory.DEFAULT.writableWrap(new int[0]);
    Memory memZeroLengthArrayLong = DefaultMemoryFactory.DEFAULT.writableWrap(new long[0]);
    Memory memZeroLengthArrayFloat = DefaultMemoryFactory.DEFAULT.writableWrap(new float[0]);
    Memory memZeroLengthArrayDouble = DefaultMemoryFactory.DEFAULT.writableWrap(new double[0]);
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
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.allocate(0));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new boolean[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new byte[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new char[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new short[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new int[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new long[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new float[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.writableWrap(new double[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new boolean[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new byte[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new char[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new short[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new int[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new long[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new float[0]));
    memoryToCheck.add(DefaultMemoryFactory.DEFAULT.wrap(new double[0]));
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.writableWrap(bb);
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
    Memory mem1 = DefaultMemoryFactory.DEFAULT.wrap(arr);
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.writableWrap(bb);
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
    Memory mem1 = DefaultMemoryFactory.DEFAULT.wrap(bb);
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
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(bb);
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
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(slice);
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(n * 8);
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
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(arr);
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
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(arr);
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.writableWrap(arr);
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
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.writableWrap(arr);
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

  @Test(expectedExceptions = AssertionError.class)
  public void checkParentUseAfterFree() throws Exception {
    int bytes = 64 * 8;
    WritableHandle wh = DefaultMemoryFactory.DEFAULT.allocateDirect(bytes);
    WritableMemory wmem = wh.getWritable();
    wh.close();
    //with -ea assert: Memory not valid.
    //with -da sometimes segfaults, sometimes passes!
    wmem.getLong(0);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkRegionUseAfterFree() throws Exception {
    int bytes = 64;
    WritableHandle wh = DefaultMemoryFactory.DEFAULT.allocateDirect(bytes);
    Memory wmem = wh.get();
    Memory region = wmem.region(0L, bytes);
    wh.close();
    //with -ea assert: Memory not valid.
    //with -da sometimes segfaults, sometimes passes!
    region.getByte(0);
  }

  @Test
  public void checkMonitorDirectStats() throws Exception {
    int bytes = 1024;
    long curAllocations = ResourceImpl.getCurrentDirectMemoryAllocations();
    long curAllocated   = ResourceImpl.getCurrentDirectMemoryAllocated();
    if (curAllocations != 0) { System.err.println(curAllocations + " should be zero!"); }
    WritableHandle wh1 = DefaultMemoryFactory.DEFAULT.allocateDirect(bytes);
    WritableHandle wh2 = DefaultMemoryFactory.DEFAULT.allocateDirect(bytes);
    assertEquals(ResourceImpl.getCurrentDirectMemoryAllocations(), 2L + curAllocations);
    assertEquals(ResourceImpl.getCurrentDirectMemoryAllocated(), 2 * bytes + curAllocated);

    wh1.close();
    assertEquals(ResourceImpl.getCurrentDirectMemoryAllocations(), 1L + curAllocations);
    assertEquals(ResourceImpl.getCurrentDirectMemoryAllocated(), bytes + curAllocated);

    wh2.close();
    wh2.close(); //check that it doesn't go negative.
    //even though the handles are closed, these methods are static access
    assertEquals(ResourceImpl.getCurrentDirectMemoryAllocations(), 0L + curAllocations);
    assertEquals(ResourceImpl.getCurrentDirectMemoryAllocated(), 0L + curAllocated);
  }

  @Test
  public void checkMonitorDirectMapStats() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    long bytes = file.length();

    MmapHandle mmh1 = DefaultMemoryFactory.DEFAULT.map(file);
    MmapHandle mmh2 = DefaultMemoryFactory.DEFAULT.map(file);

    assertEquals(ResourceImpl.getCurrentDirectMemoryMapAllocations(), 2L);
    assertEquals(ResourceImpl.getCurrentDirectMemoryMapAllocated(), 2 * bytes);

    mmh1.close();
    assertEquals(ResourceImpl.getCurrentDirectMemoryMapAllocations(), 1L);
    assertEquals(ResourceImpl.getCurrentDirectMemoryMapAllocated(), bytes);

    mmh2.close();
    mmh2.close(); //check that it doesn't go negative.
    //even though the handles are closed, these methods are static access
    assertEquals(ResourceImpl.getCurrentDirectMemoryMapAllocations(), 0L);
    assertEquals(ResourceImpl.getCurrentDirectMemoryMapAllocated(), 0L);
  }

  @Test
  public void checkMemReqSvr() throws Exception {
    WritableMemory wmem;
    WritableBuffer wbuf;
    if (DefaultMemoryRequestServer.DEFAULT == null) { //This is a policy choice
      //ON HEAP
      wmem = DefaultMemoryFactory.DEFAULT.writableWrap(new byte[16]);
      assertNull(wmem.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNull(wbuf.getMemoryRequestServer());
      //OFF HEAP
      try (WritableHandle wdh = DefaultMemoryFactory.DEFAULT.allocateDirect(16)) { //OFF HEAP
        wmem = wdh.getWritable();
        assertNull(wmem.getMemoryRequestServer());
        wbuf = wmem.asWritableBuffer();
        assertNull(wbuf.getMemoryRequestServer());
      }
      //ByteBuffer
      ByteBuffer bb = ByteBuffer.allocate(16);
      wmem = DefaultMemoryFactory.DEFAULT.writableWrap(bb);
      assertNull(wmem.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNull(wbuf.getMemoryRequestServer());
    } else {
      //ON HEAP
      wmem = DefaultMemoryFactory.DEFAULT.writableWrap(new byte[16]);
      assertNotNull(wmem.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNotNull(wbuf.getMemoryRequestServer());
      //OFF HEAP
      try (WritableHandle wdh = DefaultMemoryFactory.DEFAULT.allocateDirect(16)) {
        WritableMemory wmem2 = wdh.getWritable();
        assertNotNull(wmem2.getMemoryRequestServer());
        wbuf = wmem.asWritableBuffer();
        assertNotNull(wbuf.getMemoryRequestServer());
      }
      //ByteBuffer
      ByteBuffer bb = ByteBuffer.allocate(16);
      wmem = DefaultMemoryFactory.DEFAULT.writableWrap(bb);
      assertNotNull(wmem.getMemoryRequestServer());
      wbuf = wmem.asWritableBuffer();
      assertNotNull(wbuf.getMemoryRequestServer());
    }
  }

  @Test
  public void checkHashCode() {
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(32 + 7);
    int hc = wmem.hashCode();
    assertEquals(hc, -1895166923);
  }

  @Test
  public void checkSelfEqualsToAndCompareTo() {
    int len = 64;
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(len);
    for (int i = 0; i < len; i++) { wmem.putByte(i, (byte) i); }
    assertTrue(wmem.equalTo(0, wmem, 0, len));
    assertFalse(wmem.equalTo(0, wmem, len/2, len/2));
    assertEquals(wmem.compareTo(0, len, wmem, 0, len), 0);
    assertTrue(wmem.compareTo(0, 0, wmem, len/2, len/2) < 0);
  }

  @Test
  public void wrapBigEndianAsLittle() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    bb.putChar(0, (char)1); //as NNO
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(bb, ByteOrder.LITTLE_ENDIAN);
    assertEquals(mem.getChar(0), 256);
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
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
