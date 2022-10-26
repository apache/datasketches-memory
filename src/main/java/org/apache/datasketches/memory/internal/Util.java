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

import java.nio.ByteOrder;

/**
 * Various utility methods and helpers used by the rest of the code
 */
public final class Util {
    
    //Byte Order related
    public static final ByteOrder NON_NATIVE_BYTE_ORDER = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
        ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

    public static ByteOrder otherByteOrder(final ByteOrder order) {
      return (order == ByteOrder.nativeOrder()) ? NON_NATIVE_BYTE_ORDER : ByteOrder.nativeOrder();
    }

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
    
    public static final void zeroCheck(final long value, final String arg) {
        if (value <= 0) {
          throw new IllegalArgumentException("The argument '" + arg + "' may not be negative or zero.");
        }
    }

    public static final void negativeCheck(final long value, final String arg) {
        if (value < 0) {
            throw new IllegalArgumentException("The argument '" + arg + "' may not be negative.");
        }
    }
}