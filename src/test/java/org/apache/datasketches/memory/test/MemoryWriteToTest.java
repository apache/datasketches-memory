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

import static org.apache.datasketches.memory.internal.Util.UNSAFE_COPY_THRESHOLD_BYTES;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.internal.MemoryImpl;
import org.apache.datasketches.memory.internal.WritableMemoryImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class MemoryWriteToTest {

  @Test
  public void testOnHeap() throws IOException {
    testWriteTo(createRandomBytesMemory(0));
    testWriteTo(createRandomBytesMemory(7));
    testWriteTo(createRandomBytesMemory(1023));
    testWriteTo(createRandomBytesMemory(10_000));
    testWriteTo(createRandomBytesMemory(UNSAFE_COPY_THRESHOLD_BYTES * 5));
    testWriteTo(createRandomBytesMemory((UNSAFE_COPY_THRESHOLD_BYTES * 5) + 10));
  }

  @Test
  public void testOnHeapInts() throws IOException {
    testWriteTo(createRandomIntsMemory(0));
    testWriteTo(createRandomIntsMemory(7));
    testWriteTo(createRandomIntsMemory(1023));
    testWriteTo(createRandomIntsMemory(10_000));
    testWriteTo(createRandomIntsMemory(UNSAFE_COPY_THRESHOLD_BYTES * 5));
    testWriteTo(createRandomIntsMemory((UNSAFE_COPY_THRESHOLD_BYTES * 5) + 10));
  }

  @Test
  public void testOffHeap() throws IOException {
    try (WritableHandle handle =
        WritableMemoryImpl.allocateDirect((UNSAFE_COPY_THRESHOLD_BYTES * 5) + 10)) {
      WritableMemoryImpl mem = handle.get();
      testWriteTo(mem.region(0, 0));
      testOffHeap(mem, 7);
      testOffHeap(mem, 1023);
      testOffHeap(mem, 10_000);
      testOffHeap(mem, UNSAFE_COPY_THRESHOLD_BYTES * 5);
      testOffHeap(mem, (UNSAFE_COPY_THRESHOLD_BYTES * 5) + 10);
    }
  }

  private static void testOffHeap(WritableMemoryImpl mem, int size) throws IOException {
    createRandomBytesMemory(size).copyTo(0, mem, 0, size);
    testWriteTo(mem.region(0, size));
  }

  private static MemoryImpl createRandomBytesMemory(int size) {
    byte[] bytes = new byte[size];
    ThreadLocalRandom.current().nextBytes(bytes);
    return MemoryImpl.wrap(bytes);
  }

  private static MemoryImpl createRandomIntsMemory(int size) {
    int[] ints = ThreadLocalRandom.current().ints(size).toArray();
    return MemoryImpl.wrap(ints);
  }

  private static void testWriteTo(MemoryImpl mem) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (WritableByteChannel out = Channels.newChannel(baos)) {
      mem.writeTo(0, mem.getCapacity(), out);
    }
    byte[] result = baos.toByteArray();
    Assert.assertTrue(mem.equals(MemoryImpl.wrap(result)));
  }
}
