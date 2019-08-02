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

/**
 * Stripped down version of
 * https://github.com/protocolbuffers/protobuf\
 * /blob/master/java/core/src/test/java/com/google/protobuf/IsValidUtf8TestUtil.java
 *
 * Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 */
public class IsValidUtf8TestUtil {

  // 128 - [chars 0x0000 to 0x007f]
  static final long ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS = (0x007f - 0x0000) + 1;

  // 128
  static final long EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT = ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS;

  // 1920 [chars 0x0080 to 0x07FF]
  static final long TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS = (0x07FF - 0x0080) + 1;

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
      ((0xFFFF - 0x0800) + 1) - THREE_BYTE_SURROGATES;

  // 2,650,112
  static final long EXPECTED_THREE_BYTE_ROUNDTRIPPABLE_COUNT =
      // All one byte characters
      (long) Math.pow(EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT, 3) +
      (// One two byte character and a one byte character
      2 * TWO_BYTE_ROUNDTRIPPABLE_CHARACTERS * ONE_BYTE_ROUNDTRIPPABLE_CHARACTERS) +
      // Three byte characters
      THREE_BYTE_ROUNDTRIPPABLE_CHARACTERS;

}
