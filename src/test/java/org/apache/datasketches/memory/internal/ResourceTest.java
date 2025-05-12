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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class ResourceTest {
  private static final MemoryRequestServer memReqSvr = Resource.defaultMemReqSvr;

  @Test
  public void checkCompare() {
    byte[] arr1 = {1,2,3,4,5,6,7,8,9};
    byte[] arr2 = {1,2,3,4,5,6,7,8};
    MemorySegment seg1 = MemorySegment.ofArray(arr1);
    MemorySegment seg2 = MemorySegment.ofArray(arr2);
    int c = ResourceImpl.compare(seg1, 0, 9, seg2, 0, 8);
    //println(c);
    assertEquals(c, 1);
  }

  @Test
  public void checkCompareToDirect() throws Exception {
    byte[] arr1 = new byte[] {0, 1, 2, 3};
    byte[] arr2 = new byte[] {0, 1, 2, 4};
    byte[] arr3 = new byte[] {0, 1, 2, 3, 4};

    try (Arena arena = Arena.ofConfined()) {
      WritableMemory mem1 = WritableMemory.allocateDirect(4, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableMemory mem2 = WritableMemory.allocateDirect(4, 1, ByteOrder.nativeOrder(), memReqSvr, arena);
      WritableMemory mem3 = WritableMemory.allocateDirect(5, 1, ByteOrder.nativeOrder(), memReqSvr, arena);

      mem1.putByteArray(0, arr1, 0, 4);
      mem2.putByteArray(0, arr2, 0, 4);
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
    }
  }



  @Test
  public void checkCompareToHeap() {
    byte[] arr1 = new byte[] {0, 1, 2, 3};
    byte[] arr2 = new byte[] {0, 1, 2, 4};
    byte[] arr3 = new byte[] {0, 1, 2, 3, 4};

    Memory mem1 = Memory.wrap(arr1);
    Memory mem2 = Memory.wrap(arr2);
    Memory mem3 = Memory.wrap(arr3);
    Memory mem4 = Memory.wrap(arr3); //same resource

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
    comp = mem3.compareTo(0, 5, mem4, 0, 5);
    assertEquals(comp, 0);
    comp = mem3.compareTo(0, 4, mem4, 1, 4);
    assertEquals(comp, -1);
    ResourceImpl.checkBounds(0, 5, mem3.getCapacity());
  }

  @Test
  public void checkCompareToSamePrefix() {
    WritableMemory wmem = WritableMemory.allocate(3); wmem.clear();
    //The MemorySegment.mismatch(...) returns -1 in all 3 cases: but we need to consider order.
    assertEquals(wmem.compareTo(0, 1, wmem, 0, 2), -1); //{[0],x,x}, {[0,0],x}
    assertEquals(wmem.compareTo(1, 1, wmem, 1, 1),  0); //{x,[0],x}, {{x,[0],x}
    assertEquals(wmem.compareTo(1, 2, wmem, 1, 1),  1); //{x,[0,0]}, {{x,[0],x}
  }

  @Test
  public void checkGetRelativeOffset() {
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(1024, arena);
      WritableMemory reg = wmem.writableRegion(512, 256);
      long off = wmem.getRelativeOffset(reg);
      assertEquals(off, 512);
    }
  }

  @Test
  public void checkIsByteOrderCompatible() {
    WritableMemory wmem = WritableMemory.allocate(8);
    assertTrue(wmem.isByteOrderCompatible(ByteOrder.nativeOrder()));
  }

  @Test
  public void checkIsSameResource() {
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(1024, arena);
      WritableMemory reg = wmem.writableRegion(0, 1024);
      assertTrue(wmem.isSameResource(reg));
    }
  }

  @Test
  public void checkNativeOverlap() {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment par = arena.allocate(100);
      //Equal sizes
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par, 40, 60)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par, 20, 40)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par,  0, 20)),  20);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par, 10, 30)),  10);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 10, 30), getSeg(par,  0, 20)), -10);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 20, 40), getSeg(par,  0, 20)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0,  0), getSeg(par,  0,  0)),   0);
      //Unequal Sizes A > B
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 40), getSeg(par, 60, 80)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 40), getSeg(par, 40, 60)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 40), getSeg(par, 30, 50)),  10);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 40), getSeg(par, 20, 40)),  20);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 40), getSeg(par, 10, 30)),  20);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 40), getSeg(par,  0, 20)),  20);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 10, 50), getSeg(par,  0, 20)), -10);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 20, 60), getSeg(par,  0, 20)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 40, 80), getSeg(par,  0, 20)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 40, 80), getSeg(par,  0,  0)),   0);

      //Unequal Sizes B > A
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 60, 80), getSeg(par,  0, 40)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 40, 60), getSeg(par,  0, 40)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 30, 50), getSeg(par,  0, 40)), -10);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 20, 40), getSeg(par,  0, 40)), -20);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par, 10, 30), getSeg(par,  0, 40)), -20);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par,  0, 40)),  20);
      assertEquals(ResourceImpl.nativeOverlap( getSeg(par, 0, 20), getSeg(par, 10, 50)),  10);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par, 20, 60)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0, 20), getSeg(par, 40, 80)),   0);
      assertEquals(ResourceImpl.nativeOverlap(getSeg(par,  0,  0), getSeg(par, 40, 80)),   0);
    }
  }

  private static MemorySegment getSeg(MemorySegment parent, long left, long right) {
    return parent.asSlice(left, right - left);
  }

  @Test
  public void checkNotEqualTo() {
    byte[] arr1 = new byte[8];
    Memory mem = Memory.wrap(arr1);
    byte[] arr2 = new byte[8];
    arr2[7] = 1;
    Memory mem2 = Memory.wrap(arr2);
    assertFalse(mem.equalTo(0, mem2, 0, 8));
  }

  @Test
  public void checkParseJavaVersion() {
    try {
      ResourceImpl.parseJavaVersion("15_1");
      fail();
    } catch (IllegalArgumentException e) { }
    try {
      ResourceImpl.parseJavaVersion("20");
      fail();
    } catch (IllegalArgumentException e) { }
    ResourceImpl.parseJavaVersion("21");
    ResourceImpl.parseJavaVersion("22");
  }

  @Test
  public void checkToHexString() {
    WritableMemory mem = WritableMemory.writableWrap(new byte[16]);
    println(mem.toString("baseMem", 0, 16, true));
    for (int i = 0; i < 16; i++) { mem.putByte(i, (byte)i); }
    Buffer buf = mem.asBuffer();
    println(buf.toString("buffer", 0, 16, true));
  }

  @Test
  public void checkToMemorySegment() {
    {
      int len = 0;
      WritableMemory mem = WritableMemory.allocate(len);
      MemorySegment seg = mem.toMemorySegment(null, 8);
      assertEquals(seg.byteSize(), len);
    }
    {
      int len = 13 * 8;
      WritableMemory mem = WritableMemory.allocate(len);
      MemorySegment seg = mem.toMemorySegment(null, 8);
      assertEquals(seg.byteSize(), len);
    }
    {
      int len = 13 * 4;
      WritableMemory mem = WritableMemory.allocate(len);
      MemorySegment seg = mem.toMemorySegment(null, 8);
      assertEquals(seg.byteSize(), len);
    }
    {
      int len = 13 * 2;
      WritableMemory mem = WritableMemory.allocate(len);
      MemorySegment seg = mem.toMemorySegment(null, 8);
      assertEquals(seg.byteSize(), len);
    }
    {
      int len = 13;
      WritableMemory mem = WritableMemory.allocate(len);
      MemorySegment seg = mem.toMemorySegment(null, 8);
      assertEquals(seg.byteSize(), len);
    }
  }

  @Test
  public void checkTypeDecode() {
    for (int i = 0; i < 256; i++) {
      String str = ResourceImpl.typeDecode(i);
      println(i + "\t" + str);
    }
  }

  @Test
  public void checkXxHash64() {
    WritableMemory mem = WritableMemory.allocate(8);
    long out = mem.xxHash64(mem.getLong(0), 1L);
    assertTrue(out != 0);
  }

  @Test
  public void checkMismatch() {
    byte[] arr1 = {1,2,3,4};
    byte[] arr2 = {1,2,3,4};
    byte[] arr3 = {1,2,3,4,5};
    Memory mem1 = Memory.wrap(arr1);
    Memory mem2 = Memory.wrap(arr2);
    Memory mem3 = Memory.wrap(arr3);
    assertEquals(mem1.mismatch(mem2), -1);
    assertEquals(mem1.mismatch(mem3), 4);

    byte[] arr4 = {9,9,1,2,3,4,9,9};
    byte[] arr5 = {8,8,8,1,2,3,4,8};
    byte[] arr6 = {8,8,8,1,2,3,4,5};
    Memory mem4 = Memory.wrap(arr4);
    Memory mem5 = Memory.wrap(arr5);
    Memory mem6 = Memory.wrap(arr6);
    assertEquals(mem4.mismatch(mem4, 2, 6, mem5, 3, 7), -1);
    assertEquals(mem4.mismatch(mem4, 2, 7, mem6, 3, 8), 4);
  }

  /********************/
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
