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

import java.lang.foreign.MemorySegment;
import org.apache.datasketches.memory.internal.MurmurHash3v4;


/**
 * <p>The MurmurHash3 is a fast, non-cryptographic, 128-bit hash function that has
 * excellent avalanche and 2-way bit independence properties.</p>
 *
 * <p>Austin Appleby's C++
 * <a href="https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp">
 * MurmurHash3_x64_128(...), final revision 150</a>,
 * which is in the Public Domain, was the inspiration for this implementation in Java.</p>
 *
 * <p>This implementation of the MurmurHash3 allows hashing of a block of on-heap Memory defined by an offset
 * and length. The calling API also allows the user to supply the small output array of two longs,
 * so that the entire hash function is static and free of object allocations.</p>
 *
 * <p>This implementation produces exactly the same hash result as the
 * MurmurHash3 function in datasketches-java given compatible inputs.</p>
 *
 * <p>This version 4 of the implementation leverages the java.lang.foreign package of JDK-21 in place of
 * the Unsafe class.
 *
 * @author Lee Rhodes
 */
public final class MurmurHash3 {

  private MurmurHash3() { }

  //Provided for backward compatibility

  /**
   * Returns a 128-bit hash of the input.
   * Provided for compatibility with older version of MurmurHash3,
   * but empty or null input now throws IllegalArgumentException.
   * @param in long array
   * @param seed A long valued seed.
   * @return the hash
   */
  public static long[] hash(
      final long[] in,
      final long seed) {
    return MurmurHash3v4.hash(in, seed);
  }

  /**
   * Returns a 128-bit hash of the input.
   * Provided for compatibility with older version of MurmurHash3,
   * but empty or null input now throws IllegalArgumentException.
   * @param in int array
   * @param seed A long valued seed.
   * @return the hash
   */
  public static long[] hash(
      final int[] in,
      final long seed) {
    return MurmurHash3v4.hash(in, seed);
  }

  /**
   * Returns a 128-bit hash of the input.
   * Provided for compatibility with older version of MurmurHash3,
   * but empty or null input now throws IllegalArgumentException.
   * @param in char array
   * @param seed A long valued seed.
   * @return the hash
   */
  public static long[] hash(
      final char[] in,
      final long seed) {
    return MurmurHash3v4.hash(in, seed);
  }

  /**
   * Returns a 128-bit hash of the input.
   * Provided for compatibility with older version of MurmurHash3,
   * but empty or null input now throws IllegalArgumentException.
   * @param in byte array
   * @param seed A long valued seed.
   * @return the hash
   */
  public static long[] hash(
      final byte[] in,
      final long seed) {
    return MurmurHash3v4.hash(in, seed);
  }

  //Single primitive inputs

  /**
   * Returns a 128-bit hash of the input.
   * Note the entropy of the resulting hash cannot be more than 64 bits.
   * @param in a long
   * @param seed A long valued seed.
   * @param hashOut A long array of size 2
   * @return the hash
   */
  public static long[] hash(
      final long in,
      final long seed,
      final long[] hashOut) {
    return MurmurHash3v4.hash(in, seed, hashOut);
  }

  /**
   * Returns a 128-bit hash of the input.
   * Note the entropy of the resulting hash cannot be more than 64 bits.
   * @param in a double
   * @param seed A long valued seed.
   * @param hashOut A long array of size 2
   * @return the hash
   */
  public static long[] hash(
      final double in,
      final long seed,
      final long[] hashOut) {
    return MurmurHash3v4.hash(in, seed, hashOut);
  }

  /**
   * Returns a 128-bit hash of the input.
   * An empty or null input throws IllegalArgumentException.
   * @param in a String
   * @param seed A long valued seed.
   * @param hashOut A long array of size 2
   * @return the hash
   */
  public static long[] hash(
      final String in,
      final long seed,
      final long[] hashOut) {
    return MurmurHash3v4.hash(in, seed, hashOut);
  }

  //The main API calls

  /**
   * Returns a 128-bit hash of the input as a long array of size 2.
   *
   * @param mem The input on-heap Memory. Must be non-null and non-empty.
   * @param offsetBytes the starting point within Memory.
   * @param lengthBytes the total number of bytes to be hashed.
   * @param seed A long valued seed.
   * @param hashOut the size 2 long array for the resulting 128-bit hash
   * @return the hash.
   */
  public static long[] hash(
      final Memory mem,
      final long offsetBytes,
      final long lengthBytes,
      final long seed,
      final long[] hashOut) {
    return MurmurHash3v4.hash(mem, offsetBytes, lengthBytes, seed, hashOut);
  }

  /**
   * Returns a 128-bit hash of the input as a long array of size 2.
   *
   * @param seg The input MemorySegment. Must be non-null and non-empty.
   * @param offsetBytes the starting point within Memory.
   * @param lengthBytes the total number of bytes to be hashed.
   * @param seed A long valued seed.
   * @param hashOut the size 2 long array for the resulting 128-bit hash
   * @return the hash.
   */
  public static long[] hash(
      final MemorySegment seg,
      final long offsetBytes,
      final long lengthBytes,
      final long seed,
      final long[] hashOut) {
    return MurmurHash3v4.hash(seg, offsetBytes, lengthBytes, seed, hashOut);
  }

}
