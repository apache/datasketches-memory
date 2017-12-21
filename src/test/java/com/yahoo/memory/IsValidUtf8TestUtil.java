/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Stripped down version of https://github.com/google/protobuf/blob/3e944aec9ebdf5043780fba751d604c0a55511f2/
 * java/core/src/test/java/com/google/protobuf/IsValidUtf8TestUtil.java
 *
 * Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 */
public class IsValidUtf8TestUtil
{

  // 128 - [chars 0x0000 to 0x007f]
  static final long ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS = 0x007f - 0x0000 + 1;

  // 128
  static final long EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT = ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS;

  // 1920 [chars 0x0080 to 0x07FF]
  static final long TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS = 0x07FF - 0x0080 + 1;

  // 18,304
  static final long EXPECTED_TWO_BYTE_ROUNDTRIPPABLE_COUNT =
      // Both bytes are one byte characters
      (long) Math.pow(EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT, 2) +
      // The possible number of two byte characters
      TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS;

  // 2048
  static final long THREE_BYTE_SURROGATES = 2 * 1024;

  // 61,440 [chars 0x0800 to 0xFFFF, minus surrogates]
  static final long THREE_BYTE_ROUNDTRIPPABLE_CHARACTERS =
      0xFFFF - 0x0800 + 1 - THREE_BYTE_SURROGATES;

  // 2,650,112
  static final long EXPECTED_THREE_BYTE_ROUNDTRIPPABLE_COUNT =
      // All one byte characters
      (long) Math.pow(EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT, 3) +
      // One two byte character and a one byte character
      2 * TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS * ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS +
      // Three byte characters
      THREE_BYTE_ROUNDTRIPPABLE_CHARACTERS;

  // 1,048,576 [chars 0x10000L to 0x10FFFF]
  static final long FOUR_BYTE_ROUNDTRIPPABLE_CHARACTERS = 0x10FFFF - 0x10000L + 1;

  // 289,571,839
  static final long EXPECTED_FOUR_BYTE_ROUNDTRIPPABLE_COUNT =
      // All one byte characters
      (long) Math.pow(EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT, 4) +
      // One and three byte characters
      2 * THREE_BYTE_ROUNDTRIPPABLE_CHARACTERS * ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS +
      // Two two byte characters
      TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS * TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS +
      // Permutations of one and two byte characters
      3 * TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS * ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS
      * ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS
      +
      // Four byte characters
      FOUR_BYTE_ROUNDTRIPPABLE_CHARACTERS;
}
