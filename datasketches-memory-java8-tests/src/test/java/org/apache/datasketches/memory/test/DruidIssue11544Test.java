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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * The original design provided the MemoryRequestServer callback only for Memory segments allocated via
 * <i>WritableMemory.allocateDirect(...)</i> calls.  Memory segments allocated via
 * <i>WritableMemory.wrap(ByteBuffer)</i> did not have this capability.  This was a major oversight since
 * all off-heap memory in Druid is allocated using ByteBuffers!  It is unusual that no one has
 * uncovered this until August 2021.  Nonetheless, the fix involves instrumenting all the paths involved
 * in providing this callback mechanism for wrapped ByteBuffers.
 *
 * This issues was first identified in Druid Issue #11544 and then posted as DataSketches-java Issue #358.
 * But the actual source of the problem was in Memory.
 *
 * This test mimics the Druid issue but at a much smaller scale.
 *
 * @author Lee Rhodes
 *
 */
public class DruidIssue11544Test {

  @Test
  public void withByteBuffer() {
    int initialLongs = 1000;
    int initialMemSize = initialLongs * 8;
    ByteBuffer bb = ByteBuffer.allocateDirect(initialMemSize);
    bb.order(ByteOrder.nativeOrder());

    //Fill the byte buffer
    for (int i = 0; i < initialLongs; i++) { bb.putLong(i * 8, i); }

    //Wrap, assuming default MemoryRequestServer
    WritableMemory mem = WritableMemory.writableWrap(bb);
    assertTrue(mem.isDirect()); //confirm mem is off-heap

    //Request Bigger Memory
    MemoryRequestServer svr = mem.getMemoryRequestServer();
    assertNotNull(svr); //before the fix, this was null.

    WritableMemory newMem = svr.request(initialMemSize * 2);

    //Confirm that newMem is on the heap (the default) and 2X size
    assertFalse(newMem.isDirect());
    assertEquals(newMem.getCapacity(), 2 * initialMemSize);

    //Move data to new memory
    mem.copyTo(0, newMem, 0, initialMemSize);

    //Prepare to request deallocation
    WritableMemory oldMem = mem;
    mem = newMem;

    //In the DefaultMemoryRequestServer, this is a no-op, so nothing is actually deallocated.
    svr.requestClose(oldMem, newMem);
    assertTrue(oldMem.isValid());
    assertTrue(mem.isValid());
  }

}
