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

import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.foreign.Arena;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class AllocateDirectMemoryTest {
  //private static final MemoryRequestServer memReqSvr = Resource.defaultMemReqSvr;

  @SuppressWarnings("resource")
  @Test
  public void simpleAllocateDirect() {
    int longs = 32;
    WritableMemory wMem2 = null;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wMem = WritableMemory.allocateDirect(longs << 3, arena);
      wMem2 = wMem;
      for (int i = 0; i<longs; i++) {
        wMem.putLong(i << 3, i);
        assertEquals(wMem.getLong(i << 3), i);
      }
      //inside the TWR block the memory scope will be alive
      assertTrue(wMem.isAlive());
    }
    //The TWR block has exited, so the memory should be not alive
    if (wMem2 != null) { assertFalse(wMem2.isAlive()); }
  }

  @Test
  public void checkDefaultMemoryRequestServer() {
    int longs1 = 32;
    int bytes1 = longs1 << 3;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory origWmem = WritableMemory.allocateDirect(bytes1, 8, ByteOrder.LITTLE_ENDIAN, Resource.defaultMemReqSvr, arena);
      for (int i = 0; i < longs1; i++) { //puts data in origWmem
        origWmem.putLong(i << 3, i);
        assertEquals(origWmem.getLong(i << 3), i);
      }
      println(origWmem.toString("Test", 0, 32 * 8, true));

      int longs2 = 64;
      int bytes2 = longs2 << 3;
      origWmem.setMemoryRequestServer(Resource.defaultMemReqSvr);
      MemoryRequestServer myMemReqSvr = origWmem.getMemoryRequestServer();

      WritableMemory newWmem = myMemReqSvr.request(bytes2, 8, ByteOrder.LITTLE_ENDIAN, null); //null -> on-heap
      assertTrue(newWmem.isHeap());
      for (int i = 0; i < longs2; i++) {
        newWmem.putLong(i << 3, i);
        assertEquals(newWmem.getLong(i << 3), i);
      }
    } //allow the TWR to close all resources
  }

  @Test
  public void checkNonNativeDirect() {
    MemoryRequestServer myMemReqSvr = Resource.defaultMemReqSvr;
    try (Arena arena = Arena.ofConfined()) {
      WritableMemory wmem = WritableMemory.allocateDirect(128, 8, NON_NATIVE_BYTE_ORDER, myMemReqSvr, arena);
      wmem.putChar(0, (char) 1);
      assertEquals(wmem.getByte(1), (byte) 1);
    }
  }

  @Test
  public void checkExplicitCloseNoTWR() {
    final long cap = 128;
    Arena arena = Arena.ofConfined();
    WritableMemory wmem = WritableMemory.allocateDirect(cap, arena);
    arena.close(); //explicit close
    assertFalse(wmem.isAlive());
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
