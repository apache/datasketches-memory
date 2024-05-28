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

import static jdk.incubator.foreign.MemoryAccess.getByteAtOffset;

import jdk.incubator.foreign.MemorySegment;

/**
 * @author Lee Rhodes
 */
final class CompareAndCopy {

  private CompareAndCopy() { }

  static int compare(
      final MemorySegment seg1, final long offsetBytes1, final long lengthBytes1,
      final MemorySegment seg2, final long offsetBytes2, final long lengthBytes2) {
    final MemorySegment slice1 = seg1.asSlice(offsetBytes1, lengthBytes1);
    final MemorySegment slice2 = seg2.asSlice(offsetBytes2, lengthBytes2);
    final long mm = slice1.mismatch(slice2);
    if (mm == -1) { return 0; }
    if ((lengthBytes1 > mm) && (lengthBytes2 > mm)) {
      return Integer.compare(
          getByteAtOffset(slice1, mm) & 0XFF, getByteAtOffset(slice2, mm) & 0XFF);
    }
    if (lengthBytes1 == mm) { return -1; }
    return +1;
  }

  static boolean equals(final MemorySegment seg1, final MemorySegment seg2) {
    final long cap1 = seg1.byteSize();
    final long cap2 = seg2.byteSize();
    return (cap1 == cap2) && equals(seg1, 0, seg2, 0, cap1);
  }

  static boolean equals(
      final MemorySegment seg1, final long offsetBytes1,
      final MemorySegment seg2, final long offsetBytes2, final long lengthBytes) {
    final MemorySegment slice1 = seg1.asSlice(offsetBytes1, lengthBytes);
    final MemorySegment slice2 = seg2.asSlice(offsetBytes2, lengthBytes);
    return slice1.mismatch(slice2) == -1;
  }

  static void copy(final MemorySegment srcSegment, final long srcOffsetBytes,
      final MemorySegment dstSegment, final long dstOffsetBytes, final long lengthBytes) {
    final MemorySegment srcSlice = srcSegment.asSlice(srcOffsetBytes, lengthBytes);
    final MemorySegment dstSlice = dstSegment.asSlice(dstOffsetBytes, lengthBytes);
    dstSlice.copyFrom(srcSlice);
  }

}
