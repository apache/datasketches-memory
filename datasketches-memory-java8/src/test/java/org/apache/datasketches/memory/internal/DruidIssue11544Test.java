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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.DefaultMemoryRequestServer;
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
 * <p>This issue was first identified in Druid Issue #11544 and then posted as DataSketches-java Issue #358.
 * But the actual source of the problem was in Memory.</p>
 *
 * <p>This test mimics the Druid issue but at a much smaller scale.</p>
 *
 * @author Lee Rhodes
 *
 */
public class DruidIssue11544Test {

  @Test
  public void withByteBuffer() {
    int initialLongs = 1000;
    int size1 = initialLongs * 8;

    //Start with a ByteBuffer
    ByteBuffer bb = ByteBuffer.allocateDirect(size1);
    bb.order(ByteOrder.nativeOrder());

    //Wrap bb into WritableMemory
    WritableMemory mem1 = WritableMemory.writableWrap(bb);
    assertTrue(mem1.isDirect()); //confirm mem1 is off-heap

    //Acquire the DefaultMemoryRequestServer
    //NOTE: it is a policy decision to allow the DefaultMemoryServer to be set as a default.
    // It might be set to null. So we need to check what the current policy is.
    MemoryRequestServer svr = mem1.getMemoryRequestServer();
    if (svr == null) {
      svr = new DefaultMemoryRequestServer();
    }
    assertNotNull(svr);

    //Request Bigger Memory
    int size2 = size1 * 2;
    WritableMemory mem2 = svr.request(mem1, size2);

    //Confirm that mem2 is on the heap (the default) and 2X size1
    assertFalse(mem2.isDirect());
    assertEquals(mem2.getCapacity(), size2);

    //Move data to new memory
    mem1.copyTo(0, mem2, 0, size1);

    //Prepare to request deallocation
    //In the DefaultMemoryRequestServer, this is a no-op, so nothing is actually deallocated.
    svr.requestClose(mem1, mem2);
    assertTrue(mem1.isValid());
    assertTrue(mem2.isValid());

    //Now we are on the heap and need to grow again:
    int size3 = size2 * 2;
    WritableMemory mem3 = svr.request(mem2, size3);

    //Confirm that mem3 is still on the heap and 2X of size2
    assertFalse(mem3.isDirect());
    assertEquals(mem3.getCapacity(), size3);

    //Move data to new memory
    mem2.copyTo(0, mem3, 0, size2);

    //Prepare to request deallocation

    svr.requestClose(mem2, mem3); //No-op
    assertTrue(mem2.isValid());
    assertTrue(mem3.isValid());
  }

}
