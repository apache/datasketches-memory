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

package org.apache.datasketches.memory.test;

import static org.apache.datasketches.memory.internal.Util.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.WritableDirectHandle;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.internal.MemoryImpl;
import org.apache.datasketches.memory.internal.Util;
import org.apache.datasketches.memory.internal.WritableMemoryImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

@SuppressWarnings("javadoc")
public class MemoryTest {
  private static final String LS = System.getProperty("line.separator");
  
  @BeforeClass
  public void setReadOnly() {
    UtilTest.setGettysburgAddressFileToReadOnly();
  }

  @Test
  public void checkDirectRoundTrip() {
    int n = 1024; //longs
    try (WritableHandle wh = WritableMemoryImpl.allocateDirect(n * 8)) {
      WritableMemoryImpl mem = wh.get();
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
    WritableMemoryImpl wmem = WritableMemoryImpl.allocate(n * 8);
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
    WritableMemoryImpl wmem = WritableMemoryImpl.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      wmem.putLong(i * 8, i);
    }
    for (int i = 0; i < n; i++) {
      long v = wmem.getLong(i * 8);
      assertEquals(v, i);
    }
    MemoryImpl mem = MemoryImpl.wrap(arr, ByteOrder.nativeOrder());
    for (int i = 0; i < n; i++) {
      long v = mem.getLong(i * 8);
      assertEquals(v, i);
    }
    // check 0 length array wraps
    MemoryImpl memZeroLengthArrayBoolean = WritableMemoryImpl.writableWrap(new boolean[0]);
    MemoryImpl memZeroLengthArrayByte = WritableMemoryImpl.writableWrap(new byte[0]);
    MemoryImpl memZeroLengthArrayChar = WritableMemoryImpl.writableWrap(new char[0]);
    MemoryImpl memZeroLengthArrayShort = WritableMemoryImpl.writableWrap(new short[0]);
    MemoryImpl memZeroLengthArrayInt = WritableMemoryImpl.writableWrap(new int[0]);
    MemoryImpl memZeroLengthArrayLong = WritableMemoryImpl.writableWrap(new long[0]);
    MemoryImpl memZeroLengthArrayFloat = WritableMemoryImpl.writableWrap(new float[0]);
    MemoryImpl memZeroLengthArrayDouble = WritableMemoryImpl.writableWrap(new double[0]);
    assertEquals(memZeroLengthArrayBoolean.getCapacity(), 0);
    assertEquals(memZeroLengthArrayByte.getCapacity(), 0);
    assertEquals(memZeroLengthArrayChar.getCapacity(), 0);
    assertEquals(memZeroLengthArrayShort.getCapacity(), 0);
    assertEquals(memZeroLengthArrayInt.getCapacity(), 0);
    assertEquals(memZeroLengthArrayLong.getCapacity(), 0);
    assertEquals(memZeroLengthArrayFloat.getCapacity(), 0);
    assertEquals(memZeroLengthArrayDouble.getCapacity(), 0);

    // check 0 length array wraps
    List<MemoryImpl> memoryToCheck = Lists.newArrayList();
    memoryToCheck.add(WritableMemoryImpl.allocate(0));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new boolean[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new byte[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new char[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new short[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new int[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new long[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new float[0]));
    memoryToCheck.add(WritableMemoryImpl.writableWrap(new double[0]));
    memoryToCheck.add(MemoryImpl.wrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(MemoryImpl.wrap(new boolean[0]));
    memoryToCheck.add(MemoryImpl.wrap(new byte[0]));
    memoryToCheck.add(MemoryImpl.wrap(new char[0]));
    memoryToCheck.add(MemoryImpl.wrap(new short[0]));
    memoryToCheck.add(MemoryImpl.wrap(new int[0]));
    memoryToCheck.add(MemoryImpl.wrap(new long[0]));
    memoryToCheck.add(MemoryImpl.wrap(new float[0]));
    memoryToCheck.add(MemoryImpl.wrap(new double[0]));
    //Check the MemoryImpl lengths
    for (MemoryImpl memory : memoryToCheck) {
      assertEquals(memory.getCapacity(), 0);
    }
  }

  @Test
  public void checkByteBufHeap() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    ByteBuffer bb = ByteBuffer.wrap(arr);
    bb.order(ByteOrder.nativeOrder());
    WritableMemoryImpl wmem = WritableMemoryImpl.writableWrap(bb);
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
    MemoryImpl mem1 = MemoryImpl.wrap(arr);
    for (int i = 0; i < n; i++) { //read from wrapped arr
      long v = mem1.getLong(i * 8);
      assertEquals(v, i);
    }
    //convert to RO
    MemoryImpl mem = wmem;
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
    WritableMemoryImpl wmem = WritableMemoryImpl.writableWrap(bb);
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
    MemoryImpl mem1 = MemoryImpl.wrap(bb);
    for (int i = 0; i < n; i++) { //read from wrapped bb RO
      long v = mem1.getLong(i * 8);
      assertEquals(v, i);
    }
    //convert to RO
    MemoryImpl mem = wmem;
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
    MemoryImpl mem = MemoryImpl.wrap(bb);
    assertFalse(mem.getTypeByteOrder() == Util.nativeByteOrder);
    assertEquals(mem.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkReadOnlyHeapByteBuffer() {
    ByteBuffer bb = ByteBuffer.allocate(128);
    bb.order(ByteOrder.nativeOrder());
    for (int i = 0; i < 128; i++) { bb.put(i, (byte)i); }
    bb.position(64);
    ByteBuffer slice = bb.slice().asReadOnlyBuffer();
    slice.order(ByteOrder.nativeOrder());
    MemoryImpl mem = MemoryImpl.wrap(slice);
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
    WritableMemoryImpl wmem = WritableMemoryImpl.allocate(n * 8);
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
    MemoryImpl mem = MemoryImpl.wrap(arr);
    MemoryImpl reg = mem.region(n2 * 8, n2 * 8); //top half
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
    MemoryImpl mem = MemoryImpl.wrap(arr);
    MemoryImpl reg = mem.region(n2 * 8, n2 * 8, Util.nonNativeByteOrder); //top half
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
    WritableMemoryImpl wmem = WritableMemoryImpl.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      assertEquals(wmem.getLong(i * 8), i);
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    WritableMemoryImpl reg = wmem.writableRegion(n2 * 8, n2 * 8);
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
    WritableMemoryImpl wmem = WritableMemoryImpl.writableWrap(arr);
    for (int i = 0; i < n; i++) {
      assertEquals(wmem.getLong(i * 8), i);
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    WritableMemoryImpl reg = wmem.writableRegion(n2 * 8, n2 * 8, Util.nonNativeByteOrder);
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
  public void checkParentUseAfterFree() {
    int bytes = 64 * 8;
    @SuppressWarnings("resource") //intentionally not using try-with-resouces here
    WritableHandle wh = WritableMemoryImpl.allocateDirect(bytes);
    WritableMemoryImpl wmem = wh.get();
    wh.close();
    //with -ea assert: MemoryImpl not valid.
    //with -da sometimes segfaults, sometimes passes!
    wmem.getLong(0);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkRegionUseAfterFree() {
    int bytes = 64;
    @SuppressWarnings("resource") //intentionally not using try-with-resouces here
    WritableHandle wh = WritableMemoryImpl.allocateDirect(bytes);
    MemoryImpl wmem = wh.get();
    MemoryImpl region = wmem.region(0L, bytes);
    wh.close();
    //with -ea assert: MemoryImpl not valid.
    //with -da sometimes segfaults, sometimes passes!
    region.getByte(0);
  }

  @Test
  public void checkUnsafeByteBufferView() {
    try (WritableDirectHandle wmemDirectHandle = WritableMemoryImpl.allocateDirect(2)) {
      WritableMemoryImpl wmemDirect = wmemDirectHandle.get();
      wmemDirect.putByte(0, (byte) 1);
      wmemDirect.putByte(1, (byte) 2);
      checkUnsafeByteBufferView(wmemDirect);
    }

    checkUnsafeByteBufferView(MemoryImpl.wrap(new byte[] {1, 2}));

    try {
      @SuppressWarnings("unused")
      ByteBuffer unused = MemoryImpl.wrap(new int[]{1}).unsafeByteBufferView(0, 1);
      Assert.fail();
    } catch (UnsupportedOperationException ingore) {
      // expected
    }
  }

  private static void checkUnsafeByteBufferView(final MemoryImpl mem) {
    ByteBuffer emptyByteBuffer = mem.unsafeByteBufferView(0, 0);
    Assert.assertEquals(emptyByteBuffer.capacity(), 0);
    ByteBuffer bb = mem.unsafeByteBufferView(1, 1);
    Assert.assertTrue(bb.isReadOnly());
    Assert.assertEquals(bb.capacity(), 1);
    Assert.assertEquals(bb.get(), 2);

    try {
      @SuppressWarnings("unused")
      ByteBuffer unused = mem.unsafeByteBufferView(1, 2);
      Assert.fail();
    } catch (IllegalArgumentException ignore) {
      // expected
    }
  }

  @SuppressWarnings({ "resource", "static-access" })
  @Test
  public void checkMonitorDirectStats() {
    int bytes = 1024;
    WritableHandle wh1 = WritableMemoryImpl.allocateDirect(bytes);
    WritableHandle wh2 = WritableMemoryImpl.allocateDirect(bytes);
    WritableMemoryImpl wMem2 = wh2.get();
    assertEquals(wMem2.getCurrentDirectMemoryAllocations(), 2L);
    assertEquals(wMem2.getCurrentDirectMemoryAllocated(), 2 * bytes);

    wh1.close();
    assertEquals(wMem2.getCurrentDirectMemoryAllocations(), 1L);
    assertEquals(wMem2.getCurrentDirectMemoryAllocated(), bytes);

    wh2.close();
    wh2.close(); //check that it doesn't go negative.
    //even though the handles are closed, these methods are static access
    assertEquals(wMem2.getCurrentDirectMemoryAllocations(), 0L);
    assertEquals(wMem2.getCurrentDirectMemoryAllocated(), 0L);
  }

  @SuppressWarnings({ "resource", "static-access" })
  @Test
  public void checkMonitorDirectMapStats() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    long bytes = file.length();

    MapHandle mmh1 = MemoryImpl.map(file);
    MapHandle mmh2 = MemoryImpl.map(file);
    MemoryImpl wmem2 = mmh2.get();
    
    assertEquals(wmem2.getCurrentDirectMemoryMapAllocations(), 2L);
    assertEquals(wmem2.getCurrentDirectMemoryMapAllocated(), 2 * bytes);

    mmh1.close();
    assertEquals(wmem2.getCurrentDirectMemoryMapAllocations(), 1L);
    assertEquals(wmem2.getCurrentDirectMemoryMapAllocated(), bytes);

    mmh2.close();
    mmh2.close(); //check that it doesn't go negative.
    //even though the handles are closed, these methods are static access
    assertEquals(wmem2.getCurrentDirectMemoryMapAllocations(), 0L);
    assertEquals(wmem2.getCurrentDirectMemoryMapAllocated(), 0L);
  }

  @Test
  public void checkNullMemReqSvr() {
    WritableMemoryImpl wmem = WritableMemoryImpl.writableWrap(new byte[16]);
    assertNull(wmem.getMemoryRequestServer());
    try (WritableDirectHandle wdh = WritableMemoryImpl.allocateDirect(16)) {
      WritableMemoryImpl wmem2 = wdh.get();
      assertNotNull(wmem2.getMemoryRequestServer());
    }
    println(wmem.toHexString("Test", 0, 16));
  }

  @Test
  public void checkHashCode() {
    WritableMemoryImpl wmem = WritableMemoryImpl.allocate(32 + 7);
    int hc = wmem.hashCode();
    assertEquals(hc, -1895166923);
  }

  @Test
  public void checkSelfEqualsToAndCompareTo() {
    int len = 64;
    WritableMemoryImpl wmem = WritableMemoryImpl.allocate(len);
    for (int i = 0; i < len; i++) { wmem.putByte(i, (byte) i); }
    assertTrue(wmem.equalTo(0, wmem, 0, len));
    assertFalse(wmem.equalTo(0, wmem, len/2, len/2));
    assertEquals(wmem.compareTo(0, len, wmem, 0, len), 0);
    assertTrue(wmem.compareTo(0, 0, wmem, len/2, len/2) < 0);
  }

  @Test
  public void wrapBigEndianAsLittle() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    bb.putChar(0, (char)1); //as BE
    MemoryImpl mem = MemoryImpl.wrap(bb, ByteOrder.LITTLE_ENDIAN);
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
