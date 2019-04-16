/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.*;

/**
 * The XxHash is a fast, non-cryptographic, 64-bit hash function that has
 * excellent avalanche and 2-way bit independence properties.
 * This java version used the C++ version and the OpenHFT/Zero-Allocation-Hashing implementation
 * referenced below as inspiration.
 *
 * <p>The C++ source repository:
 * <a href="https://github.com/Cyan4973/xxHash">
 * https://github.com/Cyan4973/xxHash</a>. It has a BSD 2-Clause License:
 * <a href="http://www.opensource.org/licenses/bsd-license.php">
 * http://www.opensource.org/licenses/bsd-license.php</a>
 *
 * <p>Portions of this code were leveraged from
 * <a href="https://github.com/OpenHFT/Zero-Allocation-Hashing/blob/master/src/main/java/net/openhft/hashing/XxHash.java">
 * OpenHFT/Zero-Allocation-Hashing</a>, which has an Apache 2 license as does this site.
 *
 * @author Lee Rhodes
 */
public class XxHash64 {
  // Unsigned, 64-bit primes
  private static final long P1 = -7046029288634856825L;
  private static final long P2 = -4417276706812531889L;
  private static final long P3 =  1609587929392839161L;
  private static final long P4 = -8796714831421723037L;
  private static final long P5 =  2870177450012600261L;

  /**
   * Returns the 64-bit hash of the sequence of bytes in the unsafeObject specified by
   * <i>cumOffsetBytes</i>, <i>lengthBytes</i> and a <i>seed</i>.
   *
   * @param unsafeObj A reference to the object parameter required by unsafe. It may be null.
   * @param cumOffsetBytes cumulative offset in bytes of this object from the backing resource
   * including any user given offsetBytes. This offset may also include other offset components
   * such as the native off-heap memory address, DirectByteBuffer split offsets, region offsets,
   * and unsafe arrayBaseOffsets.
   * @param lengthBytes the length in bytes of the sequence to be hashed
   * @param seed a given seed
   * @return the 64-bit hash of the sequence of bytes in the unsafeObject specified by
   * <i>cumOffsetBytes</i>, <i>lengthBytes</i> and a <i>seed</i>.
   */
  static long hash(final Object unsafeObj, long cumOffsetBytes, final long lengthBytes,
      final long seed) {
    long hash;
    long remaining = lengthBytes;

    if (remaining >= 32) {
      long v1 = seed + P1 + P2;
      long v2 = seed + P2;
      long v3 = seed;
      long v4 = seed - P1;

      do {
        v1 += unsafe.getLong(unsafeObj, cumOffsetBytes) * P2;
        v1 = Long.rotateLeft(v1, 31);
        v1 *= P1;

        v2 += unsafe.getLong(unsafeObj, cumOffsetBytes + 8L) * P2;
        v2 = Long.rotateLeft(v2, 31);
        v2 *= P1;

        v3 += unsafe.getLong(unsafeObj, cumOffsetBytes + 16L) * P2;
        v3 = Long.rotateLeft(v3, 31);
        v3 *= P1;

        v4 += unsafe.getLong(unsafeObj, cumOffsetBytes + 24L) * P2;
        v4 = Long.rotateLeft(v4, 31);
        v4 *= P1;

        cumOffsetBytes += 32;
        remaining -= 32;
      } while (remaining >= 32);

      hash = Long.rotateLeft(v1, 1)
          + Long.rotateLeft(v2, 7)
          + Long.rotateLeft(v3, 12)
          + Long.rotateLeft(v4, 18);

      v1 *= P2;
      v1 = Long.rotateLeft(v1, 31);
      v1 *= P1;
      hash ^= v1;
      hash = (hash * P1) + P4;

      v2 *= P2;
      v2 = Long.rotateLeft(v2, 31);
      v2 *= P1;
      hash ^= v2;
      hash = (hash * P1) + P4;

      v3 *= P2;
      v3 = Long.rotateLeft(v3, 31);
      v3 *= P1;
      hash ^= v3;
      hash = (hash * P1) + P4;

      v4 *= P2;
      v4 = Long.rotateLeft(v4, 31);
      v4 *= P1;
      hash ^= v4;
      hash = (hash * P1) + P4;
    } //end remaining >= 32
    else {
      hash = seed + P5;
    }

    hash += lengthBytes;

    while (remaining >= 8) {
      long k1 = unsafe.getLong(unsafeObj, cumOffsetBytes);
      k1 *= P2;
      k1 = Long.rotateLeft(k1, 31);
      k1 *= P1;
      hash ^= k1;
      hash = (Long.rotateLeft(hash, 27) * P1) + P4;
      cumOffsetBytes += 8;
      remaining -= 8;
    }

    if (remaining >= 4) { //treat as unsigned ints
      hash ^= (unsafe.getInt(unsafeObj, cumOffsetBytes) & 0XFFFF_FFFFL) * P1;
      hash = (Long.rotateLeft(hash, 23) * P2) + P3;
      cumOffsetBytes += 4;
      remaining -= 4;
    }

    while (remaining != 0) { //treat as unsigned bytes
      hash ^= (unsafe.getByte(unsafeObj, cumOffsetBytes) & 0XFFL) * P5;
      hash = Long.rotateLeft(hash, 11) * P1;
      --remaining;
      ++cumOffsetBytes;
    }

    return finalize(hash);
  }

  private static long finalize(long hash) {
    hash ^= hash >>> 33;
    hash *= P2;
    hash ^= hash >>> 29;
    hash *= P3;
    hash ^= hash >>> 32;
    return hash;
  }

  /* with char|byte|boolean|short|int|long|float|double arrayElement */
  /**
   * Hash the given arr starting at the given offset and continuing for the given length using the
   * given seed.
   * @param arr the given array
   * @param offsetChars starting at this offset
   * @param lengthChars continuing for this length
   * @param seed the given seed
   * @return the hash
   */
  public static long hashChars(final char[] arr, final long offsetChars,
      final long lengthChars, final long seed) {
    return hash(arr, ARRAY_CHAR_BASE_OFFSET + (offsetChars << CHAR_SHIFT),
        lengthChars << CHAR_SHIFT, seed);
  }

  /* endwith */

  /**
   * Hash the given arr starting at the given offset and continuing for the given length using the
   * given seed.
   * @param str the given string
   * @param offsetChars starting at this offset
   * @param lengthChars continuing for this length
   * @param seed the given seed
   * @return the hash
   */
  public static long hashString(final String str, final long offsetChars,
      final long lengthChars, final long seed) {
    return hashChars(str.toCharArray(), offsetChars, lengthChars, seed);
  }

}

