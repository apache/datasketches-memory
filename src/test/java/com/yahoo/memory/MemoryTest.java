/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class MemoryTest {

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
    assertTrue(mem.isSwapBytes());
    assertEquals(mem.getResourceByteOrder(), ByteOrder.BIG_ENDIAN);
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
    Memory reg = mem.region(n2 * 8, n2 * 8);
    for (int i = 0; i < n2; i++) {
      assertEquals(reg.getLong(i * 8), i + n2);
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

  @SuppressWarnings("resource")
  @Test
  public void checkMonitorDirectStats() {
    int bytes = 1024;
    WritableHandle wh1 = WritableMemory.allocateDirect(bytes);
    WritableHandle wh2 = WritableMemory.allocateDirect(bytes);
    assertEquals(Memory.getCurrentDirectMemoryAllocations(), 2L);
    assertEquals(Memory.getCurrentDirectMemoryAllocated(), 2 * bytes);

    wh1.close();
    assertEquals(Memory.getCurrentDirectMemoryAllocations(), 1L);
    assertEquals(Memory.getCurrentDirectMemoryAllocated(), bytes);

    wh2.close();
    wh2.close(); //check that it doesn't go negative.
    assertEquals(Memory.getCurrentDirectMemoryAllocations(), 0L);
    assertEquals(Memory.getCurrentDirectMemoryAllocated(), 0L);
  }

  @SuppressWarnings("resource")
  @Test
  public void checkMonitorDirectMapStats() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long bytes = file.length();

    MapHandle mmh1 = Memory.map(file);
    MapHandle mmh2 = Memory.map(file);

    assertEquals(Memory.getCurrentDirectMemoryMapAllocations(), 2L);
    assertEquals(Memory.getCurrentDirectMemoryMapAllocated(), 2 * bytes);

    mmh1.close();
    assertEquals(Memory.getCurrentDirectMemoryMapAllocations(), 1L);
    assertEquals(Memory.getCurrentDirectMemoryMapAllocated(), bytes);

    mmh2.close();
    mmh2.close(); //check that it doesn't go negative.
    assertEquals(Memory.getCurrentDirectMemoryMapAllocations(), 0L);
    assertEquals(Memory.getCurrentDirectMemoryMapAllocated(), 0L);
  }

  @Test
  public void checkNullMemReqSvr() {
    Memory mem = Memory.wrap(new byte[16]);
    ResourceState state = mem.getResourceState();
    state.putMemoryRequestServer(null);
    println(mem.toHexString("Test", 0, 16));
  }

  @Test
  public void checkHashCode() {
    WritableMemory wmem = WritableMemory.allocate(32 + 7);
    int hc = wmem.hashCode();
    assertEquals(hc, 28629151);
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
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(final String s) {
    //System.out.println(s);
  }

}
