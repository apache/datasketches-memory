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

package org.apache.datasketches.memory.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static org.testng.Assert.assertEquals;

import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CopyMemoryTest {
  private static final MemoryRequestServer memReqSvr = Resource.defaultMemReqSvr;

  @Test
  public void heapWSource() {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    WritableMemory srcMem = genHeapMem(k1, false); //!empty
    //println(srcMem.toHexString("src: ", 0, k1 << 3));
    WritableMemory dstMem = genHeapMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    //println(dstMem.toHexString("dst: ", 0, k2 << 3));
    check(dstMem, k1, k1, 1);
  }

  @Test
  public void heapROSource() {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    Memory srcMem = genHeapMem(k1, false); //!empty
    WritableMemory dstMem = genHeapMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    check(dstMem, k1, k1, 1);
  }

  @Test
  public void directWSource() throws Exception {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    WritableMemory srcMem = genOffHeapMem(k1, false);
    WritableMemory dstMem = genHeapMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    check(dstMem, k1, k1, 1);
    srcMem.getArena().close();
  }

  @Test
  public void directROSource() throws Exception {
    int k1 = 1 << 20; //longs
    int k2 = 2 * k1;
    Memory srcMem = genOffHeapMem(k1, false);
    WritableMemory dstMem = genHeapMem(k2, true);
    srcMem.copyTo(0, dstMem, k1 << 3, k1 << 3);
    check(dstMem, k1, k1, 1);
    srcMem.getArena().close();
    }

  @Test
  public void heapWSrcRegion() {
    int k1 = 1 << 20; //longs
    //gen baseMem of k1 longs w data
    WritableMemory baseMem = genHeapMem(k1, false); //!empty
    //gen src region of k1/2 longs, off= k1/2
    WritableMemory srcReg = baseMem.writableRegion((k1/2) << 3, (k1/2) << 3);
    WritableMemory dstMem = genHeapMem(2 * k1, true); //empty
    srcReg.copyTo(0, dstMem, k1 << 3, (k1/2) << 3);
    //println(dstMem.toHexString("dstMem: ", k1 << 3, (k1/2) << 3));
    check(dstMem, k1, k1/2, (k1/2) + 1);
  }

  @Test
  public void heapROSrcRegion() {
    int k1 = 1 << 20; //longs
    //gen baseMem of k1 longs w data
    WritableMemory baseMem = genHeapMem(k1, false); //!empty
    //gen src region of k1/2 longs, off= k1/2
    Memory srcReg = baseMem.region((k1/2) << 3, (k1/2) << 3);
    WritableMemory dstMem = genHeapMem(2 * k1, true); //empty
    srcReg.copyTo(0, dstMem, k1 << 3, (k1/2) << 3);
    check(dstMem, k1, k1/2, (k1/2) + 1);
  }

  @Test
  public void directROSrcRegion() throws Exception {
    int k1 = 1 << 20; //longs
    //gen baseMem of k1 longs w data, direct
    Memory baseMem = genOffHeapMem(k1, false);
    //gen src region of k1/2 longs, off= k1/2
    Memory srcReg = baseMem.region((k1/2) << 3, (k1/2) << 3);
    WritableMemory dstMem = genHeapMem(2 * k1, true); //empty
    srcReg.copyTo(0, dstMem, k1 << 3, (k1/2) << 3);
    check(dstMem, k1, k1/2, (k1/2) + 1);
    baseMem.getArena().close();
  }

  @Test
  public void testOverlappingCopyLeftToRight() {
    byte[] bytes = new byte[(((1 << 20) * 5) / 2) + 1];
    ThreadLocalRandom.current().nextBytes(bytes);
    byte[] referenceBytes = bytes.clone();
    Memory referenceMem = Memory.wrap(referenceBytes);
    WritableMemory mem = WritableMemory.writableWrap(bytes);
    long copyLen = (1 << 20) * 2;
    mem.copyTo(0, mem, (1 << 20) / 2, copyLen);
    Assert.assertEquals(0, mem.compareTo((1 << 20) / 2, copyLen, referenceMem, 0, copyLen));
  }

  @Test
  public void testOverlappingCopyRightToLeft() {
    byte[] bytes = new byte[(((1 << 20) * 5) / 2) + 1];
    ThreadLocalRandom.current().nextBytes(bytes);
    byte[] referenceBytes = bytes.clone();
    Memory referenceMem = Memory.wrap(referenceBytes);
    WritableMemory mem = WritableMemory.writableWrap(bytes);
    long copyLen = (1 << 20) * 2;
    mem.copyTo((1 << 20) / 2, mem, 0, copyLen);
    Assert.assertEquals(0, mem.compareTo(0, copyLen, referenceMem, (1 << 20) / 2, copyLen));
  }

  private static void check(Memory mem, int offsetLongs, int lengthLongs, int startValue) {
    int offBytes = offsetLongs << 3;
    for (long i = 0; i < lengthLongs; i++) {
      assertEquals(mem.getLong(offBytes + (i << 3)), i + startValue);
    }
  }

  private static WritableMemory genOffHeapMem(int longs, boolean empty) {
    WritableMemory wmem = WritableMemory.allocateDirect(longs << 3, 1, ByteOrder.nativeOrder(), memReqSvr, Arena.ofConfined());
    if (empty) {
      wmem.clear();
    } else {
      for (int i = 0; i < longs; i++) { wmem.putLong(i << 3, i + 1); }
    }
    return wmem;
  }

  private static WritableMemory genHeapMem(int longs, boolean empty) {
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
  static void println(Object o) {
    //System.out.println(o); //disable here
  }
}
