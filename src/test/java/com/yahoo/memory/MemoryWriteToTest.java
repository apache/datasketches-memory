/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ThreadLocalRandom;

public class MemoryWriteToTest {

  @Test
  public void testOnHeap() throws IOException {
    testWriteTo(createRandomBytesMemory(0));
    testWriteTo(createRandomBytesMemory(7));
    testWriteTo(createRandomBytesMemory(1023));
    testWriteTo(createRandomBytesMemory(10_000));
    testWriteTo(createRandomBytesMemory(CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5));
    testWriteTo(createRandomBytesMemory(CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5 + 10));
  }

  @Test
  public void testOnHeapInts() throws IOException {
    testWriteTo(createRandomIntsMemory(0));
    testWriteTo(createRandomIntsMemory(7));
    testWriteTo(createRandomIntsMemory(1023));
    testWriteTo(createRandomIntsMemory(10_000));
    testWriteTo(createRandomIntsMemory(CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5));
    testWriteTo(createRandomIntsMemory(CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5 + 10));
  }

  @Test
  public void testOffHeap() throws IOException {
    try (WritableDirectHandle handle =
        WritableMemory.allocateDirect(CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5 + 10)) {
      WritableMemory mem = handle.get();
      testWriteTo(mem.region(0, 0));
      testOffHeap(mem, 7);
      testOffHeap(mem, 1023);
      testOffHeap(mem, 10_000);
      testOffHeap(mem, CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5);
      testOffHeap(mem, CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD * 5 + 10);
    }
  }

  private void testOffHeap(WritableMemory mem, int size) throws IOException {
    createRandomBytesMemory(size).copyTo(0, mem, 0, size);
    testWriteTo(mem.region(0, size));
  }

  private Memory createRandomBytesMemory(int size) {
    byte[] bytes = new byte[size];
    ThreadLocalRandom.current().nextBytes(bytes);
    return Memory.wrap(bytes);
  }

  private Memory createRandomIntsMemory(int size) {
    int[] ints = ThreadLocalRandom.current().ints(size).toArray();
    return Memory.wrap(ints);
  }

  private void testWriteTo(Memory mem) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    WritableByteChannel out = Channels.newChannel(baos);
    mem.writeTo(0, mem.getCapacity(), out);
    out.close();
    byte[] result = baos.toByteArray();
    Assert.assertTrue(mem.equalTo(Memory.wrap(result)));
  }
}
