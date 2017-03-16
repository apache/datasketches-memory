/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class CopyMemoryTest {

  @Test
  public void heapWSource() {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    WritableMemory srcMem = genMem(k1, false); //!empty
    //println(srcMem.toHexString("src: ", 0, k1 << 3));
    WritableMemory dstMem = genMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    //println(dstMem.toHexString("dst: ", 0, k2 << 3));
    check(dstMem, k1, k1, 1);
  }

  @Test
  public void heapROSource() {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    Memory srcMem = genMem(k1, false); //!empty
    WritableMemory dstMem = genMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    check(dstMem, k1, k1, 1);
  }

  @Test
  public void directWSource() {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    WritableResourceHandler wrh = genWRH(k1, false);
    WritableMemory srcMem = wrh.get();
    WritableMemory dstMem = genMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    check(dstMem, k1, k1, 1);
    wrh.close();
  }

  @Test
  public void directROSource() {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    WritableResourceHandler wrh = genWRH(k1, false);
    Memory srcMem = wrh.get();
    WritableMemory dstMem = genMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    check(dstMem, k1, k1, 1);
    wrh.close();
  }

  @Test
  public void heapWSrcRegion() {
    int k1 = 1 << 20; //longs
    //gen baseMem of k1 longs w data
    WritableMemory baseMem = genMem(k1, false); //!empty
    //gen src region of k1/2 longs, off= k1/2
    WritableMemory srcReg = baseMem.writableRegion((k1/2) << 3, (k1/2) << 3);
    WritableMemory dstMem = genMem(2 * k1, true); //empty
    srcReg.copyTo(0, dstMem, k1 << 3, (k1/2) << 3);
    //println(dstMem.toHexString("dstMem: ", k1 << 3, (k1/2) << 3));
    check(dstMem, k1, k1/2, k1/2 + 1);
  }

  @Test
  public void heapROSrcRegion() {
    int k1 = 1 << 20; //longs
    //gen baseMem of k1 longs w data
    WritableMemory baseMem = genMem(k1, false); //!empty
    //gen src region of k1/2 longs, off= k1/2
    Memory srcReg = baseMem.region((k1/2) << 3, (k1/2) << 3);
    WritableMemory dstMem = genMem(2 * k1, true); //empty
    srcReg.copyTo(0, dstMem, k1 << 3, (k1/2) << 3);
    check(dstMem, k1, k1/2, k1/2 + 1);
  }

  @Test
  public void directROSrcRegion() {
    int k1 = 1 << 20; //longs
    //gen baseMem of k1 longs w data, direct
    WritableResourceHandler wrh = genWRH(k1, false);
    Memory baseMem = wrh.get();
    //gen src region of k1/2 longs, off= k1/2
    Memory srcReg = baseMem.region((k1/2) << 3, (k1/2) << 3);
    WritableMemory dstMem = genMem(2 * k1, true); //empty
    srcReg.copyTo(0, dstMem, k1 << 3, (k1/2) << 3);
    check(dstMem, k1, k1/2, k1/2 + 1);
    wrh.close();
  }

  private static void check(Memory mem, int offsetLongs, int lengthLongs, int startValue) {
    int offBytes = offsetLongs << 3;
    for (long i = 0; i < lengthLongs; i++) {
      assertEquals(mem.getLong(offBytes + (i << 3)), i + startValue);
    }
  }

  private static WritableResourceHandler genWRH(int longs, boolean empty) {
    WritableResourceHandler wrh = WritableMemory.allocateDirect(longs << 3);
    WritableMemory mem = wrh.get();
    if (empty) {
      mem.clear();
    } else {
      for (int i = 0; i < longs; i++) { mem.putLong(i << 3, i + 1); }
    }
    return wrh;
  }


  private static WritableMemory genMem(int longs, boolean empty) {
    WritableMemory mem = WritableMemory.allocate(longs << 3);
    if (!empty) {
      for (int i = 0; i < longs; i++) { mem.putLong(i << 3, i + 1); }
    }
    return mem;
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
