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

import java.nio.ByteBuffer;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.Assert;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

/**
 * @author Lee Rhodes
 */
public class ZeroCapacityTest {
  private static final MemoryRequestServer memReqSvr = BaseState.defaultMemReqSvr;

  @SuppressWarnings("resource")
  @Test
  public void checkZeroCapacity() throws Exception {
    WritableMemory wmem = WritableMemory.allocate(0);
    assertEquals(wmem.getCapacity(), 0);

    Memory.wrap(new byte[0]);
    Memory.wrap(ByteBuffer.allocate(0));
    Memory mem3 = Memory.wrap(ByteBuffer.allocateDirect(0));
    mem3.region(0, 0);
    WritableMemory nullMem = null;
    ResourceScope scope = ResourceScope.newConfinedScope();
    try { //Invalid allocation size : 0
      nullMem = WritableMemory.allocateDirect(0, scope, memReqSvr);
      Assert.fail();
    } catch (IllegalArgumentException ignore) {
      if (nullMem != null) {
        nullMem.close();
      }
      // expected
    }
  }

  @Test
  public void printlnTest() {
    //println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

}
