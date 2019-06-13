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

package com.yahoo.memory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore("Test causes OutOfMemoryError in Travis CI, run only locally")
public class IgnoredArrayOverflowTest {

  private WritableHandle h;
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
