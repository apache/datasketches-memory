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

package org.apache.datasketches.memory.test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("javadoc")
public class NioBitsTest {

  static final Method GET_MAX_DIRECT_BYTE_BUFFER_MEMORY;
  static final Method IS_PAGE_ALIGHED;
  static final Method PAGE_SIZE;
  static final Method PAGE_COUNT;
  static final Method GET_DIRECT_ALLOCATIONS_COUNT;
  static final Method GET_RESERVED_MEMORY;
  static final Method GET_TOTAL_CAPACITY;
  static final Method RESERVE_MEMORY;
  static final Method UNRESERVE_MEMORY;
  
  static {
    GET_MAX_DIRECT_BYTE_BUFFER_MEMORY =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "getMaxDirectByteBufferMemory", (Class<?>[])null); //static
    IS_PAGE_ALIGHED =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "isPageAligned", (Class<?>[])null); //static
    PAGE_SIZE =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "pageSize", (Class<?>[])null); //static
    PAGE_COUNT =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "pageCount", long.class); //static
    GET_DIRECT_ALLOCATIONS_COUNT =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "getDirectAllocationsCount", (Class<?>[])null); //static
    GET_RESERVED_MEMORY =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "getReservedMemory", (Class<?>[])null); //static
    GET_TOTAL_CAPACITY =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "getTotalCapacity", (Class<?>[])null); //static
    RESERVE_MEMORY =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "reserveMemory", long.class, long.class); //static
    UNRESERVE_MEMORY =
        ReflectUtil.getMethod(ReflectUtil.NIO_BITS, "unreserveMemory", long.class, long.class); //static
  }

  private static long getMaxDirectByteBufferMemory() {
    try {
      return (long) GET_MAX_DIRECT_BYTE_BUFFER_MEMORY.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isPageAligned() {
    try {
      return (boolean) IS_PAGE_ALIGHED.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static int pageSize() {
    try {
      return (int) PAGE_SIZE.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static int pageCount(final long bytes) {
    try {
      return (int) PAGE_COUNT.invoke(null, bytes);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static long getDirectAllocationsCount() {
    try {
      return (long) GET_DIRECT_ALLOCATIONS_COUNT.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static long getReservedMemory() {
    try {
      return (long) GET_RESERVED_MEMORY.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static long getTotalCapacity() {
    try {
      return (long) GET_TOTAL_CAPACITY.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static void reserveMemory(final long allocationSize, final long capacity) {
    try {
     RESERVE_MEMORY.invoke(null, allocationSize, capacity);
    } catch (Exception e) { 
      throw new RuntimeException(e); }
  }
  private static void unreserveMemory(final long allocationSize, final long capacity) {
    try {
      UNRESERVE_MEMORY.invoke(null, allocationSize, capacity);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  @Test
  public void checkVMParams() {
    println("Max Memory: " + getMaxDirectByteBufferMemory());
    println("Page Aligned: " + isPageAligned());
    println("Page Size: " + pageSize());
  }
  
  @Test
  //testing this beyond 2GB may not work on JVMs < 8GB.
  //This must be checked manually
  public void checkGetAtomicFields() {
    long cap = 1024L + Integer.MAX_VALUE;
    printStats();
    reserveMemory(cap, cap);
    printStats();
    unreserveMemory(cap, cap);
    printStats();
  }

  @Test
  public void checkPageCount() {
    assertEquals(pageCount(0), 0);
    assertEquals(pageCount(1), 1);
  }

  private static void printStats() {
    long count = getDirectAllocationsCount();
    long resMem = getReservedMemory();
    long totCap = getTotalCapacity();
    long maxDBBmem = getMaxDirectByteBufferMemory();
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
