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
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

public class MemoryWriteToTest {
  private static final MemoryRequestServer memReqSvr = BaseState.defaultMemReqSvr;

  @Test
  public void testOnHeapBytes() throws IOException {
    testWriteTo(createRandomBytesMemory(0));
    testWriteTo(createRandomBytesMemory(7));
    testWriteTo(createRandomBytesMemory(1023));
    testWriteTo(createRandomBytesMemory(10_000));
    testWriteTo(createRandomBytesMemory((1 << 20) * 5));
    testWriteTo(createRandomBytesMemory(((1 << 20) * 5) + 10));
  }

  @Test
  public void testOnHeapInts() throws IOException {
    testWriteTo(createRandomIntsMemory(0));
    testWriteTo(createRandomIntsMemory(7));
    testWriteTo(createRandomIntsMemory(1023));
    testWriteTo(createRandomIntsMemory(10_000));
    testWriteTo(createRandomIntsMemory((1 << 20) * 5));
    testWriteTo(createRandomIntsMemory(((1 << 20) * 5) + 10));
  }

  @Test
  public void testOffHeap() throws Exception {
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory mem = WritableMemory.allocateDirect(((1 << 20) * 5) + 10, scope, memReqSvr);
      testWriteTo(mem.region(0, 0));
      testOffHeap(mem, 7);
      testOffHeap(mem, 1023);
      testOffHeap(mem, 10_000);
      testOffHeap(mem, (1 << 20) * 5);
      testOffHeap(mem, ((1 << 20) * 5) + 10);
    }
  }

  private static void testOffHeap(WritableMemory mem, int size) throws IOException {
    createRandomBytesMemory(size).copyTo(0, mem, 0, size);
    testWriteTo(mem.region(0, size));
  }

  private static Memory createRandomBytesMemory(int size) {
    byte[] bytes = new byte[size];
    ThreadLocalRandom.current().nextBytes(bytes);
    return Memory.wrap(bytes);
  }

  private static Memory createRandomIntsMemory(int size) {
    int[] ints = ThreadLocalRandom.current().ints(size).toArray();
    return Memory.wrap(ints);
  }

  private static void testWriteTo(Memory mem) throws IOException {
    int cap = (int)mem.getCapacity();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(cap);
    mem.writeToByteStream(0, cap, baos);
    byte[] result = baos.toByteArray();
    assertTrue(mem.equalTo(0, Memory.wrap(result), 0, cap));
    //OR
    byte[] barr = new byte[cap];
    mem.getByteArray(0, barr, 0, cap);
    assertEquals(barr, result);
  }

}
