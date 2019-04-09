/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.XxHash64.hashBooleans;
import static com.yahoo.memory.XxHash64.hashBytes;
import static com.yahoo.memory.XxHash64.hashChars;
import static com.yahoo.memory.XxHash64.hashDoubles;
import static com.yahoo.memory.XxHash64.hashFloats;
import static com.yahoo.memory.XxHash64.hashInts;
import static com.yahoo.memory.XxHash64.hashLongs;
import static com.yahoo.memory.XxHash64.hashShorts;
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
   * This test is modeled from
   * <a href="https://github.com/OpenHFT/Zero-Allocation-Hashing/blob/master/src/test/java/net/openhft/hashing/XxHashTest.java">
   * OpenHFT/Zero-Allocation-Hashing</a> to test hash compatibility with that implementation.
   */
  @Test
  @SuppressWarnings("ConstantOverflow")
  public void collisionTest() {
    WritableMemory wmem = WritableMemory.allocate(128);
    wmem.putLong(0, 1);
    wmem.putLong(16, 42);
    wmem.putLong(32, 2);
    long h1 = wmem.xxHash64(0, wmem.getCapacity(), 0);

    wmem.putLong(0, 1 + 0xBA79078168D4BAFL);
    wmem.putLong(32, 2 + 0x9C90005B80000000L);
    long h2 = wmem.xxHash64(0, wmem.getCapacity(), 0);
    assertEquals(h1, h2);

    wmem.putLong(0, 1 + (0xBA79078168D4BAFL * 2));
    wmem.putLong(32, 2 + (0x9C90005B80000000L * 2));

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

}
