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

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

public class AllocateDirectMemoryTest {
  private static final MemoryRequestServer memReqSvr = BaseState.defaultMemReqSvr;

  @SuppressWarnings("resource")
  @Test
  public void simpleAllocateDirect() {
    int longs = 32;
    WritableMemory wMem = null;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      wMem = WritableMemory.allocateDirect(longs << 3, scope, memReqSvr);
      for (int i = 0; i<longs; i++) {
        wMem.putLong(i << 3, i);
        assertEquals(wMem.getLong(i << 3), i);
      }
      //inside the TWR block the memory scope will be alive
      assertTrue(wMem.isAlive());
    }
    //The TWR block has exited, so the memory should be invalid
    assertFalse(wMem.isAlive());
  }

  @Test
  public void checkDefaultMemoryRequestServer() {
    int longs1 = 32;
    int bytes1 = longs1 << 3;
    WritableMemory wmem = null;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      wmem = WritableMemory.allocateDirect(bytes1, scope, memReqSvr);

      for (int i = 0; i < longs1; i++) { //puts data in origWmem
        wmem.putLong(i << 3, i);
        assertEquals(wmem.getLong(i << 3), i);
      }
      println(wmem.toHexString("Test", 0, 32 * 8, true));

      int longs2 = 64;
      int bytes2 = longs2 << 3;
      WritableMemory newWmem = memReqSvr.request(wmem, bytes2); //on the heap
      assertFalse(newWmem.isDirect()); //on heap by default
      for (int i = 0; i < longs2; i++) {
          newWmem.putLong(i << 3, i);
          assertEquals(newWmem.getLong(i << 3), i);
      }
      memReqSvr.requestClose(wmem, newWmem); //The default MRS doesn't close.
    } // So we let the TWR close it here
  }

  @SuppressWarnings("resource")
  @Test
  public void checkNonNativeDirect() {
    WritableMemory wmem = null;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      wmem = WritableMemory.allocateDirect( 128, 8, scope,
          BaseState.NON_NATIVE_BYTE_ORDER,
          memReqSvr);
      wmem.putChar(0, (char) 1);
      assertEquals(wmem.getByte(1), (byte) 1);
    }
  }

  @SuppressWarnings("resource")
  @Test
  public void checkExplicitCloseNoTWR() {
    final long cap = 128;
    WritableMemory wmem = null;
    ResourceScope scope = ResourceScope.newConfinedScope();
    wmem = WritableMemory.allocateDirect(cap, scope, memReqSvr);
    wmem.close(); //explicit close
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
