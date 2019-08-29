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

package org.apache.datasketches.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("javadoc")
public class WritableDirectCopyTest {

//Copy Within tests

  @Test
  public void checkCopyWithinNativeSmall() {
    int memCapacity = 64;
    int half = memCapacity / 2;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.clear();

      for (int i = 0; i < half; i++) { //fill first half
        mem.putByte(i, (byte) i);
      }

      mem.copyTo(0, mem, half, half);

      for (int i = 0; i < half; i++) {
        assertEquals(mem.getByte(i + half), (byte) i);
      }
    }
  }

  @Test
  public void checkCopyWithinNativeLarge() {
    int memCapacity = (2 << 20) + 64;
    int memCapLongs = memCapacity / 8;
    int halfBytes = memCapacity / 2;
    int halfLongs = memCapLongs / 2;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.clear();

      for (int i = 0; i < halfLongs; i++) {
        mem.putLong(i * 8, i);
      }

      mem.copyTo(0, mem, halfBytes, halfBytes);

      for (int i = 0; i < halfLongs; i++) {
        assertEquals(mem.getLong((i + halfLongs) * 8), i);
      }
    }
  }

  @Test
  public void checkCopyWithinNativeOverlap() {
    int memCapacity = 64;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.clear();
      //println(mem.toHexString("Clear 64", 0, memCapacity));

      for (int i = 0; i < (memCapacity / 2); i++) {
        mem.putByte(i, (byte) i);
      }
      //println(mem.toHexString("Set 1st 32 to ints ", 0, memCapacity));
      mem.copyTo(0, mem, memCapacity / 4, memCapacity / 2);  //overlap is OK
    }
  }

  @Test
  public void checkCopyWithinNativeSrcBound() {
    int memCapacity = 64;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.copyTo(32, mem, 32, 33);  //hit source bound check
      fail("Did Not Catch Assertion Error: source bound");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void checkCopyWithinNativeDstBound() {
    int memCapacity = 64;
    try (WritableHandle wrh = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem = wrh.get();
      mem.copyTo(0, mem, 32, 33);  //hit dst bound check
      fail("Did Not Catch Assertion Error: dst bound");
    } catch (IllegalArgumentException e) {
      //pass
    }
  }

  @Test
  public void checkCopyCrossNativeSmall() {
    int memCapacity = 64;

    try (WritableHandle wrh1 = WritableMemory.allocateDirect(memCapacity);
         WritableHandle wrh2 = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem1 = wrh1.get();
      WritableMemory mem2 = wrh2.get();

      for (int i = 0; i < memCapacity; i++) {
        mem1.putByte(i, (byte) i);
      }
      mem2.clear();
      mem1.copyTo(0, mem2, 0, memCapacity);

      for (int i = 0; i < memCapacity; i++) {
        assertEquals(mem2.getByte(i), (byte) i);
      }
      wrh1.close();
      wrh2.close();
    }
  }

  @Test
  public void checkCopyCrossNativeLarge() {
    int memCapacity = (2 << 20) + 64;
    int memCapLongs = memCapacity / 8;

    try (WritableHandle wrh1 = WritableMemory.allocateDirect(memCapacity);
         WritableHandle wrh2 = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem1 = wrh1.get();
      WritableMemory mem2 = wrh2.get();

      for (int i = 0; i < memCapLongs; i++) {
        mem1.putLong(i * 8, i);
      }
      mem2.clear();

      mem1.copyTo(0, mem2, 0, memCapacity);

      for (int i = 0; i < memCapLongs; i++) {
        assertEquals(mem2.getLong(i * 8), i);
      }
    }
  }

  @Test
  public void checkCopyCrossNativeAndByteArray() {
    int memCapacity = 64;
    try (WritableHandle wrh1 = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem1 = wrh1.get();

      for (int i = 0; i < mem1.getCapacity(); i++) {
        mem1.putByte(i, (byte) i);
      }

      WritableMemory mem2 = WritableMemory.allocate(memCapacity);
      mem1.copyTo(8, mem2, 16, 16);

      for (int i = 0; i < 16; i++) {
        assertEquals(mem1.getByte(8 + i), mem2.getByte(16 + i));
      }
      //println(mem2.toHexString("Mem2", 0, (int)mem2.getCapacity()));
    }
  }

  @Test
  public void checkCopyCrossRegionsSameNative() {
    int memCapacity = 128;

    try (WritableHandle wrh1 = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem1 = wrh1.get();

      for (int i = 0; i < mem1.getCapacity(); i++) {
        mem1.putByte(i, (byte) i);
      }
      //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));

      Memory reg1 = mem1.region(8, 16);
      //println(reg1.toHexString("Reg1", 0, (int)reg1.getCapacity()));

      WritableMemory reg2 = mem1.writableRegion(24, 16);
      //println(reg2.toHexString("Reg2", 0, (int)reg2.getCapacity()));
      reg1.copyTo(0, reg2, 0, 16);

      for (int i = 0; i < 16; i++) {
        assertEquals(reg1.getByte(i), reg2.getByte(i));
        assertEquals(mem1.getByte(8 + i), mem1.getByte(24 + i));
      }
      //println(mem1.toHexString("Mem1", 0, (int)mem1.getCapacity()));
    }
  }

  @Test
  public void checkCopyCrossNativeArrayAndHierarchicalRegions() {
    int memCapacity = 64;
    try (WritableHandle wrh1 = WritableMemory.allocateDirect(memCapacity)) {
      WritableMemory mem1 = wrh1.get();

      for (int i = 0; i < mem1.getCapacity(); i++) { //fill with numbers
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
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

}
