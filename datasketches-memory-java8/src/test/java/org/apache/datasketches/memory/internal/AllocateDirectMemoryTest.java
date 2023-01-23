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
import static org.testng.Assert.fail;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.DefaultMemoryRequestServer;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class AllocateDirectMemoryTest {

  @Test
  public void simpleAllocateDirect() throws Exception {
    int longs = 32;
    WritableMemory wMem;
    try (WritableHandle wh = WritableMemory.allocateDirect(longs << 3)) {
      wMem = wh.getWritable();
      for (int i = 0; i < longs; i++) {
        wMem.putLong(i << 3, i);
        assertEquals(wMem.getLong(i << 3), i);
      }
      //inside the TWR block the memory should be valid
      ((BaseStateImpl)wMem).checkValid();
      //OK
    }
    //The TWR block has exited, so the memory should be invalid
    try {
      ((BaseStateImpl)wMem).checkValid();
      fail();
    } catch (final RuntimeException e) {
      //OK
    }
  }

  @Test
  public void checkDefaultMemoryRequestServer() throws Exception {
    int longs1 = 32;
    int bytes1 = longs1 << 3;
    try (WritableHandle wh = WritableMemory.allocateDirect(bytes1)) {
      WritableMemory origWmem = wh.getWritable();
      for (int i = 0; i < longs1; i++) { //puts data in wMem1
        origWmem.putLong(i << 3, i);
        assertEquals(origWmem.getLong(i << 3), i);
      }
      println(origWmem.toHexString("Test", 0, 32 * 8));

      int longs2 = 64;
      int bytes2 = longs2 << 3;
      MemoryRequestServer memReqSvr;
      if (BaseState.defaultMemReqSvr == null) {
        memReqSvr = new DefaultMemoryRequestServer();
      } else {
        memReqSvr = origWmem.getMemoryRequestServer();
      }
      WritableMemory newWmem = memReqSvr.request(origWmem, bytes2);
      assertFalse(newWmem.isDirect()); //on heap by default
      for (int i = 0; i < longs2; i++) {
          newWmem.putLong(i << 3, i);
          assertEquals(newWmem.getLong(i << 3), i);
      }
      memReqSvr.requestClose(origWmem, newWmem);
      //The default MRS doesn't actually release because it could be easily misused.
      // So we let the TWR release it.
    }
  }

  @Test
  public void checkNonNativeDirect() throws Exception {
    try (WritableHandle h = WritableMemory.allocateDirect(128, Util.NON_NATIVE_BYTE_ORDER, null)) {
      WritableMemory wmem = h.getWritable();
      wmem.putChar(0, (char) 1);
      assertEquals(wmem.getByte(1), (byte) 1);
    }
  }

  @Test
  public void checkExplicitClose() throws Exception {
    final long cap = 128;
    try (WritableHandle wdh = WritableMemory.allocateDirect(cap)) {
      wdh.close(); //explicit close. Does the work of closing
    } //end of scope call to Cleaner/Deallocator also will be redundant
  }

  @AfterClass
  public void checkDirectCounter() {
    WritableMemory.writableWrap(new byte[8]);
    long count = BaseState.getCurrentDirectMemoryAllocations();
    if (count != 0) {
      println("" + count);
      fail();
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
