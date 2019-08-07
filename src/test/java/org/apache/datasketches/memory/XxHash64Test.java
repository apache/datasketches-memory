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

package org.apache.datasketches.memory;

import static org.apache.datasketches.memory.XxHash64.hashBooleans;
import static org.apache.datasketches.memory.XxHash64.hashBytes;
import static org.apache.datasketches.memory.XxHash64.hashChars;
import static org.apache.datasketches.memory.XxHash64.hashDoubles;
import static org.apache.datasketches.memory.XxHash64.hashFloats;
import static org.apache.datasketches.memory.XxHash64.hashInts;
import static org.apache.datasketches.memory.XxHash64.hashLongs;
import static org.apache.datasketches.memory.XxHash64.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.testng.annotations.Test;

import net.openhft.hashing.LongHashFunction;

/**
 * @author Lee Rhodes
 */
public class XxHash64Test {

  @Test
  public void offsetChecks() {
    long seed = 12345;
    int blocks = 6;
    int cap = blocks * 16;

    long hash;

    WritableMemory wmem = WritableMemory.allocate(cap);
    for (int i = 0; i < cap; i++) { wmem.putByte(i, (byte)(-128 + i)); }

    for (int offset = 0; offset < 16; offset++) {
      int arrLen = cap - offset;
      hash = wmem.xxHash64(offset, arrLen, seed);
      assertTrue(hash != 0);
    }
  }

  @Test
  public void byteArrChecks() {
    long seed = 0;
    int offset = 0;
    int bytes = 16;

    for (int j = 1; j < bytes; j++) {
      byte[] in = new byte[bytes];

      WritableMemory wmem = WritableMemory.wrap(in);
      for (int i = 0; i < j; i++) { wmem.putByte(i, (byte) (-128 + i)); }

      long hash =wmem.xxHash64(offset, bytes, seed);
      assertTrue(hash != 0);
    }
  }

  /*
   * This test is adapted from
   * <a href="https://github.com/OpenHFT/Zero-Allocation-Hashing/blob/master/src/test/java/net/openhft/hashing/XxHashCollisionTest.java">
   * OpenHFT/Zero-Allocation-Hashing</a> to test hash compatibility with that implementation.
   * It is licensed under Apache License, version 2.0. See LICENSE.
   */
  @Test
  public void collisionTest() {
    WritableMemory wmem = WritableMemory.allocate(128);
    wmem.putLong(0, 1);
    wmem.putLong(16, 42);
    wmem.putLong(32, 2);
    long h1 = wmem.xxHash64(0, wmem.getCapacity(), 0);

    wmem.putLong(0, 1L + 0xBA79078168D4BAFL);
    wmem.putLong(32, 2L + 0x9C90005B80000000L);
    long h2 = wmem.xxHash64(0, wmem.getCapacity(), 0);
    assertEquals(h1, h2);

    wmem.putLong(0, 1L + (0xBA79078168D4BAFL * 2));
    wmem.putLong(32, 2L + (0x392000b700000000L)); //= (0x9C90005B80000000L * 2) fix overflow false pos

    long h3 = wmem.xxHash64(0, wmem.getCapacity(), 0);
    assertEquals(h2, h3);
  }

  /**
   * This simple test compares the output of {@link BaseState#xxHash64(long, long, long)} with the
   * output of {@link net.openhft.hashing.LongHashFunction}, that itself is tested against the
   * reference implementation in C.  This increase confidence that the xxHash function implemented
   * in this package is in fact the same xxHash function implemented in C.
   *
   * @author Roman Leventov
   * @author Lee Rhodes
   */
  @Test
  public void testXxHash() {
    Random random = ThreadLocalRandom.current();
    for (int len = 0; len < 100; len++) {
      byte[] bytes = new byte[len];
      for (int i = 0; i < 10; i++) {
        long zahXxHash = LongHashFunction.xx().hashBytes(bytes);
        long memoryXxHash = Memory.wrap(bytes).xxHash64(0, len, 0);
        assertEquals(memoryXxHash, zahXxHash);
        random.nextBytes(bytes);
      }
    }
  }

  private static final byte[] barr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

  @Test
  public void testArrHashes() {
    WritableMemory wmem = WritableMemory.wrap(barr);
    long hash0 = wmem.xxHash64(8, 8, 0);
    long hash1 = hashBytes(barr, 8, 8, 0);
    assertEquals(hash1, hash0);

    char[] carr = new char[8];
    wmem.getCharArray(0, carr, 0, 8);
    hash1 = hashChars(carr, 4, 4, 0);
    assertEquals(hash1, hash0);

    short[] sarr = new short[8];
    wmem.getShortArray(0, sarr, 0, 8);
    hash1 = hashShorts(sarr, 4, 4, 0);
    assertEquals(hash1, hash0);

    int[] iarr = new int[4];
    wmem.getIntArray(0, iarr, 0, 4);
    hash1 = hashInts(iarr, 2, 2, 0);
    assertEquals(hash1, hash0);

    float[] farr = new float[4];
    wmem.getFloatArray(0, farr, 0, 4);
    hash1 = hashFloats(farr, 2, 2, 0);
    assertEquals(hash1, hash0);

    long[] larr = new long[2];
    wmem.getLongArray(0, larr, 0, 2);
    hash1 = hashLongs(larr, 1, 1, 0);
    assertEquals(hash1, hash0);

    double[] darr = new double[2];
    wmem.getDoubleArray(0, darr, 0, 2);
    hash1 = hashDoubles(darr, 1, 1, 0);
    assertEquals(hash1, hash0);

    boolean[] blarr = new boolean[16];
    wmem.getBooleanArray(0, blarr, 0, 16); //any byte != 0 is true
    hash1 = hashBooleans(blarr, 8, 8, 0);
    assertEquals(hash1, hash0);
  }

  @Test
  public void testString() {
    String s = "Now is the time for all good men to come to the aid of their country.";
    char[] arr = s.toCharArray();
    long hash0 = hashString(s, 0, s.length(), 0);
    long hash1 = hashChars(arr, 0, arr.length, 0);
    assertEquals(hash1, hash0);
  }

}
