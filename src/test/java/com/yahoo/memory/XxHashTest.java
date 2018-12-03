/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.openhft.hashing.LongHashFunction;

/**
 * This test compares the output of {@link BaseState#longHashCode(long, long)} with the output of
 * {@link net.openhft.hashing.LongHashFunction}, that itself is tested against the reference
 * implementation in C. The point of verifying that longHashCode() is exactly xxHash is not because
 * longHashCode() is specified to be xxHash (it's not), but in order to be sure that longHashCode()
 * has good avalanche and mixing properties (xxHash is tested using SMHasher,
 * https://github.com/aappleby/smhasher)
 */
public class XxHashTest {

  @Test
  public void testXxHash() {
    Random random = ThreadLocalRandom.current();
    for (int len = 0; len < 100; len++) {
      byte[] bytes = new byte[len];
      for (int i = 0; i < 10; i++) {
        long zahXxHash = LongHashFunction.xx().hashBytes(bytes);
        long memoryXxHash = Memory.wrap(bytes).longHashCode(0, len);
        Assert.assertEquals(memoryXxHash, zahXxHash);
        random.nextBytes(bytes);
      }
    }
  }
}
