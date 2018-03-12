/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NioBitsTest {

  @Test
  public void checkVMParams() {
    println("Max Memory: " + NioBits.getMaxDirectByteBufferMemory());
    println("Page Aligned: " + NioBits.isPageAligned());
    println("Page Size: " + NioBits.pageSize());
  }

  @Test
  //testing this beyond 2GB may not work on JVMs < 8GB.
  //This must be checked manually
  public void checkGetAtomicFields() {
    long cap = 1024L;// + Integer.MAX_VALUE;
    printStats();
    NioBits.reserveMemory(cap);
    printStats();
    NioBits.unreserveMemory(cap);
    printStats();
  }

  @Test
  public void checkPageCount() {
    assertEquals(NioBits.pageCount(0), 0);
    assertEquals(NioBits.pageCount(1), 1);
  }

  private static void printStats() {
    long count = NioBits.getCount();
    long resMem = NioBits.getReservedMemory();
    long totCap = NioBits.getTotalCapacity();
    long maxDBBmem = NioBits.getMaxDirectByteBufferMemory();
    String s = String.format("%,10d\t%,15d\t%,15d\t%,15d", count, resMem, totCap, maxDBBmem);
    println(s);
  }


  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(final String s) {
    //System.out.println(s); //disable here
  }

}
