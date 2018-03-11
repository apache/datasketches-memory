/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

/**
 * Provide linkage to java.nio.Bits.
 *
 * @author Lee Rhodes
 */
class Bits {
  static final Class<?> BITS_CLASS;
  static final Method BITS_SWAP_INT; //example
  static final Method BITS_RESERVE_MEMORY;

  static final Class<?> VM_CLASS;
  static final Method VM_MAX_DIRECT_MEMORY;
  static final Method VM_IS_DIRECT_MEMORY_PAGE_ALIGNED;


  private static int pageSize = -1;
  private static final long maxDBBMemory;
  private static final boolean isPageAligned;

  static {
    try {
      VM_CLASS = Class.forName("sun.misc.VM");
      VM_MAX_DIRECT_MEMORY = VM_CLASS.getDeclaredMethod("maxDirectMemory");
      VM_MAX_DIRECT_MEMORY.setAccessible(true);
      maxDBBMemory = (long)VM_MAX_DIRECT_MEMORY.invoke(VM_MAX_DIRECT_MEMORY);

      VM_IS_DIRECT_MEMORY_PAGE_ALIGNED = VM_CLASS.getDeclaredMethod("isDirectMemoryPageAligned");
      VM_IS_DIRECT_MEMORY_PAGE_ALIGNED.setAccessible(true);
      isPageAligned = (boolean)VM_IS_DIRECT_MEMORY_PAGE_ALIGNED.invoke(VM_IS_DIRECT_MEMORY_PAGE_ALIGNED);

      BITS_CLASS = Class.forName("java.nio.Bits");
      BITS_RESERVE_MEMORY = BITS_CLASS.getDeclaredMethod("reserveMemory", long.class, int.class);


      BITS_SWAP_INT = BITS_CLASS.getDeclaredMethod("swap", int.class); //EXAMPLE
      BITS_SWAP_INT.setAccessible(true);

    } catch (final Exception e) {
      throw new RuntimeException("Could not acquire java.nio.Bits class: " + e.getClass()
      + UnsafeUtil.tryIllegalAccessPermit);
    }
  }

  static int pageSize() {
    if (pageSize == -1) {
      pageSize = unsafe.pageSize();
    }
    return pageSize;
  }

  static int pageCount(final long bytes) {
    return (int)((bytes + pageSize()) - 1L) / pageSize();
  }

  static long getMaxDirectByteBufferMemory() {
    return maxDBBMemory;
  }

  static boolean isPageAligned() {
    return isPageAligned;
  }



  static int swap(final int x) { //EXAMPLE
    try {
      final Integer x1 = new Integer(x);
      return (int) BITS_SWAP_INT.invoke(null, x1);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void checkVMParams() {
    println("Max Memory: " + maxDBBMemory);
    println("Int Max Value: " + Integer.MAX_VALUE);
    println("Page Aligned: " + isPageAligned);
  }

  @Test
  public void checkSwap() {
    final int swapped = swap(1);
    final String s = Integer.toBinaryString(swapped);
    final String s2 = Util.characterPad(s, 32, '0', false);
    println("1: " + s2);
  }

  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(final String s) {
    System.out.println(s); //disable here
  }

}
