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

import static org.apache.datasketches.memory.Util.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.Util;
import org.apache.datasketches.memory.WritableDirectHandle;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

@SuppressWarnings("javadoc")
public class MemoryTest {
  private static final String LS = System.getProperty("line.separator");

  static final Method GET_CURRENT_DIRECT_MEMORY_ALLOCATIONS;
  static final Method GET_CURRENT_DIRECT_MEMORY_ALLOCATED;
  static final Method GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS;
  static final Method GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATED;
  
  static {
    GET_CURRENT_DIRECT_MEMORY_ALLOCATIONS = 
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "getCurrentDirectMemoryAllocations", (Class<?>[])null); //static
    GET_CURRENT_DIRECT_MEMORY_ALLOCATED =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "getCurrentDirectMemoryAllocated", (Class<?>[])null); //static
    GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS = 
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "getCurrentDirectMemoryMapAllocations", (Class<?>[])null); //static
    GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATED =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "getCurrentDirectMemoryMapAllocated", (Class<?>[])null); //static
  }
  
  private static long getCurrentDirectMemoryAllocations() {
    try {
      return (long) GET_CURRENT_DIRECT_MEMORY_ALLOCATIONS.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static long getCurrentDirectMemoryAllocated() {
    try {
      return (long) GET_CURRENT_DIRECT_MEMORY_ALLOCATED.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static long getCurrentDirectMemoryMapAllocations() {
    try {
      return (long) GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static long getCurrentDirectMemoryMapAllocated() {
    try {
      return (long) GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATED.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  @BeforeClass
  public void setReadOnly() {
    UtilTest.setGettysburgAddressFileToReadOnly();
  }

  @Test
  public void checkDirectRoundTrip() {
    int n = 1024; //longs
    try (WritableHandle wh = WritableMemory.allocateDirect(n * 8)) {
      WritableMemory mem = wh.get();
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
    WritableMemory wmem = WritableMemory.wrap(arr);
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
    Memory memZeroLengthArrayBoolean = WritableMemory.wrap(new boolean[0]);
    Memory memZeroLengthArrayByte = WritableMemory.wrap(new byte[0]);
    Memory memZeroLengthArrayChar = WritableMemory.wrap(new char[0]);
    Memory memZeroLengthArrayShort = WritableMemory.wrap(new short[0]);
    Memory memZeroLengthArrayInt = WritableMemory.wrap(new int[0]);
    Memory memZeroLengthArrayLong = WritableMemory.wrap(new long[0]);
    Memory memZeroLengthArrayFloat = WritableMemory.wrap(new float[0]);
    Memory memZeroLengthArrayDouble = WritableMemory.wrap(new double[0]);
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
    memoryToCheck.add(WritableMemory.wrap(ByteBuffer.allocate(0)));
    memoryToCheck.add(WritableMemory.wrap(new boolean[0]));
    memoryToCheck.add(WritableMemory.wrap(new byte[0]));
    memoryToCheck.add(WritableMemory.wrap(new char[0]));
    memoryToCheck.add(WritableMemory.wrap(new short[0]));
    memoryToCheck.add(WritableMemory.wrap(new int[0]));
    memoryToCheck.add(WritableMemory.wrap(new long[0]));
    memoryToCheck.add(WritableMemory.wrap(new float[0]));
    memoryToCheck.add(WritableMemory.wrap(new double[0]));
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
    WritableMemory wmem = WritableMemory.wrap(bb);
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
    WritableMemory wmem = WritableMemory.wrap(bb);
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
    Memory reg = mem.region(n2 * 8, n2 * 8, Util.nonNativeByteOrder); //top half
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
    WritableMemory wmem = WritableMemory.wrap(arr);
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
    WritableMemory wmem = WritableMemory.wrap(arr);
    for (int i = 0; i < n; i++) {
      assertEquals(wmem.getLong(i * 8), i);
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    WritableMemory reg = wmem.writableRegion(n2 * 8, n2 * 8, Util.nonNativeByteOrder);
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
    WritableHandle wh = WritableMemory.allocateDirect(bytes);
    WritableMemory wmem = wh.get();
    wh.close();
    //with -ea assert: Memory not valid.
    //with -da sometimes segfaults, sometimes passes!
    wmem.getLong(0);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkRegionUseAfterFree() {
    int bytes = 64;
    @SuppressWarnings("resource") //intentionally not using try-with-resouces here
    WritableHandle wh = WritableMemory.allocateDirect(bytes);
    Memory wmem = wh.get();
    Memory region = wmem.region(0L, bytes);
    wh.close();
    //with -ea assert: Memory not valid.
    //with -da sometimes segfaults, sometimes passes!
    region.getByte(0);
  }

  @Test
  public void checkUnsafeByteBufferView() {
    try (WritableDirectHandle wmemDirectHandle = WritableMemory.allocateDirect(2)) {
      WritableMemory wmemDirect = wmemDirectHandle.get();
      wmemDirect.putByte(0, (byte) 1);
      wmemDirect.putByte(1, (byte) 2);
      checkUnsafeByteBufferView(wmemDirect);
    }

    checkUnsafeByteBufferView(Memory.wrap(new byte[] {1, 2}));

    try {
      @SuppressWarnings("unused")
      ByteBuffer unused = Memory.wrap(new int[]{1}).unsafeByteBufferView(0, 1);
      Assert.fail();
    } catch (UnsupportedOperationException ingore) {
      // expected
    }
  }

  private static void checkUnsafeByteBufferView(final Memory mem) {
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

  @SuppressWarnings("resource")
  @Test
  public void checkMonitorDirectStats() {
    int bytes = 1024;
    WritableHandle wh1 = WritableMemory.allocateDirect(bytes);
    WritableHandle wh2 = WritableMemory.allocateDirect(bytes);
    assertEquals(getCurrentDirectMemoryAllocations(), 2L);
    assertEquals(getCurrentDirectMemoryAllocated(), 2 * bytes);

    wh1.close();
    assertEquals(getCurrentDirectMemoryAllocations(), 1L);
    assertEquals(getCurrentDirectMemoryAllocated(), bytes);

    wh2.close();
    wh2.close(); //check that it doesn't go negative.
    assertEquals(getCurrentDirectMemoryAllocations(), 0L);
    assertEquals(getCurrentDirectMemoryAllocated(), 0L);
  }

  @SuppressWarnings("resource")
  @Test
  public void checkMonitorDirectMapStats() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    long bytes = file.length();

    MapHandle mmh1 = Memory.map(file);
    MapHandle mmh2 = Memory.map(file);

    assertEquals(getCurrentDirectMemoryMapAllocations(), 2L);
    assertEquals(getCurrentDirectMemoryMapAllocated(), 2 * bytes);

    mmh1.close();
    assertEquals(getCurrentDirectMemoryMapAllocations(), 1L);
    assertEquals(getCurrentDirectMemoryMapAllocated(), bytes);

    mmh2.close();
    mmh2.close(); //check that it doesn't go negative.
    assertEquals(getCurrentDirectMemoryMapAllocations(), 0L);
    assertEquals(getCurrentDirectMemoryMapAllocated(), 0L);
  }

  @Test
  public void checkNullMemReqSvr() {
    WritableMemory wmem = WritableMemory.wrap(new byte[16]);
    assertNull(wmem.getMemoryRequestServer());
    try (WritableDirectHandle wdh = WritableMemory.allocateDirect(16)) {
      WritableMemory wmem2 = wdh.get();
      assertNotNull(wmem2.getMemoryRequestServer());
    }
    println(wmem.toHexString("Test", 0, 16));
  }

  @Test
  public void checkHashCode() {
    WritableMemory wmem = WritableMemory.allocate(32 + 7);
    int hc = wmem.hashCode();
    assertEquals(hc, -1895166923);
  }

  @Test
  public void checkSelfEqualsToAndCompareTo() {
    int len = 64;
    WritableMemory wmem = WritableMemory.allocate(len);
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
    Memory mem = Memory.wrap(bb, ByteOrder.LITTLE_ENDIAN);
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