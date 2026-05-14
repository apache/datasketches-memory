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

package org.apache.datasketches.memory.internal;

import static java.util.Arrays.fill;

import java.nio.ByteOrder;

/**
 * @author Lee Rhodes
 */
public final class Util {

  private Util() { }

  /**
   * Java line separator that is platform independent.
   */
  public static final String LS = System.getProperty("line.separator");

  /**
   * The static final for <i>ByteOrder.nativeOrder()</i>.
   */
  public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

  /**
   * The static final for NON <i>ByteOrder.nativeOrder()</i>.
   */
  public static final ByteOrder NON_NATIVE_BYTE_ORDER =
      (NATIVE_BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
  
  /**
   * Returns the opposite byte order from the given one.
   * @param order the given byte order
   * @return the opposite byte order from the given one
   */
  public static ByteOrder otherByteOrder(final ByteOrder order) {
    return (order == NATIVE_BYTE_ORDER) ? NON_NATIVE_BYTE_ORDER : NATIVE_BYTE_ORDER;
  }
  
  /**
   * Don't use sun.misc.Unsafe#copyMemory to copy blocks of memory larger than this
   * threshold, because internally it doesn't have safepoint polls, that may cause long
   * "Time To Safe Point" pauses in the application. This has been fixed in JDK 9 (see
   * https://bugs.openjdk.java.net/browse/JDK-8149596 and
   * https://bugs.openjdk.java.net/browse/JDK-8141491), but not in JDK 8, so the Memory library
   * should keep having this boilerplate as long as it supports Java 8.
   *
   * <p>A reference to this can be found in java.nio.Bits.</p>
   */
  public static final int UNSAFE_COPY_THRESHOLD_BYTES = 1024 * 1024;

  /**
   * Returns true if the given byteOrder is the same as the native byte order.
   * @param byteOrder the given byte order
   * @return true if the given byteOrder is the same as the native byte order.
   */
  public static boolean isNativeByteOrder(final ByteOrder byteOrder) {
    if (byteOrder == null) {
      throw new IllegalArgumentException("ByteOrder parameter cannot be null.");
    }
    return ByteOrder.nativeOrder() == byteOrder;
  }

  /**
   * Prepend the given string with zeros. If the given string is equal or greater than the given
   * field length, it will be returned without modification.
   * @param s the given string
   * @param fieldLength desired total field length including the given string
   * @return the given string prepended with zeros.
   */
  public static final String zeroPad(final String s, final int fieldLength) {
    return characterPad(s, fieldLength, '0', false);
  }

  /**
   * Prepend or postpend the given string with the given character to fill the given field length.
   * If the given string is equal or greater than the given field length, it will be returned
   * without modification.
   * @param s the given string
   * @param fieldLength the desired field length
   * @param padChar the desired pad character
   * @param postpend if true append the pacCharacters to the end of the string.
   * @return prepended or postpended given string with the given character to fill the given field length.
   */
  public static final String characterPad(final String s, final int fieldLength, final char padChar, final boolean postpend) {
    final int sLen = s.length();
    if (sLen < fieldLength) {
      final char[] cArr = new char[fieldLength - sLen];
      fill(cArr, padChar);
      final String addstr = String.valueOf(cArr);
      return (postpend) ? s.concat(addstr) : addstr.concat(s);
    }
    return s;
  }

  /**
   * Checks if given value is &lt; zero.
   * @param value the given value
   * @param arg a meaningful name of the actual argument.
   * @throws IllegalArgumentException if value is &lt; 0.
   */
  public static final void negativeCheck(final long value, final String arg) {
    if (value < 0) {
      throw new IllegalArgumentException("The argument '" + arg + "' may not be negative.");
    }
  }
}
