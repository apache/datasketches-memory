/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore("Test causes OutOfMemoryError in Travis CI, run only locally")
public class ArrayOverflowTest {

  private WritableDirectHandle h;
  private WritableMemory memory;

  @BeforeClass
  public void allocate() {
    h = WritableMemory.allocateDirect(Integer.MAX_VALUE + 100L);
    memory = h.get();
  }

  @AfterClass
  public void close() {
    h.close();
  }

  @Test
  public void testCharArray() {
    int size = (int) (memory.getCapacity() / 2);
    char[] array = new char[size];
    memory.getCharArray(0, array, 0, size);
    memory.asBuffer().getCharArray(array, 0, size);
    memory.putCharArray(0, array, 0, size);
    memory.asWritableBuffer().putCharArray(array, 0, size);
  }

  @Test
  public void testShortArray() {
    int size = (int) (memory.getCapacity() / 2);
    short[] array = new short[size];
    memory.getShortArray(0, array, 0, size);
    memory.asBuffer().getShortArray(array, 0, size);
    memory.putShortArray(0, array, 0, size);
    memory.asWritableBuffer().putShortArray(array, 0, size);
  }

  @Test
  public void testIntArray() {
    int size = (int) (memory.getCapacity() / 4);
    int[] array = new int[size];
    memory.getIntArray(0, array, 0, size);
    memory.asBuffer().getIntArray(array, 0, size);
    memory.putIntArray(0, array, 0, size);
    memory.asWritableBuffer().putIntArray(array, 0, size);
  }

  @Test
  public void testFloatArray() {
    int size = (int) (memory.getCapacity() / 4);
    float[] array = new float[size];
    memory.getFloatArray(0, array, 0, size);
    memory.asBuffer().getFloatArray(array, 0, size);
    memory.putFloatArray(0, array, 0, size);
    memory.asWritableBuffer().putFloatArray(array, 0, size);
  }

  @Test
  public void testLongArray() {
    int size = (int) (memory.getCapacity() / 8);
    long[] array = new long[size];
    memory.getLongArray(0, array, 0, size);
    memory.asBuffer().getLongArray(array, 0, size);
    memory.putLongArray(0, array, 0, size);
    memory.asWritableBuffer().putLongArray(array, 0, size);
  }

  @Test
  public void testDoubleArray() {
    int size = (int) (memory.getCapacity() / 8);
    double[] array = new double[size];
    memory.getDoubleArray(0, array, 0, size);
    memory.asBuffer().getDoubleArray(array, 0, size);
    memory.putDoubleArray(0, array, 0, size);
    memory.asWritableBuffer().putDoubleArray(array, 0, size);
  }
}
