/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class WritableMemoryImplTest {

  //Simple Native direct

  @Test
  public void checkNativeCapacityAndClose() {
    int memCapacity = 64;
    WritableResourceHandler wmh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wmh.get();
    assertEquals(memCapacity, mem.getCapacity());

    wmh.close(); //intentional
    assertFalse(mem.isValid());

    wmh.close(); //intentional, nothing to free
  }

  //Simple Native arrays

  @Test
  public void checkBooleanArray() {
    boolean[] srcArray = { true, false, true, false, false, true, true, false };
    boolean[] dstArray = new boolean[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getBooleanArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getBooleanArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
    assertTrue(mem.hasArray());
  }

  @Test
  public void checkByteArray() {
    byte[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    byte[] dstArray = new byte[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getByteArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getByteArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkCharArray() {
    char[] srcArray = { 1, 2, 3, 4, 5, 6, 7, 8 };
    char[] dstArray = new char[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getCharArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getCharArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkShortArray() {
    short[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    short[] dstArray = new short[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getShortArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getShortArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkIntArray() {
    int[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    int[] dstArray = new int[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getIntArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getIntArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkLongArray() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    long[] dstArray = new long[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getLongArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getLongArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkFloatArray() {
    float[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    float[] dstArray = new float[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getFloatArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getFloatArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test
  public void checkDoubleArray() {
    double[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };
    double[] dstArray = new double[8];

    Memory mem = Memory.wrap(srcArray);
    mem.getDoubleArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }

    WritableMemory wmem = WritableMemory.wrap(srcArray);
    wmem.getDoubleArray(0, dstArray, 0, 8);
    for (int i=0; i<8; i++) {
      assertEquals(dstArray[i], srcArray[i]);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkNullInput() {
    byte[] arr = null;
    Memory.wrap(arr);
  }

  @Test
  public void checkNativeBaseBound() {
    int memCapacity = 64;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.toHexString("Force Assertion Error", memCapacity, 8);
    } catch (AssertionError e) {
      //ok
    }
  }

  @Test
  public void checkNativeSrcArrayBound() {
    long memCapacity = 64;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      byte[] srcArray = { 1, -2, 3, -4 };
      mem.putByteArray(0L, srcArray, 0, 5);
    } catch (AssertionError e) {
      //pass
    }
  }

  //Copy Within tests

  @Test
  public void checkCopyWithinNativeSmall() {
    int memCapacity = 64;
    int half = memCapacity/2;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.clear();

      for (int i=0; i<half; i++) { //fill first half
        mem.putByte(i, (byte) i);
      }

      mem.copyTo(0, mem, half, half);

      for (int i=0; i<half; i++) {
        assertEquals(mem.getByte(i+half), (byte) i);
      }
    }
  }

  @Test
  public void checkCopyWithinNativeLarge() {
    int memCapacity = (2 << 20) + 64;
    int memCapLongs = memCapacity / 8;
    int halfBytes = memCapacity / 2;
    int halfLongs = memCapLongs / 2;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.clear();

      for (int i=0; i < halfLongs; i++) {
        mem.putLong(i*8,  i);
      }

      mem.copyTo(0, mem, halfBytes, halfBytes);

      for (int i=0; i < halfLongs; i++) {
        assertEquals(mem.getLong((i + halfLongs)*8), i);
      }
    }
  }

  @Test
  public void checkCopyWithinNativeOverlap() {
    int memCapacity = 64;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.clear();
      //println(mem.toHexString("Clear 64", 0, memCapacity));

      for (int i=0; i < memCapacity/2; i++) {
        mem.putByte(i, (byte) i);
      }
      //println(mem.toHexString("Set 1st 32 to ints ", 0, memCapacity));

      mem.copyTo(0, mem, memCapacity/4, memCapacity/2);
      fail("Did Not Catch Assertion Error: Region Overlap");
    } catch (AssertionError e) {
      //pass
    }
  }

  @Test
  public void checkCopyWithinNativeSrcBound() {
    int memCapacity = 64;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.copyTo(32, mem, 32, 33);  //hit source bound check
      fail("Did Not Catch Assertion Error: source bound");
    }
    catch (AssertionError e) {
      //pass
    }
  }

  @Test
  public void checkCopyWithinNativeDstBound() {
    int memCapacity = 64;
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.copyTo(0, mem, 32, 33);  //hit dst bound check
      fail("Did Not Catch Assertion Error: dst bound");
    }
    catch (AssertionError e) {
      //pass
    }
  }

  @Test
  public void checkCopyCrossNativeSmall() {
    int memCapacity = 64;
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem1 = wrh1.get();
    WritableResourceHandler wrh2 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem2 = wrh2.get();

    for (int i=0; i < memCapacity; i++) {
      mem1.putByte(i, (byte) i);
    }
    mem2.clear();
    mem1.copyTo(0, mem2, 0, memCapacity);

    for (int i=0; i<memCapacity; i++) {
      assertEquals(mem2.getByte(i), (byte) i);
    }
    wrh1.close();
    wrh2.close();
  }

  @Test
  public void checkCopyCrossNativeLarge() {
    int memCapacity = (2<<20) + 64;
    int memCapLongs = memCapacity / 8;
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem1 = wrh1.get();
    WritableResourceHandler wrh2 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem2 = wrh2.get();

    for (int i=0; i < memCapLongs; i++) {
      mem1.putLong(i*8, i);
    }
    mem2.clear();

    mem1.copyTo(0, mem2, 0, memCapacity);

    for (int i=0; i<memCapLongs; i++) {
      assertEquals(mem2.getLong(i*8), i);
    }
    wrh1.close();
    wrh2.close();
  }

  @Test
  public void checkCopyCrossNativeAndByteArray() {
    int memCapacity = 64;
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem1 = wrh1.get();

    for (int i= 0; i < mem1.getCapacity(); i++) {
      mem1.putByte(i, (byte) i);
    }

    WritableMemory mem2 = WritableMemory.allocate(memCapacity);
    mem1.copyTo(8, mem2, 16, 16);

    for (int i=0; i<16; i++) {
      assertEquals(mem1.getByte(8+i), mem2.getByte(16+i));
    }
    //println(mem2.toHexString("Mem2", 0, (int)mem2.getCapacity()));
    wrh1.close();
  }

  @Test
  public void checkCopyCrossRegionsSameNative() {
    int memCapacity = 128;
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem1 = wrh1.get();

    for (int i= 0; i < mem1.getCapacity(); i++) {
      mem1.putByte(i, (byte) i);
    }
    //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));

    Memory reg1 = mem1.region(8, 16);
    //println(reg1.toHexString("Reg1", 0, (int)reg1.getCapacity()));

    WritableMemory reg2 = mem1.writableRegion(24, 16);
    //println(reg2.toHexString("Reg2", 0, (int)reg2.getCapacity()));
    reg1.copyTo(0, reg2, 0, 16);

    for (int i=0; i<16; i++) {
      assertEquals(reg1.getByte(i), reg2.getByte(i));
      assertEquals(mem1.getByte(8+i), mem1.getByte(24+i));
    }

    //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));
    wrh1.close();
  }

  @Test
  public void checkCopyCrossNativeArrayAndHierarchicalRegions() {
    int memCapacity = 64;
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem1 = wrh1.get();

    for (int i= 0; i < mem1.getCapacity(); i++) { //fill with numbers
      mem1.putByte(i, (byte) i);
    }
    //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));

    WritableMemory mem2 = WritableMemory.allocate(memCapacity);

    Memory reg1 = mem1.region(8, 32);
    Memory reg1B = reg1.region(8, 16);
    //println(reg1.toHexString("Reg1", 0, (int)reg1.getCapacity()));
    //println(reg1B.toHexString("Reg1B", 0, (int)reg1B.getCapacity()));

    WritableMemory reg2 = mem2.writableRegion(32, 16);
    reg1B.copyTo(0, reg2, 0, 16);
    //println(reg2.toHexString("Reg2", 0, (int)reg2.getCapacity()));

    //println(mem2.toHexString("Mem2", 0, (int)mem2.getCapacity()));
    for (int i = 32, j = 16; i < 40; i++, j++) {
      assertEquals(mem2.getByte(i), j);
    }
    wrh1.close();

  }

  //Tests using CommonTests

  @Test
  public void checkToHexStringAllMem() {
    int memCapacity = 48; //must be 48
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh1.get();

    CommonTest.toHexStringAllMemTests(mem); //requires println enabled to visually check

    wrh1.close();
  }

  @Test
  public void checkSetClearMemoryRegions() {
    int memCapacity = 64; //must be 64
    WritableResourceHandler wrh1 = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh1.get();

    CommonTest.setClearMemoryRegionsTests(mem); //requires println enabled to visually check
    for (int i = 0; i < memCapacity; i++) {
      assertEquals(mem.getByte(i), 0);
    }
    wrh1.close();
  }

  @Test
  public void checkSetGet() {
    int memCapacity = 16; //must be 16
    WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh.get();
    assertEquals(mem.getCapacity(), memCapacity);

    CommonTest.setGetTests(mem);

    wrh.close();
  }

  @Test
  public void checkSetGetArrays() {
    int memCapacity = 32;
    WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh.get();
    assertEquals(memCapacity, mem.getCapacity());

    CommonTest.setGetArraysTests(mem);

    wrh.close();
  }

  @Test
  public void checkSetGetPartialArraysWithOffset() {
    int memCapacity = 32;
    WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh.get();
    assertEquals(memCapacity, mem.getCapacity());

    CommonTest.setGetPartialArraysWithOffsetTests(mem);

    wrh.close();
  }

  @Test
  public void checkSetClearIsBits() {
    int memCapacity = 8;
    WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh.get();

    assertEquals(memCapacity, mem.getCapacity());
    mem.clear();

    CommonTest.setClearIsBitsTests(mem);

    wrh.close();
  }

  @Test
  public void checkAtomicMethods() {
    int memCapacity = 8;
    WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity);
    WritableMemory mem = wrh.get();

    assertEquals(mem.getCapacity(), memCapacity);

    CommonTest.atomicMethodTests(mem);

    wrh.close();
  }

  @Test
  public void checkByteBufferWrap() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocate(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());

    for (int i=0; i<memCapacity; i++) {
      byteBuf.put(i, (byte) i);
    }

    WritableMemory mem = WritableMemory.wrap(byteBuf);

    for (int i=0; i<memCapacity; i++) {
      assertEquals(mem.getByte(i), byteBuf.get(i));
    }

    assertTrue(mem.hasByteBuffer());
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

    Memory mem = WritableMemory.wrap(byteBuf);

    for (int i = 0; i < memCapacity; i++) {
      assertEquals(mem.getByte(i), byteBuf.get(i));
    }

    //println(mem.toHexString("HeapBB", 0, memCapacity));
  }

  @Test(expectedExceptions = ReadOnlyMemoryException.class)
  public void checkWrapWithBBReadonly2() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocate(memCapacity);
    byteBuf.order(ByteOrder.nativeOrder());
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();

    WritableMemory.wrap(byteBufRO);
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

  @Test(expectedExceptions = ReadOnlyMemoryException.class)
  public void checkWrapWithDirectBBReadonlyPut() {
    int memCapacity = 64;
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(memCapacity);
    ByteBuffer byteBufRO = byteBuf.asReadOnlyBuffer();
    byteBufRO.order(ByteOrder.nativeOrder());

    WritableMemory.wrap(byteBufRO);
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkLongArrEmptyExcep() {
    long[] arr = new long[0];
    Memory.wrap(arr);
  }

  @Test
  public void checkIsDirect() {
    int memCapacity = 64;
    WritableMemory mem = WritableMemory.allocate(memCapacity);
    assertFalse(mem.isDirect());
    WritableResourceHandler wrh = WritableMemory.allocateDirect(memCapacity);
    mem = wrh.get();
    assertTrue(mem.isDirect());
    wrh.close();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkNullByteArray() {
    byte[] byteArr = null;
    Memory.wrap(byteArr);
  }

  @Test
  public void checkIsReadOnly() {
    long[] srcArray = { 1, -2, 3, -4, 5, -6, 7, -8 };

    WritableMemory mem = WritableMemory.wrap(srcArray);
    assertFalse(mem.isResourceReadOnly());

    Memory memRO = mem.asReadOnly();
    assertFalse(memRO.isResourceReadOnly());

    for (int i = 0; i < mem.getCapacity(); i++) {
      assertEquals(mem.getByte(i), memRO.getByte(i));
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkNullIntArray() {
    int[] intArr = null;
    Memory.wrap(intArr);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkEmptyIntArray() {
    int[] intArr = new int[0];
    Memory.wrap(intArr);
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

    Memory mem1 = Memory.wrap(arr1);
    Memory mem2 = Memory.wrap(arr2);
    Memory mem3 = Memory.wrap(arr3);

    int comp = mem1.compareTo(0, 3, mem2, 0, 3);
    assertEquals(comp, 0);
    comp = mem1.compareTo(0, 4, mem2, 0, 4);
    assertEquals(comp, -1);
    comp = mem2.compareTo(0, 4, mem1, 0, 4);
    assertEquals(comp, 1);
    //different lengths
    comp = mem1.compareTo(0, 4, mem3, 0, 5);
    assertEquals(comp, -1);
    comp = mem3.compareTo(0, 5, mem1, 0, 4);
    assertEquals(comp, 1);
  }

  @Test
  public void checkCompareToDirect() {
    byte[] arr1 = new byte[] {0, 1, 2, 3};
    byte[] arr2 = new byte[] {0, 1, 2, 4};
    byte[] arr3 = new byte[] {0, 1, 2, 3, 4};

    WritableResourceHandler h1 = WritableMemory.allocateDirect(4);
    WritableMemory mem1 = h1.get();
    mem1.putByteArray(0, arr1, 0, 4);

    WritableResourceHandler h2 = WritableMemory.allocateDirect(4);
    WritableMemory mem2 = h2.get();
    mem2.putByteArray(0, arr2, 0, 4);

    WritableResourceHandler h3 = WritableMemory.allocateDirect(5);
    WritableMemory mem3 = h3.get();
    mem3.putByteArray(0, arr3, 0, 5);

    int comp = mem1.compareTo(0, 3, mem2, 0, 3);
    assertEquals(comp, 0);
    comp = mem1.compareTo(0, 4, mem2, 0, 4);
    assertEquals(comp, -1);
    comp = mem2.compareTo(0, 4, mem1, 0, 4);
    assertEquals(comp, 1);
    //different lengths
    comp = mem1.compareTo(0, 4, mem3, 0, 5);
    assertEquals(comp, -1);
    comp = mem3.compareTo(0, 5, mem1, 0, 4);
    assertEquals(comp, 1);

    h1.close();
    h2.close();
    h3.close();
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
